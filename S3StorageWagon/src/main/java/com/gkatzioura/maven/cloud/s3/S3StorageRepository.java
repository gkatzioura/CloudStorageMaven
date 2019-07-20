/*
 * Copyright 2018 Emmanouil Gkatziouras
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gkatzioura.maven.cloud.s3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gkatzioura.maven.cloud.s3.utils.S3Connect;
import org.apache.commons.io.IOUtils;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.gkatzioura.maven.cloud.resolver.KeyResolver;
import com.gkatzioura.maven.cloud.transfer.TransferProgress;
import com.gkatzioura.maven.cloud.transfer.TransferProgressFileInputStream;
import com.gkatzioura.maven.cloud.transfer.TransferProgressFileOutputStream;
import com.gkatzioura.maven.cloud.wagon.PublicReadProperty;

public class S3StorageRepository {

    private final String bucket;
    private final String baseDirectory;

    private final KeyResolver keyResolver = new KeyResolver();

    private AmazonS3 amazonS3;
    private PublicReadProperty publicReadProperty;

    private static final Logger LOGGER = Logger.getLogger(S3StorageRepository.class.getName());

    public S3StorageRepository(String bucket) {
        this.bucket = bucket;
        this.baseDirectory = "";
        this.publicReadProperty = new PublicReadProperty(false);
    }

    public S3StorageRepository(String bucket, PublicReadProperty publicReadProperty) {
        this.bucket = bucket;
        this.baseDirectory = "";
        this.publicReadProperty = publicReadProperty;
    }

    public S3StorageRepository(String bucket, String baseDirectory) {
        this.bucket = bucket;
        this.baseDirectory = baseDirectory;
        this.publicReadProperty = new PublicReadProperty(false);
    }

    public S3StorageRepository(String bucket, String baseDirectory, PublicReadProperty publicReadProperty) {
        this.bucket = bucket;
        this.baseDirectory = baseDirectory;
        this.publicReadProperty = publicReadProperty;
    }


    public void connect(AuthenticationInfo authenticationInfo, String region, EndpointProperty endpoint, PathStyleEnabledProperty pathStyle) throws AuthenticationException {
        this.amazonS3 = S3Connect.connect(authenticationInfo, region, endpoint, pathStyle);
    }

    public void copy(String resourceName, File destination, TransferProgress transferProgress) throws TransferFailedException, ResourceDoesNotExistException {

        final String key = resolveKey(resourceName);

        try {

            final S3Object s3Object;
            try {
                s3Object = amazonS3.getObject(bucket, key);
            } catch (AmazonS3Exception e) {
                throw new ResourceDoesNotExistException("Resource does not exist");
            }
            destination.getParentFile().mkdirs();//make sure the folder exists or the outputStream will fail.
            try(OutputStream outputStream = new TransferProgressFileOutputStream(destination,transferProgress);
                InputStream inputStream = s3Object.getObjectContent()) {
                IOUtils.copy(inputStream,outputStream);
            }
        } catch (AmazonS3Exception |IOException e) {
            LOGGER.log(Level.SEVERE,"Could not transfer file", e);
            throw new TransferFailedException("Could not download resource "+key);
        }
    }

    public void put(File file, String destination,TransferProgress transferProgress) throws TransferFailedException {

        final String key = resolveKey(destination);

        try {
            try(InputStream inputStream = new TransferProgressFileInputStream(file,transferProgress)) {
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucket,key,inputStream,createContentLengthMetadata(file));
                applyPublicRead(putObjectRequest);
                amazonS3.putObject(putObjectRequest);
            }
        } catch (AmazonS3Exception | IOException e) {
            LOGGER.log(Level.SEVERE,"Could not transfer file ",e);
            throw new TransferFailedException("Could not transfer file "+file.getName());
        }
    }

    private ObjectMetadata createContentLengthMetadata(File file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.length());
        return metadata;
    }

    public boolean newResourceAvailable(String resourceName,long timeStamp) throws ResourceDoesNotExistException {

        final String key = resolveKey(resourceName);

        LOGGER.log(Level.FINER,String.format("Checking if new key %s exists",key));

        try {
            ObjectMetadata objectMetadata = amazonS3.getObjectMetadata(bucket, key);

            long updated = objectMetadata.getLastModified().getTime();
            return updated>timeStamp;
        } catch (AmazonS3Exception e) {
            LOGGER.log(Level.SEVERE,String.format("Could not retrieve %s",key),e);
            throw new ResourceDoesNotExistException("Could not retrieve key "+key);
        }
    }


    public List<String> list(String path) {

        String key = resolveKey(path);

        ObjectListing objectListing = amazonS3.listObjects(new ListObjectsRequest()
                    .withBucketName(bucket)
                    .withPrefix(key));
        List<String> objects = new ArrayList<>();
        retrieveAllObjects(objectListing, objects);
        return objects;
    }

    private void applyPublicRead(PutObjectRequest putObjectRequest) {
        if(publicReadProperty.get()) {
            LOGGER.info("Public read was set to true");
            putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
        }
    }

    private void retrieveAllObjects(ObjectListing objectListing, List<String> objects) {

        objectListing.getObjectSummaries().forEach( os-> objects.add(os.getKey()));

        if(objectListing.isTruncated()) {
            ObjectListing nextObjectListing = amazonS3.listNextBatchOfObjects(objectListing);
            retrieveAllObjects(nextObjectListing, objects);
        }
    }

    public boolean exists(String resourceName) {

        final String key = resolveKey(resourceName);

        try {
            amazonS3.getObjectMetadata(bucket, key);
            return true;
        } catch (AmazonS3Exception e) {
            return false;
        }
    }

    public void disconnect() {
        amazonS3 = null;
    }

    private String resolveKey(String path) {
        return keyResolver.resolve(baseDirectory,path);
    }

    public String getBucket() {
        return bucket;
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }


}
