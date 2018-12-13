package com.gkatzioura.maven.cloud.s3.plugin.download;

import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "s3-upload")
public class S3UploadMojo extends AbstractMojo {

    @Parameter( property = "s3-upload.bucket")
    private String bucket;

    @Parameter(property="s3-upload.keys")
    private List<String> keys;

    public S3UploadMojo() {
    }

    public S3UploadMojo(String bucket, List<String> keys) {
        this.bucket = bucket;
        this.keys = keys;
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
    }

}
