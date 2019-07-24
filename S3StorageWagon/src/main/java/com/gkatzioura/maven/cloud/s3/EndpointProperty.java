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


public class EndpointProperty {

    private static final String S3_ENDPOINT = "S3_ENDPOINT";
    private String endpoint;

    /**
     *
     * @param endpoint may be null
     */
    public EndpointProperty(String endpoint){

        this.endpoint = endpoint;
    }

    public static final EndpointProperty empty() {
        return new EndpointProperty(null);
    }

    public boolean isPresent() {
        return endpoint != null || System.getProperty(S3_ENDPOINT)!=null;
    }

    /**
     * @return the endpoint set in the constructor or set using the S3_ENDPOINT system property or null
     * */
    public String get() {
        if (endpoint != null){
            return endpoint;
        }
        String endpointEnv = System.getProperty(S3_ENDPOINT);
        if(endpointEnv != null) {
            return endpointEnv;
        }
        return null;
    }

}
