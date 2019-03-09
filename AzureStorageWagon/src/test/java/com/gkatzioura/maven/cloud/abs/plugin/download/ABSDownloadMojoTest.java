package com.gkatzioura.maven.cloud.abs.plugin.download;

import java.util.Arrays;

public class ABSDownloadMojoTest {

    public static void main(String[] args) throws Exception{
        ABSDownloadMojo absDownloadMojo = new ABSDownloadMojo("paparas", Arrays.asList("directory"),"/Users/gkatzioura/Documents/egkatzioura/pullrequests/test/pathtodow");
        absDownloadMojo.execute();
    }

}
