package com.gkatzioura.maven.cloud.s3.plugin.download;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class KeyIteratorConcated implements Iterator<String> {

    private final List<Iterator<String>> iterators;

    public KeyIteratorConcated(List<Iterator<String>> iterators) {
        this.iterators = iterators;
    }

    @Override
    public boolean hasNext() {
        if(iterators.size()>0) {
            return iterators.get(0).hasNext();
        } else {
            return false;
        }
    }

    @Override
    public String next() {
        if(iterators.size()==0) {
            return null;
        }

        Iterator<String> stringIterator = iterators.get(0);

        if(!stringIterator.hasNext()) {
            iterators.remove(stringIterator);
            next();
        }

        return stringIterator.next();
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
