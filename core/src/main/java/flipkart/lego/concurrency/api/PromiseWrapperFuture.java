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

import flipkart.lego.api.entities.DataType;
import flipkart.lego.concurrency.exceptions.PromiseBrokenException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PromiseWrapperFuture<T> implements Future<T>, DataType {
    private Promise<T> promise;

    public PromiseWrapperFuture(Promise<T> promise) {
        this.promise = promise;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return promise.isRealized();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        try {
            return promise.get();
        } catch (PromiseBrokenException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            return promise.get(timeout, unit);
        } catch (PromiseBrokenException e) {
            throw new ExecutionException(e);
        }
    }
}
