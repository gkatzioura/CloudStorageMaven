package com.gkatzioura.maven.cloud.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.gkatzioura.maven.cloud.s3.utils.S3Connect;
import org.apache.maven.wagon.WagonTestCase;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;

import java.io.IOException;
import java.util.Iterator;

/***
 * this class will launch 14 unit test from the apache wagon provider tester
 * */
public class S3StorageWagonTest extends WagonTestCase {

    AmazonS3 amazonS3;

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
        amazonS3 = S3Connect.connect(getAuthInfo(), null, new EndpointProperty(null), new PathStyleEnabledProperty(null));

        createBucket();
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        emptyBucket();
    }

    private String getTestBucket() {
        return System.getProperty("s3.test.bucket.name", "s3wagontestbucket");
    }



    /**
     * creates the bucket, this is only used for testing purposes hence it is package protected
     */
    void createBucket(){
        if (!bucketExists(getTestBucket())) {
            amazonS3.createBucket(getTestBucket());
        }
    }

    boolean bucketExists(String bucket){
        try {
            amazonS3.getObjectMetadata(bucket, "");
            return true;
        } catch (AmazonS3Exception e) {
            return false;
        }
    }


    /**
     * delete the bucket, this is only used for testing hence it is package protected
     */
    void deleteBucket() {
        emptyBucket();
        amazonS3.deleteBucket(getTestBucket());
    }
    /**
     * delete the bucket, this is only used for testing hence it is package protected
     */
    void emptyBucket() {
        ObjectListing objectListing = amazonS3.listObjects(getTestBucket());
        while (true) {
            Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
            while (objIter.hasNext()) {
                amazonS3.deleteObject(getTestBucket(), objIter.next().getKey());
            }

            // If the bucket contains many objects, the listObjects() call
            // might not return all of the objects in the first listing. Check to
            // see whether the listing was truncated. If so, retrieve the next page of objects
            // and delete them.
            if (objectListing.isTruncated()) {
                objectListing = amazonS3.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }
    }


}
