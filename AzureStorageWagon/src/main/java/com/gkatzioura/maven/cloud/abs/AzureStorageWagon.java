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

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;

import com.gkatzioura.maven.cloud.transfer.TransferProgress;
import com.gkatzioura.maven.cloud.transfer.TransferProgressImpl;
import com.gkatzioura.maven.cloud.wagon.AbstractStorageWagon;

public class AzureStorageWagon extends AbstractStorageWagon {

    private AzureStorageRepository azureStorageRepository;

    private static final Logger LOGGER = Logger.getLogger(AzureStorageWagon.class.getName());

    @Override
    public void get(String resourceName, File destination) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {

        Resource resource = new Resource(resourceName);
        transferListenerContainer.fireTransferInitiated(resource, TransferEvent.REQUEST_GET);
        transferListenerContainer.fireTransferStarted(resource, TransferEvent.REQUEST_GET, destination);

        final TransferProgress transferProgress = new TransferProgressImpl(resource, TransferEvent.REQUEST_GET, transferListenerContainer);

        try {
            azureStorageRepository.copy(resourceName,destination,transferProgress);
            transferListenerContainer.fireTransferCompleted(resource,TransferEvent.REQUEST_GET);
        } catch (Exception e) {
            transferListenerContainer.fireTransferError(resource,TransferEvent.REQUEST_GET,e);
            throw e;
        }
    }

    @Override
    public boolean getIfNewer(String resourceName, File file, long l) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {

        Resource resource = new Resource(resourceName);

        try {
            if(azureStorageRepository.newResourceAvailable(resourceName, l)) {
                get(resourceName,file);
                return true;
            }

            return false;
        } catch (TransferFailedException| ResourceDoesNotExistException| AuthorizationException e) {
            this.transferListenerContainer.fireTransferError(resource, TransferEvent.REQUEST_GET, e);
            throw e;
        }
    }

    @Override
    public void put(File file, String resourceName) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        resourceName = Paths.get(resourceName).normalize().toString();
        Resource resource = new Resource(resourceName);

        LOGGER.log(Level.FINER, String.format("Uploading file %s to %s", file.getAbsolutePath(), resourceName));

        transferListenerContainer.fireTransferInitiated(resource,TransferEvent.REQUEST_PUT);
        transferListenerContainer.fireTransferStarted(resource,TransferEvent.REQUEST_PUT, file);
        final TransferProgress transferProgress = new TransferProgressImpl(resource, TransferEvent.REQUEST_PUT, transferListenerContainer);

        try {
            azureStorageRepository.put(file, resourceName,transferProgress);
            transferListenerContainer.fireTransferCompleted(resource, TransferEvent.REQUEST_PUT);
        } catch (TransferFailedException e) {
            transferListenerContainer.fireTransferError(resource,TransferEvent.REQUEST_PUT,e);
            throw e;
        }
    }

    @Override
    public void putDirectory(File source, String destination) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        File[] files = source.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    putDirectory(f, destination + "/" + f.getName());
                } else {
                    put(f, destination + "/" + f.getName());
                }
            }
        }
    }

    @Override
    public boolean resourceExists(String resourceName) throws TransferFailedException, AuthorizationException {
        try {
            return azureStorageRepository.exists(resourceName);
        } catch (TransferFailedException e) {
            transferListenerContainer.fireTransferError(new Resource(resourceName), TransferEvent.REQUEST_GET, e);
            throw e;
        }
    }

    @Override
    public List<String> getFileList(String resourceName) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {

        try {
            return azureStorageRepository.list(resourceName);
        } catch (Exception e) {
            transferListenerContainer.fireTransferError(new Resource(resourceName),TransferEvent.REQUEST_GET, e);
            throw new TransferFailedException("Could not fetch resource");
        }
    }

    @Override
    public void connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfoProvider) throws ConnectionException, AuthenticationException {

        this.repository = repository;
        this.sessionListenerContainer.fireSessionOpening();

        try {

            final String account = accountResolver.resolve(repository);
            final String container = containerResolver.resolve(repository);

            LOGGER.log(Level.FINER,String.format("Opening connection for account %s and container %s",account,container));

            azureStorageRepository = new AzureStorageRepository(container);
            azureStorageRepository.connect(authenticationInfo);
            sessionListenerContainer.fireSessionLoggedIn();
            sessionListenerContainer.fireSessionOpened();
        } catch (Exception e) {
            this.sessionListenerContainer.fireSessionConnectionRefused();
            throw e;
        }
    }

    @Override
    public void disconnect() throws ConnectionException {
        sessionListenerContainer.fireSessionDisconnecting();
        azureStorageRepository.disconnect();
        sessionListenerContainer.fireSessionLoggedOff();
        sessionListenerContainer.fireSessionDisconnected();
    }

}
