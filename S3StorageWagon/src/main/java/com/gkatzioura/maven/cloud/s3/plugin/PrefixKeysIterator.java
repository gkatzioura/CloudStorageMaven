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

package com.gkatzioura.maven.cloud.s3.plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class PrefixKeysIterator implements Iterator<String> {

    private AmazonS3 amazonS3;
    private String prefix;
    private String bucket;

    private ObjectListing tempListing = null;
    private List<S3ObjectSummary> currentKeys = new ArrayList<>();

    public PrefixKeysIterator(AmazonS3 amazonS3, String bucket, String prefix) {
        this.amazonS3 = amazonS3;
        this.bucket = bucket;
        this.prefix = prefix;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override public void forEachRemaining(Consumer<? super String> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasNext() {
        if(currentKeys.size()>0) {
            return true;
        }

        fetchKeysIfExist();
        return currentKeys.size()>0;
    }

    private void fetchKeysIfExist() {
        if(tempListing==null) {
            tempListing = getObjectListing();
            currentKeys.addAll(tempListing.getObjectSummaries());
        } else {
            if(tempListing.isTruncated()) {
                tempListing = amazonS3.listNextBatchOfObjects(tempListing);
                currentKeys.addAll(tempListing.getObjectSummaries());
            }
        }
    }

    private ObjectListing getObjectListing() {

        return amazonS3.listObjects(new ListObjectsRequest()
                                            .withBucketName(bucket)
                                            .withPrefix(prefix));
    }

    @Override
    public String next() {
        if(!hasNext()) {
            return null;
        }

        return currentKeys.remove(0).getKey();
    }

}
