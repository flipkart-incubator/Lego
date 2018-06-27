/*
 * Copyright 2016 Flipkart Internet, pvt ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package flipkart.lego.concurrency.api;

import flipkart.lego.api.entities.DataSource;
import flipkart.lego.api.entities.DataType;

public abstract class NonBlockingDataSource<S, T extends DataType> implements DataSource<PromiseWrapperFuture> {
    private Promise<S> promise;

    @Override
    public final PromiseWrapperFuture call() throws Exception {
         promise = callAsync();
         return new PromiseWrapperFuture(promise);
    }

    public T get() throws Exception {
        return map(promise.get());
    }

    public abstract Promise<S> callAsync() throws Exception;

    public abstract T map(S object) throws Exception;
}
