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

import org.apache.maven.wagon.repository.Repository;

public class BaseDirectoryResolver {

    public String resolve(Repository repository) {

        StringBuilder stringBuilder = new StringBuilder(repository.getBasedir()).deleteCharAt(0);

        if ((stringBuilder.length() != 0) && (stringBuilder.charAt(stringBuilder.length() - 1) != '/')) {
            stringBuilder.append('/');
        }

        return stringBuilder.toString();
    }
}
