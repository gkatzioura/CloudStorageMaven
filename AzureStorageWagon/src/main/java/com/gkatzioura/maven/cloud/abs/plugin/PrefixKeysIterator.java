package com.gkatzioura.maven.cloud.abs.plugin;

import java.util.Iterator;
import java.util.function.Consumer;

import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;

public class PrefixKeysIterator implements Iterator<ListBlobItem> {

    private final CloudBlobContainer cloudBlobContainer;
    private final String prefix;

    private final Iterator<ListBlobItem> tempListing;

    public PrefixKeysIterator(final CloudBlobContainer cloudBlobContainer, final String prefix) {
        this.cloudBlobContainer = cloudBlobContainer;
        this.prefix = prefix;
        tempListing = cloudBlobContainer.listBlobs(prefix,true).iterator();
    }

    @Override
    public boolean hasNext() {
        return tempListing.hasNext();
    }

    @Override
    public ListBlobItem next() {
        return tempListing.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachRemaining(Consumer<? super ListBlobItem> action) {
        throw new UnsupportedOperationException();
    }
}
