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

package com.gkatzioura.maven.cloud.gcs.wagon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import com.gkatzioura.maven.cloud.transfer.TransferProgressFileInputStream;
import com.gkatzioura.maven.cloud.transfer.TransferProgressImpl;
import com.gkatzioura.maven.cloud.wagon.AbstractStorageWagon;
import com.gkatzioura.maven.cloud.wagon.PublicReadProperty;

public class GoogleStorageWagon extends AbstractStorageWagon {

    private GoogleStorageRepository googleStorageRepository;
    private Optional<String> keyPath;
    private Boolean publicRepository;

    private static final Logger LOGGER = Logger.getLogger(GoogleStorageWagon.class.getName());

    @Override
    public void get(String resourceName, File destination) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {

        Resource resource = new Resource(resourceName);
        transferListenerContainer.fireTransferInitiated(resource, TransferEvent.REQUEST_GET);
        transferListenerContainer.fireTransferStarted(resource, TransferEvent.REQUEST_GET, destination);

        try {
            googleStorageRepository.copy(resourceName, destination);
            transferListenerContainer.fireTransferCompleted(resource,TransferEvent.REQUEST_GET);
        } catch (Exception e) {
            transferListenerContainer.fireTransferError(resource,TransferEvent.REQUEST_GET,e);
            throw e;
        }
    }

    @Override
    public boolean getIfNewer(String s, File file, long l) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        if(googleStorageRepository.newResourceAvailable(s, l)) {
            get(s,file);
            return true;
        }

        return false;
    }

    @Override
    public void put(File file, String resourceName) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {

        Resource resource = new Resource(resourceName);

        LOGGER.log(Level.FINER, String.format("Uploading file %s to %s", file.getAbsolutePath(), resourceName));

        transferListenerContainer.fireTransferInitiated(resource,TransferEvent.REQUEST_PUT);
        transferListenerContainer.fireTransferStarted(resource,TransferEvent.REQUEST_PUT, file);
        final TransferProgress transferProgress = new TransferProgressImpl(resource, TransferEvent.REQUEST_PUT, transferListenerContainer);

        try(InputStream inputStream = new TransferProgressFileInputStream(file, transferProgress)) {
            googleStorageRepository.put(inputStream, resourceName);
            transferListenerContainer.fireTransferCompleted(resource,TransferEvent.REQUEST_PUT);
        } catch (FileNotFoundException e) {
            transferListenerContainer.fireTransferError(resource,TransferEvent.REQUEST_PUT,e);
            throw new ResourceDoesNotExistException("Faild to transfer artifact",e);
        } catch (IOException e) {
            transferListenerContainer.fireTransferError(resource,TransferEvent.REQUEST_PUT,e);
            throw new TransferFailedException("Faild to transfer artifact",e);
        }
    }

    @Override
    public void putDirectory(File source, String destination) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        File[] files = source.listFiles();
        if (files != null) {
            for (File f : files) {
                put(f, destination + "/" + f.getName());
            }
        }
    }

    @Override
    public boolean resourceExists(String resourceName) throws TransferFailedException, AuthorizationException {

        return googleStorageRepository.exists(resourceName);
    }

    @Override
    public List<String> getFileList(String resourceName) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        try {
            return googleStorageRepository.list(resourceName);
        } catch (Exception e) {
            transferListenerContainer.fireTransferError(new Resource(resourceName),TransferEvent.REQUEST_GET, e);
            throw new TransferFailedException("Could not fetch resource");
        }
    }

    @Override
    public void connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfoProvider) throws AuthenticationException {
        this.repository = repository;
        this.sessionListenerContainer.fireSessionOpening();
        try {
            final String bucket = accountResolver.resolve(repository);
            final String directory = containerResolver.resolve(repository);

            LOGGER.log(Level.FINER,String.format("Opening connection for bucket %s and directory %s",bucket,directory));

            googleStorageRepository = new GoogleStorageRepository(keyPath ,bucket, directory, new PublicReadProperty(publicRepository));
            googleStorageRepository.connect();
            sessionListenerContainer.fireSessionLoggedIn();
            sessionListenerContainer.fireSessionOpened();
        } catch (AuthenticationException e) {
            this.sessionListenerContainer.fireSessionConnectionRefused();
            throw e;
        }
    }

    @Override
    public void disconnect() throws ConnectionException {
        sessionListenerContainer.fireSessionDisconnecting();
        googleStorageRepository.disconnect();
        sessionListenerContainer.fireSessionLoggedOff();
        sessionListenerContainer.fireSessionDisconnected();
    }

    public String getKeyPath() {
        return keyPath.get();
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = Optional.of(keyPath);
    }

    public Boolean getPublicRepository() {
        return publicRepository;
    }

    public void setPublicRepository(Boolean publicRepository) {
        this.publicRepository = publicRepository;
    }

}
