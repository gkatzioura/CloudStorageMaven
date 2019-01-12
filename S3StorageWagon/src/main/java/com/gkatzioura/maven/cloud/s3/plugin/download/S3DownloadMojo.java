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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

@Mojo(name = "s3-download")
public class S3DownloadMojo extends AbstractMojo {

    @Parameter( property = "s3-download.bucket")
    private String bucket;

    @Parameter(property = "s3-download.keys")
    private List<String> keys;

    @Parameter(property = "s3-download.downloadPath")
    private String downloadPath;

    private static final Logger LOGGER = Logger.getLogger(S3DownloadMojo.class.getName());

    public S3DownloadMojo() {
    }

    public S3DownloadMojo(String bucket, List<String> keys, String downloadPath) {
        this.bucket = bucket;
        this.keys = keys;
        this.downloadPath = downloadPath;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        AmazonS3 amazonS3 = AmazonS3ClientBuilder.defaultClient();

        List<Iterator<String>> prefixKeysIterators = keys.stream()
                                                 .map(pi -> new PrefixKeysIterator(amazonS3,bucket,pi))
                                                 .collect(Collectors.toList());
        Iterator<String> keyIteratorConcated = new KeyIteratorConcated(prefixKeysIterators);

        while (keyIteratorConcated.hasNext()) {

            String key = keyIteratorConcated.next();
            downloadFile(amazonS3,key);
        }
    }

    private void downloadFile(AmazonS3 amazonS3,String key) {

        File file = new File(createFullFilePath(key));

        if(file.getParent()!=null) {
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

    private final String createFullFilePath(String key) {

        String fullPath = downloadPath+"/"+key;
        return fullPath;
    }


}
