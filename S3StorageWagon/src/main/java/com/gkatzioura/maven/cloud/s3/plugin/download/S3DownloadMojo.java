package com.gkatzioura.maven.cloud.s3.plugin.download;

import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.gkatzioura.maven.cloud.s3.S3StorageRepository;

@Mojo(name = "s3-download")
public class S3DownloadMojo extends AbstractMojo {

    @Parameter( property = "s3-download.bucket")
    private String bucket;

    @Parameter(property = "s3-download.keys")
    private List<String> keys;

    @Parameter(property = "s3-download.downloadPath")
    private String downloadPath;

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

        /*
        S3StorageRepository s3StorageRepository = new S3StorageRepository(bucket);

        for(String keyPrefix: keys) {
            List<String> keys = s3StorageRepository.list(keyPrefix);

            for(String key: keys) {
                downloadFile(key);
            }
        }
        */
    }

    private void downloadFile(String key) {
        /*
        S3StorageRepository s3StorageRepository = new S3StorageRepository(bucket);
        String fullFilePath = createFullFilePath(key);

        s3StorageRepository.copy();
        */
    }

    private final String createFullFilePath(String key) {

        String fullPath = downloadPath+"/"+key;
        return fullPath;
    }


}
