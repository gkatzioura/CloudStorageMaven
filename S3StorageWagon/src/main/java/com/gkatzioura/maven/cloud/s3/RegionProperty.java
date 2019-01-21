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

import com.amazonaws.regions.Regions;

import java.util.Optional;

public class RegionProperty {

    public static final String AWS_DEFAULT_REGION_TAG = "AWS_DEFAULT_REGION";
    private String region;

    /**
     *
     * @param region may be null
     */
    public RegionProperty(String region){

        this.region = region;
    }

    /**
     * return the region set in the constructor or the region set using the AWS_DEFAULT_REGION system property or the default AWS region (us_west_2)
     * */
    public String get() {
        if (region != null){
            return region;
        }
        String regionEnv = System.getProperty("AWS_DEFAULT_REGION");
        if(regionEnv != null) {
            return regionEnv;
        }
        return Regions.DEFAULT_REGION.getName();
    }

}
