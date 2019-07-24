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
import java.io.FileOutputStream;
import java.io.IOException;
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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.gkatzioura.maven.cloud.KeyIteratorConcated;
import com.gkatzioura.maven.cloud.s3.EndpointProperty;
import com.gkatzioura.maven.cloud.s3.PathStyleEnabledProperty;
import com.gkatzioura.maven.cloud.s3.plugin.PrefixKeysIterator;
import com.gkatzioura.maven.cloud.s3.utils.S3Connect;

@Mojo(name = "s3-download")
public class S3DownloadMojo extends AbstractMojo {

    @Parameter( property = "s3-download.bucket")
    private String bucket;

    @Parameter(property = "s3-download.keys")
    private List<String> keys;

    @Parameter(property = "s3-download.downloadPath")
    private String downloadPath;

    @Parameter(property = "s3-download.region")
    private String region;

    private static final String DIRECTORY_CONTENT_TYPE = "application/x-directory";

    private static final Logger LOGGER = Logger.getLogger(S3DownloadMojo.class.getName());

    public S3DownloadMojo() {
    }

    public S3DownloadMojo(String bucket, List<String> keys, String downloadPath, String region) {
        this.bucket = bucket;
        this.keys = keys;
        this.downloadPath = downloadPath;
        this.region = region;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        AmazonS3 amazonS3;

        try {
            //Sending the authenticationInfo as null will make this use the default S3 authentication, which will only
            //look at the environment Java properties or environment variables
            amazonS3 = S3Connect.connect(null, region, EndpointProperty.empty(), new PathStyleEnabledProperty(String.valueOf(S3ClientOptions.DEFAULT_PATH_STYLE_ACCESS)));
        } catch (AuthenticationException e) {
            throw new MojoExecutionException(
                    String.format("Unable to authenticate to S3 with the available credentials. Make sure to either define the environment variables or System properties defined in https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html.%n" +
                            "Detail: %s", e.getMessage()),
                    e);
        }

        if (keys.size()==1) {
            downloadSingleFile(amazonS3,keys.get(0));
            return;
        }

        List<Iterator<String>> prefixKeysIterators = keys.stream()
                                                 .map(pi -> new PrefixKeysIterator(amazonS3, bucket, pi))
                                                 .collect(Collectors.toList());
        Iterator<String> keyIteratorConcated = new KeyIteratorConcated(prefixKeysIterators);

        while (keyIteratorConcated.hasNext()) {

            String key = keyIteratorConcated.next();
            downloadFile(amazonS3,key);
        }
    }

    private void downloadSingleFile(AmazonS3 amazonS3,String key) {
        File file = new File(downloadPath);

        if(file.getParentFile()!=null) {
            file.getParentFile().mkdirs();
        }

        S3Object s3Object = amazonS3.getObject(bucket, key);

        try(S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
            FileOutputStream fileOutputStream = new FileOutputStream(file)
        ) {
            IOUtils.copy(s3ObjectInputStream,fileOutputStream);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not download s3 file");
            e.printStackTrace();
        }
    }

    private void downloadFile(AmazonS3 amazonS3,String key) {

        File file = new File(createFullFilePath(key));

        if(file.getParent()!=null) {
            file.getParentFile().mkdirs();
        }

        S3Object s3Object = amazonS3.getObject(bucket, key);

        if(isDirectory(s3Object)) {
            return;
        }

        try(S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
            FileOutputStream fileOutputStream = new FileOutputStream(file)
        ) {
            IOUtils.copy(s3ObjectInputStream,fileOutputStream);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not download s3 file");
            e.printStackTrace();
        }
    }

    private final String createFullFilePath(String key) {

        String fullPath = downloadPath+"/"+key;
        return fullPath;
    }

    private final boolean isDirectory(S3Object s3Object) {
        return s3Object.getObjectMetadata().getContentType().equals(DIRECTORY_CONTENT_TYPE);
    }


}
