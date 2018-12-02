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

package com.gkatzioura.maven.cloud.gcs;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.authentication.AuthenticationException;

import com.gkatzioura.maven.cloud.resolver.KeyResolver;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class GoogleStorageRepository {

    private final String bucket;
    private final String baseDirectory;
    private final KeyResolver keyResolver;
    private Storage storage;

    private static final Logger LOGGER = Logger.getLogger(GoogleStorageRepository.class.getName());

    public GoogleStorageRepository(String bucket, String directory) {
        this.keyResolver = new KeyResolver();
        this.bucket = bucket;
        this.baseDirectory = directory;
    }

    public void connect() throws AuthenticationException {

        try {
            storage = StorageOptions.getDefaultInstance().getService();
            storage.list(bucket, Storage.BlobListOption.pageSize(1));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Could not establish connection with google cloud",e);
            throw new AuthenticationException("Please configure you google cloud account by logging using gcloud and specify a default project");
        }
    }

    public void copy(String resourceName, File destination) throws ResourceDoesNotExistException {

        final String key = resolveKey(resourceName);

        LOGGER.log(Level.FINER,String.format("Downloading key %s from bucket %s into %s",key,bucket ,destination.getAbsolutePath()));

        Blob blob = storage.get(bucket, resolveKey(resourceName));

        if(blob==null) {
            LOGGER.log(Level.FINER,String.format("Blob %s does not exist",key));
            throw new ResourceDoesNotExistException(key);
        }
        blob.downloadTo(destination.toPath());
    }

    public boolean newResourceAvailable(String resourceName,long timeStamp) {

        final String key = resolveKey(resourceName);

        LOGGER.log(Level.FINER,String.format("Checking if new key %s exists",key));

        Blob blob = storage.get(bucket, key);

        if(blob==null) {
            return false;
        }

        long updated = blob.getUpdateTime();
        return updated>timeStamp;
    }

    public void put(InputStream inputStream,String destination) {

        String key = resolveKey(destination);

        LOGGER.log(Level.FINER,String.format("Uploading key %s ",key));

        BlobInfo blobInfo = BlobInfo.newBuilder(bucket,key).build();

        Blob createdBlob = storage.create(blobInfo,inputStream);
        LOGGER.info(String.format("Blob created at %d",createdBlob.getCreateTime()));
    }

    public List<String> list(String path) {

        String key = resolveKey(path);

        LOGGER.log(Level.FINER,String.format("Listing files for %s",path));

        Page<Blob> page = storage.list(bucket, Storage.BlobListOption.prefix(key));
        return totalBlobs(page);
    }

    private List<String> totalBlobs(Page<Blob> page) {

        List<String> blobs = new ArrayList<>();
        page.getValues().forEach(bv->blobs.add(bv.getName()));
        if(page.hasNextPage()) {
            Page<Blob> newPage = storage.list(bucket,Storage.BlobListOption.pageToken(page.getNextPageToken()));
            blobs.addAll(totalBlobs(newPage));
        }

        return blobs;
    }

    public boolean exists(String resourceName) {
        final String key = resolveKey(resourceName);
        Blob blob = storage.get(bucket, resourceName);
        return blob.exists();
    }

    public void disconnect() {
        storage = null;
    }

    private String resolveKey(String path) {
        return keyResolver.resolve(baseDirectory,path);
    }

}
