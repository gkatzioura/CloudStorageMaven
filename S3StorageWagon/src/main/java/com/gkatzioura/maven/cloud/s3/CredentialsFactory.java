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

import org.apache.maven.wagon.authentication.AuthenticationInfo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

public class CredentialsFactory {

    public AWSStaticCredentialsProvider create(AuthenticationInfo authenticationInfo) {

        final AWSCredentials awsCredentials;

        if(authenticationInfo==null) {
            awsCredentials = new ProfileCredentialsProvider().getCredentials();
        } else {
            awsCredentials = new BasicAWSCredentials(authenticationInfo.getUserName(),authenticationInfo.getPassword());
        }

        return new AWSStaticCredentialsProvider(awsCredentials);
    }
}
