package com.gkatzioura.maven.cloud.gcs.plugin;

import java.util.Iterator;
import java.util.function.Consumer;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;

public class PrefixKeysIterator implements Iterator<Blob> {

    private final Storage storage;
    private final String bucket;
    private final String prefix;

    private final Iterator<Blob> tempListing;

    public PrefixKeysIterator(final Storage storage, final String bucket, final String prefix) {
        this.storage = storage;
        this.bucket = bucket;
        this.prefix = prefix;
        this.tempListing = storage.list(bucket,Storage.BlobListOption.prefix(prefix))
                                  .iterateAll()
                                  .iterator();
    }

    @Override
    public boolean hasNext() {
        return tempListing.hasNext();
    }

    @Override
    public Blob next() {
        return tempListing.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachRemaining(Consumer<? super Blob> action) {
        throw new UnsupportedOperationException();
    }

}
