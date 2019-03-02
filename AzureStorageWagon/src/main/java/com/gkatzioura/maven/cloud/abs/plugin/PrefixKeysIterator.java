package com.gkatzioura.maven.cloud.abs.plugin;

import java.util.Iterator;
import java.util.function.Consumer;

import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;

public class PrefixKeysIterator implements Iterator<String> {

    private final CloudBlobContainer cloudBlobContainer;
    private final String prefix;

    private Iterator<ListBlobItem> tempListing = null;

    public PrefixKeysIterator(final CloudBlobContainer cloudBlobContainer, final String prefix) {
        this.cloudBlobContainer = cloudBlobContainer;
        this.prefix = prefix;
        tempListing = cloudBlobContainer.listBlobs(prefix).iterator();
    }

    @Override
    public boolean hasNext() {
        return tempListing.hasNext();
    }

    @Override
    public String next() {
        return tempListing.next().getUri().toString();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachRemaining(Consumer<? super String> action) {
        throw new UnsupportedOperationException();
    }
}
