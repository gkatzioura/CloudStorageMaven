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

package io.github.ruschecker.transfer;

import org.apache.maven.wagon.resource.Resource;

import io.github.ruschecker.listener.TransferListenerContainer;

public class TransferProgressImpl implements TransferProgress {

    private final Resource resource;
    private final int requestType;
    private final TransferListenerContainer listenerContainer;

    public TransferProgressImpl(Resource resource, int requestType, TransferListenerContainer listenerContainer) {
        this.resource = resource;
        this.requestType = requestType;
        this.listenerContainer = listenerContainer;
    }

    @Override public void progress(byte[] buffer, int length) {
        listenerContainer.fireTransferProgress(this.resource, this.requestType, buffer, length);
    }
}
