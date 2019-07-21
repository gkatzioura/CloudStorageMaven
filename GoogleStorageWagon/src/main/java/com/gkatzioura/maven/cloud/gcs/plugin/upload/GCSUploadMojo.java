package com.gkatzioura.maven.cloud.gcs.plugin.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.gkatzioura.maven.cloud.gcs.StorageFactory;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Mojo(name = "gcs-upload")
public class GCSUploadMojo extends AbstractMojo {

    @Parameter( property = "gcs-upload.bucket")
    private String bucket;

    @Parameter(property = "gcs-upload.path")
    private String path;

    @Parameter(property = "gcs-upload.key")
    private String key;

    @Parameter(property = "gcs-upload.keyPath")
    private String keyPath;

    private final StorageFactory storageFactory = new StorageFactory();

    public GCSUploadMojo() {
    }

    /**
     * If the path is a file then a file shall be uploaded. If the file specified is a directory
     * then the directory shall be uploaded using prefix and the rest files shall be uploaded recursively
     * @param bucket
     * @param path
     * @param key
     */
    public GCSUploadMojo(String bucket, String path, String key) {
        this.bucket = bucket;
        this.path = path;
        this.key = key;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(bucket == null) {
            throw new MojoExecutionException("You need to specify a bucket");
        }

        Storage storage = initializeStorage();

        if(isDirectory()){
            List<String> filesToUpload = findFilesToUpload(path);

            for(String fileToUpload: filesToUpload) {
                keyUpload(storage, generateKeyName(fileToUpload), new File(fileToUpload));
            }
        } else {
            keyUpload(storage, keyIfNull(), new File(path));
        }
    }

    private Storage initializeStorage() throws MojoExecutionException {
        if(keyPath==null) {
            return storageFactory.createDefault();
        } else {
            try {
                return storageFactory.createWithKeyFile(keyPath);
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to set Authentication to Google Cloud");
            }
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

    private void keyUpload(Storage storage, String keyName, File file) throws MojoExecutionException {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucket, keyName).build();

        try (InputStream inputStream = new FileInputStream(file)) {
            storage.create(blobInfo,IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to upload mojo",e);
        }
    }

    private boolean isDirectory() {
        return new File(path).isDirectory();
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


    private String keyIfNull() {
        if(key==null) {
            return new File(path).getName();
        } else {
            return key;
        }
    }

}
