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

package com.gkatzioura.maven.cloud.transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public final class TransferProgressFileInputStream extends FileInputStream {

    private final TransferProgress transferProgress;

    public TransferProgressFileInputStream(File file, TransferProgress transferProgress) throws IOException{
        super(file);
        this.transferProgress = transferProgress;
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if(b != -1){
            this.transferProgress.progress(new byte[]{(byte) b}, 1);
        }//else we try to read but it was the end of the stream so nothing to report
        return b;
    }

    @Override
    public int read(byte b[]) throws IOException {
        int count = super.read(b);
        if (count < 1) {
            return count;
        }

        this.transferProgress.progress(b, count);
        return count;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int count = super.read(b, off, len);
        if (count < 1) {
            return count;
        }

        if(off == 0) {
            this.transferProgress.progress(b, count);
        } else {
            byte[] bytes = new byte[count];
            System.arraycopy(b, off, bytes, 0, count);
            this.transferProgress.progress(bytes, count);
        }

        return count;
    }
}
