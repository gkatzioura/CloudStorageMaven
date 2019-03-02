package com.gkatzioura.maven.cloud.abs.plugin.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.wagon.authentication.AuthenticationException;

import com.gkatzioura.maven.cloud.abs.ConnectionStringFactory;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import static com.gkatzioura.maven.cloud.abs.ContentTypeResolver.getContentType;

@Mojo(name = "abs-upload")
public class ABSUploadMojo extends AbstractMojo {

    private CloudStorageAccount cloudStorageAccount;

    @Parameter(property = "abs-upload.container")
    private String container;

    @Parameter(property = "abs-upload.path")
    private String path;

    @Parameter(property = "abs-upload.key")
    private String key;

    public ABSUploadMojo() throws AuthenticationException {
        try {
            String connectionString = new ConnectionStringFactory().create();
            cloudStorageAccount = CloudStorageAccount.parse(connectionString);
        } catch (Exception e) {
            throw new AuthenticationException("Could not setup azure client", e);
        }
    }

    public ABSUploadMojo(String container, String path, String key) throws AuthenticationException {
        this();
        this.container = container;
        this.path = path;
        this.key = key;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            CloudBlobContainer blobContainer = cloudStorageAccount.createCloudBlobClient().getContainerReference(container);
            blobContainer.getMetadata();

            if(isDirectory()) {
                List<String> filesToUpload = findFilesToUpload(path);

                for(String fileToUpload: filesToUpload) {
                    String generateKeyName = generateKeyName(fileToUpload);
                    uploadFileToStorage(blobContainer, generateKeyName, new File(fileToUpload));
                }
            } else {
                uploadFileToStorage(blobContainer, keyIfNull(), new File(path));
            }

        } catch (StorageException |URISyntaxException e) {
            throw new MojoFailureException("Could not get container "+container,e);
        }
    }

    private List<String> findFilesToUpload(String filePath) {
        List<String> totalFiles = new ArrayList<>();

        File file = new File(filePath);

        if(file.isDirectory()) {
            File[] files = file.listFiles();

            for(File lFile: files) {
                if(lFile.isDirectory()) {
                    List<String> filesFound = findFilesToUpload(lFile.getAbsolutePath());
                    totalFiles.addAll(filesFound);
                } else {
                    totalFiles.add(lFile.getAbsolutePath());
                }
            }

        } else {
            totalFiles.add(file.getAbsolutePath());
        }

        return totalFiles;
    }

    private String generateKeyName(String fullFilePath) {
        StringBuilder keyNameBuilder = new StringBuilder();

        String absolutePath = new File(path).getAbsolutePath();

        if(key!=null) {
            keyNameBuilder.append(key);
            if(!fullFilePath.startsWith("/")) {
                keyNameBuilder.append("/");
            }
            keyNameBuilder.append(fullFilePath.replace(absolutePath,""));
        } else {
            final String clearFilePath = fullFilePath.replace(absolutePath,"");
            final String filePathToAppend = clearFilePath.startsWith("/")? clearFilePath.replaceFirst("/",""):clearFilePath;
            keyNameBuilder.append(filePathToAppend);
        }
        return keyNameBuilder.toString();
    }

    private void uploadFileToStorage(CloudBlobContainer blobContainer, String key, File file) throws MojoExecutionException {
        try {
            CloudBlockBlob blob = blobContainer.getBlockBlobReference(key);
            blob.getProperties().setContentType(getContentType(file));

            try (InputStream inputStream = new FileInputStream(file)) {
                blob.upload(inputStream, -1);
            }
        } catch (URISyntaxException| IOException | StorageException e) {
            throw new MojoExecutionException("Could not upload file "+file.getName(),e);
        }
    }

    private boolean isDirectory() {
        return new File(path).isDirectory();
    }

    private String keyIfNull() {
        if(key==null) {
            return new File(path).getName();
        } else {
            return key;
        }
    }

}
