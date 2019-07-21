package com.gkatzioura.maven.cloud.gcs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class StorageFactory {

    private static final Logger LOGGER = Logger.getLogger(StorageFactory.class.getName());

    public Storage createWithKeyFile(String keyPath) throws IOException {
        File credentialsPath = new File(keyPath);
        try(FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
            GoogleCredentials googleCredentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
            return StorageOptions.newBuilder().setCredentials(googleCredentials).build().getService();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not parse properly service account key file", e);
            throw e;
        }
    }

    public Storage createDefault() {
        return StorageOptions.getDefaultInstance().getService();
    }
}
