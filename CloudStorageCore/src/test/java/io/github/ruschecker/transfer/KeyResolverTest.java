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

import io.github.ruschecker.resolver.KeyResolver;
import org.junit.Assert;
import org.junit.Test;

public class KeyResolverTest {

    @Test
    public void resolveSlashDirectories() {

        KeyResolver keyResolver = new KeyResolver();
        String directoryJoin = keyResolver.resolve("/t/","/tesanother/key/");
        Assert.assertEquals("t/tesanother/key", directoryJoin);
    }

    @Test
    public void resolveEmptyBaseDirectory() {

        KeyResolver keyResolver = new KeyResolver();
        String directoryJoin = keyResolver.resolve("","/tesanother/key/");
        Assert.assertEquals("tesanother/key", directoryJoin);
    }

    @Test
    public void testResolveSimple() {

        KeyResolver keyResolver = new KeyResolver();
        String directoryJoin = keyResolver.resolve("/tesanother/key/");
        Assert.assertEquals("tesanother/key", directoryJoin);
    }

    @Test
    public void testResolveConcat() {

        KeyResolver keyResolver = new KeyResolver();
        String directoryJoin = keyResolver.resolve("test-repo/release/production/", "/tesanother/key");
        Assert.assertEquals("test-repo/release/production/tesanother/key", directoryJoin);
    }

}
