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

import java.util.logging.Logger;

import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

public class ConnectionStringFactory {

    private static final String CONNECTION_STRING_TEMPLATE = "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net";

    private static final Logger LOGGER = Logger.getLogger(ConnectionStringFactory.class.getName());

    public String create(AuthenticationInfo authenticationInfo) throws AuthenticationException {

        if(authenticationInfo==null) {
            throw new AuthenticationException("Please provide storage account credentials");
        }

        String username = authenticationInfo.getUserName();
        String password = authenticationInfo.getPassword();

        if (username == null || username.isEmpty()) {
            return password;
        } else {
            return String.format(CONNECTION_STRING_TEMPLATE, username, password);
        }
    }

    /**
     * This shall create the connection string based on the environmental params
     * @return
     * @throws AuthenticationException
     */
    public String create() throws AuthenticationException {
        String accountName = System.getenv("ACCOUNT_NAME");
        String accountKey = System.getenv("ACCOUNT_KEY");

        if(accountName ==null || accountKey == null) {
            throw new AuthenticationException("Please provide storage account credentials using environmental variables");
        }

        return String.format(CONNECTION_STRING_TEMPLATE,accountName,accountKey);
    }

}
