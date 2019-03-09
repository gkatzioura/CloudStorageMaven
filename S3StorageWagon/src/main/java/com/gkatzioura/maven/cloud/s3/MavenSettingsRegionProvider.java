package com.gkatzioura.maven.cloud.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.regions.AwsRegionProvider;

public class MavenSettingsRegionProvider extends AwsRegionProvider {

    public final String settingsRegion;

    public MavenSettingsRegionProvider(String settingsRegion) {
        this.settingsRegion = settingsRegion;
    }

    @Override
    public String getRegion() throws SdkClientException {
        return settingsRegion;
    }
}
