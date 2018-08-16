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

package io.github.ruschecker.s3;

import java.util.Optional;

public class RegionProperty {

    private static final String AWS_DEFAULT_REGION_TAG = "AWS_DEFAULT_REGION";

    public Optional<String> get() {

        String region = System.getProperty("AWS_DEFAULT_REGION");

        if(region==null) {
            return Optional.empty();
        }

        return Optional.of(region);
    }

}
