package com.gkatzioura.maven.cloud.gcs;

import org.apache.maven.wagon.repository.Repository;
import org.junit.Assert;
import org.junit.Test;

public class BaseDirectoryResolverTest {

    @Test
    public void testResolve() {

        BaseDirectoryResolver directoryResolver = new BaseDirectoryResolver();
        Repository repository = new Repository("test-repo","gs://test-repo/release");
        String baseDirectory = directoryResolver.resolve(repository);
        Assert.assertEquals("release/",baseDirectory);
    }

    @Test
    public void testResolveSubDirectory() {

        BaseDirectoryResolver directoryResolver = new BaseDirectoryResolver();
        Repository repository = new Repository("test-repo","gs://test-repo/release/production");
        String baseDirectory = directoryResolver.resolve(repository);
        Assert.assertEquals("release/production/",baseDirectory);
    }

}
