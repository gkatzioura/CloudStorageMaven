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

package com.gkatzioura.maven.cloud.s3.plugin.download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Mojo(name = "s3-upload")
public class S3UploadMojo extends AbstractMojo {

    @Parameter( property = "s3-upload.bucket")
    private String bucket;

    @Parameter(property = "s3-upload.path")
    private String path;

    @Parameter(property = "s3-upload.key")
    private String key;

    public S3UploadMojo() {
    }

    /**
     * If the path is a file then a file shall be uploaded. If the file specified is a directory
     * then the directory shall be uploaded using prefix and the rest files shall be uploaded recursively
     * @param bucket
     * @param path
     * @param key
     */
    public S3UploadMojo(String bucket, String path, String key) {
        this.bucket = bucket;
        this.path = path;
        this.key = key;
    }

    /**
     * At least the bucket should be null or else everything else shall be fetched
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(bucket == null) {
            throw new MojoExecutionException("You need to specify a bucket");
        }

        AmazonS3 amazonS3 = AmazonS3ClientBuilder.defaultClient();

        if(isDirectory()){
            List<String> filesToUpload = findFilesToUpload(path);

            for(String fileToUpload: filesToUpload) {
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucket,generateKeyName(fileToUpload),new File(fileToUpload));
                amazonS3.putObject(putObjectRequest);
            }
        } else {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket,keyIfNull(),new File(path));
            amazonS3.putObject(putObjectRequest);
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
