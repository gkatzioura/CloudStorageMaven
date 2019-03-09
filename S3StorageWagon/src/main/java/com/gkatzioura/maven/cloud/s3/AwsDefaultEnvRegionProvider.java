package com.gkatzioura.maven.cloud.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.regions.AwsRegionProvider;

public class AwsDefaultEnvRegionProvider extends AwsRegionProvider {

    public AwsDefaultEnvRegionProvider() {
    }

    @Override
    public String getRegion() throws SdkClientException {
        return System.getenv("AWS_DEFAULT_REGION");
    }
}
