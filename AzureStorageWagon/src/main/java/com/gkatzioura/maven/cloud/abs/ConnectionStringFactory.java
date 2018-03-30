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

package com.gkatzioura.maven.cloud.abs;

import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionStringFactory {

    private static final String CONNECTION_STRING_TEMPLATE = "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionStringFactory.class);

    public String create(AuthenticationInfo authenticationInfo) throws AuthenticationException {

        if(authenticationInfo==null) {
            throw new AuthenticationException("Please provide storage account credentials");
        }

        return String.format(CONNECTION_STRING_TEMPLATE,authenticationInfo.getUserName(),authenticationInfo.getPassword());
    }

}
