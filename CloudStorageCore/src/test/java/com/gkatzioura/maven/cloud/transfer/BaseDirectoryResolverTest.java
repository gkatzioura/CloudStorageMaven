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

import org.apache.maven.wagon.repository.Repository;
import org.junit.Assert;
import org.junit.Test;

import com.gkatzioura.maven.cloud.resolver.BaseDirectoryResolver;

public class BaseDirectoryResolverTest {

    @Test
    public void testResolve() {

        BaseDirectoryResolver directoryResolver = new BaseDirectoryResolver();
        Repository repository = new Repository("test-repo","gs://test-repo/release");
        String baseDirectory = directoryResolver.resolve(repository);
        Assert.assertEquals("release/",baseDirectory);
    }

    @Test
    public void testResolveSubDirectory() {

        BaseDirectoryResolver directoryResolver = new BaseDirectoryResolver();
        Repository repository = new Repository("test-repo","gs://test-repo/release/production");
        String baseDirectory = directoryResolver.resolve(repository);
        Assert.assertEquals("release/production/",baseDirectory);
    }

}
