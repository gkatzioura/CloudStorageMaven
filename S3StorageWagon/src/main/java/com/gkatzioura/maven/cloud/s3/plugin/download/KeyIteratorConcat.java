package com.gkatzioura.maven.cloud.s3.plugin.download;

import java.util.Iterator;
import java.util.function.Consumer;

public class KeyIteratorConcat implements Iterator<String> {

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public String next() {
        return null;
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
