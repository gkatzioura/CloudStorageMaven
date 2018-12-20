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

    private static final String AWS_DEFAULT_REGION_TAG = "AWS_DEFAULT_REGION";

    /**
     * return the region set using  the AWS_DEFAULT_REGION system property or the default AWS region (us_west_2)
     * */
    public Optional<String> get() {

        String region = System.getProperty("AWS_DEFAULT_REGION");

        if(region==null) {
            return Optional.of(Regions.DEFAULT_REGION.getName());
        }

        return Optional.of(region);
    }

}
