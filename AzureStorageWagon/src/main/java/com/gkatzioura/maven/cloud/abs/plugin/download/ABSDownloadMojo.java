package com.gkatzioura.maven.cloud.abs.plugin.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.wagon.authentication.AuthenticationException;

import com.gkatzioura.maven.cloud.KeyIteratorConcated;
import com.gkatzioura.maven.cloud.abs.ConnectionStringFactory;
import com.gkatzioura.maven.cloud.abs.plugin.PrefixKeysIterator;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobInputStream;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;

@Mojo(name = "abs-download")
public class ABSDownloadMojo extends AbstractMojo {

    private CloudStorageAccount cloudStorageAccount;

    @Parameter(property = "abs-download.container")
    private String container;

    @Parameter(property = "abs-download.keys")
    private List<String> keys;

    @Parameter(property = "abs-download.downloadPath")
    private String downloadPath;

    private static final Logger LOGGER = Logger.getLogger(ABSDownloadMojo.class.getName());

    public ABSDownloadMojo(String container, List<String> keys, String downloadPath) throws AuthenticationException {
        this();
        this.container = container;
        this.keys = keys;
        this.downloadPath = downloadPath;
    }

    public ABSDownloadMojo() throws AuthenticationException {
        try {
            String connectionString = new ConnectionStringFactory().create();
            cloudStorageAccount = CloudStorageAccount.parse(connectionString);
        } catch (Exception e) {
            throw new AuthenticationException("Could not setup azure client",e);
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            CloudBlobContainer blobContainer = cloudStorageAccount.createCloudBlobClient().getContainerReference(container);
            blobContainer.getMetadata();

            if (keys.size()==1) {
                downloadSingleFile(blobContainer,keys.get(0));
                return;
            }

            List<Iterator<ListBlobItem>> prefixKeysIterators = keys.stream()
                                                                   .map(pi -> new PrefixKeysIterator(blobContainer, pi))
                                                                   .collect(Collectors.toList());
            Iterator<ListBlobItem> keyIteratorConcatenated = new KeyIteratorConcated<ListBlobItem>(prefixKeysIterators);

            while (keyIteratorConcatenated.hasNext()) {
                ListBlobItem key = keyIteratorConcatenated.next();
                downloadFile(blobContainer,key);
            }

        } catch (StorageException |URISyntaxException e) {
            throw new MojoFailureException("Could not get container "+container,e);
        }
    }

    private void downloadSingleFile(CloudBlobContainer cloudBlobContainer,String key) throws MojoExecutionException {
        File file = new File(downloadPath);

        if(file.getParentFile()!=null) {
            file.getParentFile().mkdirs();
        }

        try {
            CloudBlob cloudBlob = cloudBlobContainer.getBlobReferenceFromServer(key);

            if(!cloudBlob.exists()) {
                LOGGER.log(Level.FINER,"Blob {} does not exist", key);
                throw new MojoExecutionException("Could not find blob "+key);
            }

            try(BlobInputStream blobInputStream = cloudBlob.openInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(file)
            ) {
                IOUtils.copy(blobInputStream, fileOutputStream);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Could not download abs file");
                throw new MojoExecutionException("Could not download abs file "+key);
            }
        } catch (URISyntaxException| StorageException e) {
            throw new MojoExecutionException("Could not fetch abs file "+key,e);
        }
    }

    private void downloadFile(CloudBlobContainer cloudBlobContainer,ListBlobItem listBlobItem) throws MojoExecutionException {
        String key = listBlobItem.getUri().getPath().replace("/"+container+"/","");
        File file = new File(createFullFilePath(key));

        if(file.getParent()!=null) {
            file.getParentFile().mkdirs();
        }

        if(isDirectory(cloudBlobContainer, key)) {
            return;
        }

        final CloudBlob cloudBlob;

        try {
            cloudBlob = cloudBlobContainer.getBlobReferenceFromServer(key);
        } catch (URISyntaxException |StorageException e) {
            throw new MojoExecutionException("Could not fetch abs file "+key,e);
        }

        try(InputStream objectInputStream = cloudBlob.openInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(file)
        ) {
            IOUtils.copy(objectInputStream,fileOutputStream);
        } catch (IOException |StorageException e) {
            LOGGER.log(Level.SEVERE, "Could not download abs file");
            throw new MojoExecutionException("Could not download abs file "+key,e);
        }
    }

    private final String createFullFilePath(String key) {
        String fullPath = downloadPath+"/"+key;
        return fullPath;
    }

    private final boolean isDirectory(CloudBlobContainer container, String key) {
        try {
            return container.getDirectoryReference(key).listBlobs().iterator().hasNext();
        } catch (StorageException |URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Abs key is not a directory");
            return false;
        }
    }

}
