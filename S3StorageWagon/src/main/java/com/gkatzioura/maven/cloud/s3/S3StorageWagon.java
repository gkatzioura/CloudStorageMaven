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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.gkatzioura.maven.cloud.resolver.KeyResolver;
import org.apache.commons.io.FileUtils;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.PathUtils;
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
import com.gkatzioura.maven.cloud.wagon.PublicReadProperty;

public class S3StorageWagon extends AbstractStorageWagon {

    private S3StorageRepository s3StorageRepository;
    private final KeyResolver keyResolver = new KeyResolver();

    private String region;
    private Boolean publicRepository;

    private static final Logger LOGGER = Logger.getLogger(S3StorageWagon.class.getName());
    private String endpoint;
    private String pathStyleEnabled;

    @Override
    public void get(String resourceName, File file) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {

        Resource resource = new Resource(resourceName);
        transferListenerContainer.fireTransferInitiated(resource, TransferEvent.REQUEST_GET);
        transferListenerContainer.fireTransferStarted(resource, TransferEvent.REQUEST_GET, file);

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
        transferListenerContainer.fireTransferStarted(resource,TransferEvent.REQUEST_PUT, file);
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
        Collection<File> allFiles = FileUtils.listFiles(source, null, true);
        String relativeDestination = destination;
        //removes the initial .
        if (destination != null && destination.startsWith(".")){
            relativeDestination = destination.length() == 1 ? "" : destination.substring(1);
        }
        for (File file : allFiles) {
            //compute relative path
            String relativePath = PathUtils.toRelative(source, file.getAbsolutePath());
            put(file, relativeDestination +"/"+relativePath);
        }
    }

    @Override
    public boolean resourceExists(String resourceName) throws TransferFailedException, AuthorizationException {
        return s3StorageRepository.exists(resourceName);
    }

    @Override
    public List<String> getFileList(String s) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        try {
            List<String> list = s3StorageRepository.list(s);
            list = convertS3ListToMavenFileList(list, s);
            if (list.isEmpty()){
                throw new ResourceDoesNotExistException(s);//expected by maven
            }
            return list;
        } catch (AmazonS3Exception e) {
            throw new TransferFailedException("Could not fetch objects for prefix "+s);
        }
    }

    //removes the prefix path
    //adds folders files
    private List<String> convertS3ListToMavenFileList(List<String> list, String path) {
        String prefix = keyResolver.resolve( s3StorageRepository.getBaseDirectory(), path);
        Set<String> folders = new HashSet<>();
        List<String> result = list.stream().map( key -> {
            String filePath = key;
            //removes the prefix from the object path
            if (prefix != null && prefix.length() > 0) {
                filePath = key.substring(prefix.length() + 1);
            }
            extractFolders(folders, filePath);
            return filePath;
        }).collect(Collectors.toList());
        result.addAll(folders);
        return result;
    }

    private void extractFolders(Set<String> folders, String filePath) {
        if (filePath.contains("/")){
            String folder = filePath.substring(0, filePath.lastIndexOf('/'));
            folders.add(folder +'/');
            if (folder.contains("/")) {//recurse
                extractFolders(folders, folder);
            }//else we already stored it.
        }else{
            folders.add(filePath);
        }
    }

    @Override
    public void connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfoProvider) throws ConnectionException, AuthenticationException {

        this.repository = repository;
        this.sessionListenerContainer.fireSessionOpening();

        final String bucket = accountResolver.resolve(repository);
        final String directory = containerResolver.resolve(repository);

        LOGGER.log(Level.FINER,String.format("Opening connection for bucket %s and directory %s",bucket,directory));
        s3StorageRepository = new S3StorageRepository(bucket, directory, new PublicReadProperty(publicRepository));
        s3StorageRepository.connect(authenticationInfo, region, new EndpointProperty(endpoint), new PathStyleEnabledProperty(pathStyleEnabled));

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

    public Boolean getPublicRepository() {
        return publicRepository;
    }

    public void setPublicRepository(Boolean publicRepository) {
        this.publicRepository = publicRepository;
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
