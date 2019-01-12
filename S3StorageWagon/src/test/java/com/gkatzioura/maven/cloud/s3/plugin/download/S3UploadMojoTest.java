package com.gkatzioura.maven.cloud.s3.plugin.download;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

public class S3UploadMojoTest {

    //@Test
    public void testDownloadBucket() throws Exception {
        S3DownloadMojo s3DownloadMojo = new S3DownloadMojo();
        s3DownloadMojo.execute();
    }

}
