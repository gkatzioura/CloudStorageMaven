package com.gkatzioura.maven.cloud.s3.plugin.download;

import org.junit.Test;

public class S3DownloadMojoTest {

    @Test
    public void downloadDirectory() throws Exception {

        S3UploadMojo s3UploadMojo = new S3UploadMojo();
        s3UploadMojo.execute();
    }

}
