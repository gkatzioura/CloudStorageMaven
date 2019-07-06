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

import java.util.logging.Logger;

import org.apache.maven.wagon.authentication.AuthenticationInfo;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

public class CredentialsFactory {

    private static final Logger LOGGER = Logger.getLogger(CredentialsFactory.class.getName());

    /**
     * Creates an {@link AWSCredentialsProvider} from the passed {@link AuthenticationInfo}. This should contain the
     * username and password used to authenticate when connecting to AWS .<p>
     * When {@code authenticationInfo} is passed as {@code null}, a {@link DefaultAWSCredentialsProviderChain} will be
     * used. This is an authentication provider that gets the credentials from Java environment properties, system
     * environment variables or other global locations. See the {@link DefaultAWSCredentialsProviderChain} documentation
     * for details.
     *
     * @param authenticationInfo an {@link AuthenticationInfo} containing the AWS credentials to use
     * @return a newly-built {@link AWSCredentialsProvider} with the credentials associated to the passed
     *         {@code authenticationInfo}
     */
    public AWSCredentialsProvider create(AuthenticationInfo authenticationInfo) {
        if(authenticationInfo==null) {
            return new DefaultAWSCredentialsProviderChain();
        } else {
            LOGGER.info("Using static credentials provider");
            return new AWSStaticCredentialsProvider(new BasicAWSCredentials(authenticationInfo.getUserName(),authenticationInfo.getPassword()));
        }
    }
}
