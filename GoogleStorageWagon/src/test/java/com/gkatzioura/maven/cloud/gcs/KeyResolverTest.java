package com.gkatzioura.maven.cloud.gcs;

import org.junit.Assert;
import org.junit.Test;

import com.gkatzioura.maven.cloud.resolver.KeyResolver;

public class KeyResolverTest {

    @Test
    public void testResolveSimple() {

        KeyResolver keyResolver1 = new KeyResolver();
        String directoryJoin = keyResolver1.resolve("/tesanother/key/");
        Assert.assertEquals("tesanother/key", directoryJoin);
    }

    @Test
    public void testResolveConcat() {

        KeyResolver keyResolver1 = new KeyResolver();
        String directoryJoin = keyResolver1.resolve("test-repo/release/production/", "/tesanother/key");
        Assert.assertEquals("test-repo/release/production/tesanother/key", directoryJoin);
    }

}
