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

import java.io.File;
import java.util.List;

import io.github.ruschecker.transfer.TransferProgress;
import io.github.ruschecker.transfer.TransferProgressImpl;
import io.github.ruschecker.wagon.AbstractStorageWagon;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.model.AmazonS3Exception;

public class S3StorageWagon extends AbstractStorageWagon {

    private S3StorageRepository s3StorageRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(S3StorageWagon.class);

    @Override
    public void get(String resourceName, File file) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {

        Resource resource = new Resource(resourceName);
        transferListenerContainer.fireTransferInitiated(resource, TransferEvent.REQUEST_GET);
        transferListenerContainer.fireTransferStarted(resource, TransferEvent.REQUEST_GET);

        final TransferProgress transferProgress = new TransferProgressImpl(resource, TransferEvent.REQUEST_GET, transferListenerContainer);

        try {
            s3StorageRepository.copy(resourceName,file,transferProgress);
            transferListenerContainer.fireTransferCompleted(resource,TransferEvent.REQUEST_GET);
        } catch (Exception e) {
            transferListenerContainer.fireTransferError(resource,TransferEvent.REQUEST_GET,e);
            throw e;
        }
    }

    @Override
    public void put(File file, String resourceName) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {

        Resource resource = new Resource(resourceName);

        LOGGER.debug("Uploading file {} to {}",file.getAbsolutePath(),resourceName);

        transferListenerContainer.fireTransferInitiated(resource,TransferEvent.REQUEST_PUT);
        transferListenerContainer.fireTransferStarted(resource,TransferEvent.REQUEST_PUT);
        final TransferProgress transferProgress = new TransferProgressImpl(resource, TransferEvent.REQUEST_PUT, transferListenerContainer);

        try {
            s3StorageRepository.put(file, resourceName,transferProgress);
            transferListenerContainer.fireTransferCompleted(resource, TransferEvent.REQUEST_PUT);
        } catch (TransferFailedException e) {
            transferListenerContainer.fireTransferError(resource,TransferEvent.REQUEST_PUT,e);
            throw e;
        }
    }

    @Override
    public boolean getIfNewer(String resourceName, File file, long timeStamp) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {

        if(s3StorageRepository.newResourceAvailable(resourceName,timeStamp)) {
            get(resourceName,file);
            return true;
        }

        return false;
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
        return s3StorageRepository.exists(resourceName);
    }

    @Override
    public List<String> getFileList(String s) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        try {
            return s3StorageRepository.list(s);
        } catch (AmazonS3Exception e) {
            throw new TransferFailedException("Could not fetch objects for prefix "+s);
        }
    }

    @Override
    public void connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfoProvider) throws ConnectionException, AuthenticationException {

        this.repository = repository;
        this.sessionListenerContainer.fireSessionOpening();

        final String bucket = accountResolver.resolve(repository);
        final String directory = containerResolver.resolve(repository);

        LOGGER.debug("Opening connection for bucket {} and directory {}",bucket,directory);

        s3StorageRepository = new S3StorageRepository(bucket,directory);
        s3StorageRepository.connect(authenticationInfo);

        sessionListenerContainer.fireSessionLoggedIn();
        sessionListenerContainer.fireSessionOpened();
    }

    @Override
    public void disconnect() throws ConnectionException {
        sessionListenerContainer.fireSessionDisconnecting();
        s3StorageRepository.disconnect();
        sessionListenerContainer.fireSessionLoggedOff();
        sessionListenerContainer.fireSessionDisconnected();
    }

}
