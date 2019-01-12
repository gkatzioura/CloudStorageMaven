package com.gkatzioura.maven.cloud.s3.plugin.download;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

public class S3DownloadMojoTest {

    @Test
    public void testDownloadBucket() throws Exception {

        List<String> files = new ArrayList<>();
        files.add("a-key-to-download.txt");
        files.add("25226_big.jpg");
        files.add("738ypD45_400x400.jpg");

        S3DownloadMojo s3DownloadMojo = new S3DownloadMojo("gkatzfiledownloads",files,"/Users/gkatzioura/Documents/s3filedownload/downloads");
        s3DownloadMojo.execute();
    }

    @Test
    public void testDownloadPrefix() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("prefix");

        S3DownloadMojo s3DownloadMojo = new S3DownloadMojo("gkatzfiledownloads",files,"/Users/gkatzioura/Documents/s3filedownload/downloads/onprefix");
        s3DownloadMojo.execute();
    }

}
