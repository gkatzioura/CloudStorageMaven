package com.gkatzioura.maven.cloud.s3;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonConstants;
import org.apache.maven.wagon.WagonTestCase;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/***
 * this class will launch 14 unit test from the apache wagon provider tester
 * */
public class S3StorageWagonTest extends WagonTestCase {


    @Override
    protected AuthenticationInfo getAuthInfo() {
        return null;//this will make the AWS sdk use it's credentials provider chain
    }

    @Override
    protected String getTestRepositoryUrl() throws IOException {
        return "s3://"+getTestBucket()+"/foo";
    }

    @Override
    protected String getProtocol() {
        return "s3";
    }

    @Override
    protected int getTestRepositoryPort() {
        return 0;
    }

    //TODO se tit to true and fix the failing tests
    protected boolean supportsGetIfNewer()
    {
        return false;
    }

    protected long getExpectedLastModifiedOnGet(Repository repository, Resource resource )
    {
        return 0;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //creates the bucket
        S3StorageRepository s3StorageRepository = new S3StorageRepository(getTestBucket(), "");
        s3StorageRepository.connect(getAuthInfo(), new RegionProperty(null), new EndpointProperty(null), new PathStyleEnabledProperty(null));
        try {
            s3StorageRepository.createBucket();
        }finally {
            s3StorageRepository.disconnect();
        }
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        //empty the bucket
        S3StorageRepository s3StorageRepository = new S3StorageRepository(getTestBucket(), "");
        s3StorageRepository.connect(getAuthInfo(), new RegionProperty(null), new EndpointProperty(null), new PathStyleEnabledProperty(null));
        try {
            s3StorageRepository.emptyBucket();
        }finally {
            s3StorageRepository.disconnect();
        }
    }

    private String getTestBucket() {
        return System.getProperty("s3.test.bucket.name", "s3wagontestbucket");
    }


}
