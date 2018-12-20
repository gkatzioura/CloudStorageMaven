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

import java.io.File;
import java.util.List;
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

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.gkatzioura.maven.cloud.transfer.TransferProgress;
import com.gkatzioura.maven.cloud.transfer.TransferProgressImpl;
import com.gkatzioura.maven.cloud.wagon.AbstractStorageWagon;

public class S3StorageWagon extends AbstractStorageWagon {

    private S3StorageRepository s3StorageRepository;
    
    private String region;

    private static final Logger LOGGER = Logger.getLogger(S3StorageWagon.class.getName());
    private String endpoint;
    private String pathStyleEnabled;

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

        LOGGER.log(Level.FINER, String.format("Uploading file %s to %s", file.getAbsolutePath(), resourceName));

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

        LOGGER.log(Level.FINER,String.format("Opening connection for bucket %s and directory %s",bucket,directory));
        s3StorageRepository = new S3StorageRepository(bucket,directory);
        s3StorageRepository.connect(authenticationInfo, new RegionProperty(region), new EndpointProperty(endpoint), new PathStyleEnabledProperty(pathStyleEnabled));

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

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getPathStyleAccessEnabled() {
        return pathStyleEnabled;
    }

    public void setPathStyleAccessEnabled(String pathStyleEnabled) {
        this.pathStyleEnabled = pathStyleEnabled;
    }

}
