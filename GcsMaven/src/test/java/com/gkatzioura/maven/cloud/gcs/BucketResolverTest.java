package com.gkatzioura.maven.cloud.gcs;

import org.apache.maven.wagon.repository.Repository;
import org.junit.Assert;
import org.junit.Test;

public class BucketResolverTest {

    @Test
    public void testResolve() {

        BucketResolver bucketResolver = new BucketResolver();
        Repository repository = new Repository("test-repo","gs://test-repo/release");
        String bucketName = bucketResolver.resolve(repository);
        Assert.assertEquals("test-repo",bucketName);
    }

}
