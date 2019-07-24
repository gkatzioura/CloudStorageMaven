/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2019 Feedzai, Strictly Confidential
 */
package com.gkatzioura.maven.cloud.s3.utils;

import com.amazonaws.SdkClientException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.gkatzioura.maven.cloud.s3.CredentialsFactory;
import com.gkatzioura.maven.cloud.s3.EndpointProperty;
import com.gkatzioura.maven.cloud.s3.PathStyleEnabledProperty;
import com.gkatzioura.maven.cloud.s3.S3StorageRegionProviderChain;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

import java.util.logging.Logger;

/**
 * Utility methods used to connect to Amazon's S3 API.
 */
public class S3Connect {

    /**
     * A logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(S3Connect.class.getName());

    /**
     * Connects to the AWS API. The provided authentication, region, endpoint and path-style are all taken into account
     * to create the returned {@link AmazonS3} instance.
     *
     * @param authenticationInfo When {@code authenticationInfo} is passed as {@code null}, an authentication provider
     *                           that gets the credentials from environment properties, system environment variables or
     *                           other global locations will be used. See the documentation for the
     *                           <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html">DefaultAWSCredentialsProviderChain</a>
     *                           class for details.
     * @param region    The region where the bucket was created in.
     * @param endpoint  The endpoint/bucket to connect to.
     * @param pathStyle A {@link PathStyleEnabledProperty} indicating whether the endpoint/bucket configuration being
     *                  passed is in a path-style configuration. See
     *                  <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/UsingBucket.html#access-bucket-intro">Accessing a Bucket in the S3 documentation</a>.
     * @return An instance of {@link AmazonS3} that can be used to send and receive data to the intended endpoint/bucket.
     * @throws AuthenticationException if the passed credentials are invalid for connecting to the intended endpoint/bucket.
     */
    public static AmazonS3 connect(AuthenticationInfo authenticationInfo, String region, EndpointProperty endpoint, PathStyleEnabledProperty pathStyle) throws AuthenticationException {
        AmazonS3ClientBuilder builder = null;
        try {
            builder = createAmazonS3ClientBuilder(authenticationInfo, region, endpoint, pathStyle);

            AmazonS3 amazonS3 = builder.build();

            LOGGER.finer(String.format("Connected to S3 using bucket %s.", endpoint.get()));

            return amazonS3;
        } catch (SdkClientException e) {
            if (builder != null){
                StringBuilder message = new StringBuilder();
                message.append("Failed to connect");
                if (builder.getEndpoint() != null){
                    message.append(
                            String.format(" to endpoint [%s] using region [%s]",
                                    builder.getEndpoint().getServiceEndpoint(),
                                    builder.getEndpoint().getSigningRegion()));

                } else {
                    message.append(String.format(" using region [%s]", builder.getRegion()));
                }
                throw new AuthenticationException(message.toString(), e);
            }
            throw new AuthenticationException("Could not authenticate", e);
        }
    }

    private static AmazonS3ClientBuilder createAmazonS3ClientBuilder(AuthenticationInfo authenticationInfo, String region, EndpointProperty endpoint, PathStyleEnabledProperty pathStyle) {
        final S3StorageRegionProviderChain regionProvider = new S3StorageRegionProviderChain(region);

        AmazonS3ClientBuilder builder;
        builder = AmazonS3ClientBuilder.standard().withCredentials(new CredentialsFactory().create(authenticationInfo));

        if (endpoint.isPresent()){
            builder.setEndpointConfiguration( new AwsClientBuilder.EndpointConfiguration(endpoint.get(), builder.getRegion()));
        } else {
            builder.setRegion(regionProvider.getRegion());
        }

        builder.setPathStyleAccessEnabled(pathStyle.get());
        return builder;
    }
}
