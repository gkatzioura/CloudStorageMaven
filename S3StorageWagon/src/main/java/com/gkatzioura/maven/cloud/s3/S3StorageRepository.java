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
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.gkatzioura.maven.cloud.resolver.KeyResolver;
import com.gkatzioura.maven.cloud.transfer.TransferProgress;
import com.gkatzioura.maven.cloud.transfer.TransferProgressFileInputStream;
import com.gkatzioura.maven.cloud.transfer.TransferProgressFileOutputStream;

public class S3StorageRepository {

    private final String bucket;
    private final String baseDirectory;

    private final CredentialsFactory credentialsFactory = new CredentialsFactory();
    private final KeyResolver keyResolver = new KeyResolver();

    private AmazonS3 amazonS3;

    private static final Logger LOGGER = LoggerFactory.getLogger(S3StorageRepository.class);

    public S3StorageRepository(String bucket, String baseDirectory) {

        this.bucket = bucket;
        this.baseDirectory = baseDirectory;
    }

    public void connect(AuthenticationInfo authenticationInfo) throws AuthenticationException {

        try {
            Optional<String> region = new RegionProperty().get();

            AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                                                                 .withCredentials(credentialsFactory.create(authenticationInfo));

            if(region.isPresent()) {
                builder.withRegion(region.get());
            }

            amazonS3 = builder.build();
            amazonS3.listBuckets();

            LOGGER.debug("Connected to s3 using bucket {} with base direcctory ",bucket,baseDirectory);

        } catch (Exception e) {
            throw new AuthenticationException("Could not authenticate",e);
        }
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

            try(OutputStream outputStream = new TransferProgressFileOutputStream(destination,transferProgress);
                InputStream inputStream = s3Object.getObjectContent()) {
                IOUtils.copy(inputStream,outputStream);
            }
        } catch (AmazonS3Exception |IOException e) {
            LOGGER.error("Could not transfer file",e);
            throw new TransferFailedException("Could not download resource "+key);
        }
    }

    public void put(File file, String destination,TransferProgress transferProgress) throws TransferFailedException {

        final String key = resolveKey(destination);

        try {
            try(InputStream inputStream = new TransferProgressFileInputStream(file,transferProgress)) {
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucket,key,inputStream,new ObjectMetadata());
                amazonS3.putObject(putObjectRequest);
            }
        } catch (AmazonS3Exception | IOException e) {
            LOGGER.error("Could not transfer file ",e);
            throw new TransferFailedException("Could not transfer file "+file.getName());
        }
    }

    public boolean newResourceAvailable(String resourceName,long timeStamp) throws ResourceDoesNotExistException {

        final String key = resolveKey(resourceName);

        LOGGER.debug("Checking if new key {} exists",key);

        try {
            ObjectMetadata objectMetadata = amazonS3.getObjectMetadata(bucket, key);

            long updated = objectMetadata.getLastModified().getTime();
            return updated>timeStamp;
        } catch (AmazonS3Exception e) {
            LOGGER.error("Could not retrieve {}",key,e);
            throw new ResourceDoesNotExistException("Could not retrieve key "+key);
        }
    }


    public List<String> list(String path) {

        String key = resolveKey(path);

        ObjectListing objectListing = amazonS3.listObjects(new ListObjectsRequest()
                    .withBucketName(bucket)
                    .withPrefix(key));

        return totalObjects(objectListing);
    }

    private List<String> totalObjects(ObjectListing objectListing) {

        List<String> objects = new ArrayList<>();

        objectListing.getObjectSummaries().forEach(os->objects.add(os.getKey()));

        if(objectListing.isTruncated()) {

            ObjectListing nextObjectListing = amazonS3.listNextBatchOfObjects(objectListing);
            objects.addAll(totalObjects(nextObjectListing));
        }

        return objects;
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

}
