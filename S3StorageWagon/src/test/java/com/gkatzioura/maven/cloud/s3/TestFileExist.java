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

import java.util.List;

import org.junit.Test;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;

public class TestFileExist {

    @Test
    public void testSTuff() {



        //AWSCredentials awsCredentials = new BasicAWSCredentials("asddsadas", "asddsds");

        AWSCredentials awsCredentials = new ProfileCredentialsProvider().getCredentials();
        AWSStaticCredentialsProvider awsStaticCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);

        AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
                                                 .withCredentials(awsStaticCredentialsProvider)
                                                 .withRegion(Regions.EU_WEST_1)
                                                 .build();

        List<Bucket> buckets = amazonS3.listBuckets();

        ObjectMetadata objectMetadata = amazonS3.getObjectMetadata("egkatzioura", "alla.txt");

        System.out.println("");
    }

}
