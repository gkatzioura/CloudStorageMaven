package com.gkatzioura.maven.cloud.s3;

import com.amazonaws.regions.AwsEnvVarOverrideRegionProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.amazonaws.SDKGlobalConfiguration.AWS_REGION_ENV_VAR;
import static com.amazonaws.SDKGlobalConfiguration.AWS_REGION_SYSTEM_PROPERTY;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
public class S3StorageRegionProviderChainTest {

    private final static String PROVIDED_REGION = "provided-region";
    private final static String ENV_VAR_REGION = "env-var-region";
    private final static String SYSTEM_PROPERTY_REGION = "sys-prop-region";

    @Test
    public void testProvidedRegionConstructor() {
        final S3StorageRegionProviderChain regionProvider = new S3StorageRegionProviderChain(PROVIDED_REGION);
        Assert.assertEquals(PROVIDED_REGION, regionProvider.getRegion());
    }

    @Test
    @PrepareForTest(AwsEnvVarOverrideRegionProvider.class)
    public void testEnvVarRegion() {
        mockStatic(System.class);
        when(System.getenv(AWS_REGION_ENV_VAR)).thenReturn(ENV_VAR_REGION);

        final S3StorageRegionProviderChain regionProvider = new S3StorageRegionProviderChain();
        Assert.assertEquals(ENV_VAR_REGION, regionProvider.getRegion());
    }

    @Test
    @PrepareForTest(AwsDefaultEnvRegionProvider.class)
    public void testDefaultEnvVarRegion() {
        mockStatic(System.class);
        when(System.getenv("AWS_DEFAULT_REGION")).thenReturn(ENV_VAR_REGION);

        final S3StorageRegionProviderChain regionProvider = new S3StorageRegionProviderChain();
        Assert.assertEquals(ENV_VAR_REGION, regionProvider.getRegion());
    }

    @Test
    public void testSystemPropertyRegion() {
        System.setProperty(AWS_REGION_SYSTEM_PROPERTY, SYSTEM_PROPERTY_REGION);

        final S3StorageRegionProviderChain regionProvider = new S3StorageRegionProviderChain();
        Assert.assertEquals(SYSTEM_PROPERTY_REGION, regionProvider.getRegion());

        System.clearProperty(AWS_REGION_SYSTEM_PROPERTY);
    }

}
