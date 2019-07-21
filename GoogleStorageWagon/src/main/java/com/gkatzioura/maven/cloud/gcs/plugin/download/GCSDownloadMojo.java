package com.gkatzioura.maven.cloud.gcs.plugin.download;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.gkatzioura.maven.cloud.KeyIteratorConcated;
import com.gkatzioura.maven.cloud.gcs.StorageFactory;
import com.gkatzioura.maven.cloud.gcs.plugin.PrefixKeysIterator;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;

@Mojo(name = "gcs-download")
public class GCSDownloadMojo extends AbstractMojo {

    @Parameter(property = "gcs-download.bucket")
    private String bucket;

    @Parameter(property = "gcs-download.keys")
    private List<String> keys;

    @Parameter(property = "gcs-download.downloadPath")
    private String downloadPath;

    @Parameter(property = "gcs-download.keyPath")
    private String keyPath;

    private final StorageFactory storageFactory = new StorageFactory();
    private Storage storage;

    private static final Logger LOGGER = Logger.getLogger(GCSDownloadMojo.class.getName());

    public GCSDownloadMojo() {
    }

    public GCSDownloadMojo(String bucket, List<String> keys, String downloadPath) throws MojoExecutionException, MojoFailureException {
        this.bucket = bucket;
        this.keys = keys;
        this.downloadPath = downloadPath;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        storage = initializeStorage();

        if (keys.size()==1) {
            downloadSingleFile(storage,keys.get(0));
            return;
        }

        List<Iterator<Blob>> prefixKeyIterators = keys.stream()
                                                        .map(pi -> new PrefixKeysIterator(storage,bucket,pi))
                                                        .collect(Collectors.toList());

        Iterator<Blob> keyIteratorConcated = new KeyIteratorConcated(prefixKeyIterators);

        while (keyIteratorConcated.hasNext()) {
            Blob blob = keyIteratorConcated.next();
            LOGGER.info("Scheduling blob for download "+blob.getBucket()+" "+blob.getName());
            downloadFile(blob);
        }
    }

    private Storage initializeStorage() throws MojoExecutionException {
        if(keyPath==null) {
            return storageFactory.createDefault();
        } else {
            try {
                return storageFactory.createWithKeyFile(keyPath);
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to set Authentication to Google Cloud");
            }
        }
    }

    private void downloadSingleFile(Storage storage,String key) {
        File file = new File(downloadPath);

        if(file.getParentFile()!=null) {
            file.getParentFile().mkdirs();
        }

        Blob blob = storage.get(BlobId.of(bucket, key));
        blob.downloadTo(file.toPath());
    }

    private void downloadFile(Blob blob) {
        LOGGER.log(Level.INFO, "Downloading from bucket " + blob.getBucket() + " with key " + blob.getName());
        File file = new File(createFullFilePath(blob.getName()));

        if(file.getParent()!=null) {
            file.getParentFile().mkdirs();
        }

        if(isDirectory(blob)) {
            LOGGER.log(Level.INFO,"Bucket "+blob.getBucket()+" key "+blob.getName()+" is as directory");
            return;
        }

        LOGGER.info("Downloading file "+blob.getBucket()+" key "+blob.getName()+" to path "+file.toPath());


        LOGGER.info("Path file "+file.isDirectory()+" "+file.isFile());

        blob.downloadTo(file.toPath());
    }

    private final String createFullFilePath(String key) {

        String fullPath = downloadPath+"/"+key;
        return fullPath;
    }

    /**
     * Due to blob.isDirectory is not working as expected will have to check if there are more than two files
     * @param blob
     * @return
     */
    private final boolean isDirectory(Blob blob) {
        Iterator<Blob> blobs = storage.list(bucket,
                                            Storage.BlobListOption.prefix(blob.getName()
                                            )).getValues().iterator();

        if(blobs.hasNext()) {
            blobs.next();
            return blobs.hasNext();
        }

        return false;
    }

}
