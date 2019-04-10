package com.gkatzioura.maven.cloud.gcs.plugin.download;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.gkatzioura.maven.cloud.KeyIteratorConcated;
import com.gkatzioura.maven.cloud.gcs.plugin.PrefixKeysIterator;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Mojo(name = "gcs-download")
public class GCSDownloadMojo extends AbstractMojo {

    @Parameter(property = "gcs-download.bucket")
    private String bucket;

    @Parameter(property = "gcs-download.keys")
    private List<String> keys;

    @Parameter(property = "gcs-download.downloadPath")
    private String downloadPath;

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
        Storage storage = StorageOptions.getDefaultInstance().getService();

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
            downloadFile(blob);
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

        File file = new File(createFullFilePath(blob.getName()));

        if(file.getParent()!=null) {
            file.getParentFile().mkdirs();
        }

        if(isDirectory(blob)) {
            return;
        }

        blob.downloadTo(file.toPath());
    }

    private final String createFullFilePath(String key) {

        String fullPath = downloadPath+"/"+key;
        return fullPath;
    }

    private final boolean isDirectory(Blob blob) {
        return false;
    }

}
