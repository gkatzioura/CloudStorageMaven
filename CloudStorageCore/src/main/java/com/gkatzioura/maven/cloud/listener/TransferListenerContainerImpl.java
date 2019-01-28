/*
 * Copyright 2018 Emmanouil Gkatziouras
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gkatzioura.maven.cloud.listener;

import java.io.File;
import java.util.Vector;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.resource.Resource;

public class TransferListenerContainerImpl implements TransferListenerContainer {

    private final Wagon wagon;
    private final Vector<TransferListener> transferListeners;

    public TransferListenerContainerImpl(Wagon wagon) {
        this.wagon = wagon;
        this.transferListeners = new Vector<>();
    }

    @Override
    public void addTransferListener(TransferListener transferListener) {
        if(transferListener==null) {
            throw new NullPointerException();
        }
        if(!transferListeners.contains(transferListener)) {
            transferListeners.add(transferListener);
        }
    }

    @Override
    public void removeTransferListener(TransferListener transferListener) {
        transferListeners.remove(transferListener);
    }

    @Override
    public boolean hasTransferListener(TransferListener transferListener) {
        return transferListeners.contains(transferListener);
    }

    @Override
    public void fireTransferInitiated(Resource resource, int requestType) {
        TransferEvent transferEvent = new TransferEvent(this.wagon,resource,TransferEvent.TRANSFER_INITIATED,requestType);
        transferListeners.forEach(tl->tl.transferInitiated(transferEvent));
    }

    @Override
    public void fireTransferStarted(Resource resource, int requestType, File localFile) {
        resource.setContentLength(localFile.length());
        resource.setLastModified(localFile.lastModified());
        TransferEvent transferEvent = new TransferEvent(this.wagon,resource,TransferEvent.TRANSFER_STARTED,requestType);
        transferEvent.setLocalFile(localFile);
        transferListeners.forEach(tl->tl.transferStarted(transferEvent));
    }

    @Override
    public void fireTransferProgress(Resource resource, int requestType, byte[] buffer, int length) {
        TransferEvent transferEvent = new TransferEvent(this.wagon, resource, TransferEvent.TRANSFER_PROGRESS, requestType);
        transferListeners.forEach(tl->tl.transferProgress(transferEvent,buffer,length));
    }

    @Override public void fireTransferCompleted(Resource resource, int requestType) {
        TransferEvent transferEvent = new TransferEvent(this.wagon, resource, TransferEvent.TRANSFER_COMPLETED, requestType);
        transferListeners.forEach(tl->tl.transferCompleted(transferEvent));
    }

    @Override public void fireTransferError(Resource resource, int requestType, Exception exception) {
        TransferEvent transferEvent = new TransferEvent(this.wagon, resource, exception, requestType);
        transferListeners.forEach(tl->tl.transferError(transferEvent));
    }
}
