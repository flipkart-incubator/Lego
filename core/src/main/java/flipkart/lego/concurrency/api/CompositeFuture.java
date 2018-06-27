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

import com.google.common.base.Stopwatch;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompositeFuture<T> implements Future<T> {
    private Collection<Future<T>> futureList;
    private boolean allMustSucceed;

    public CompositeFuture(Collection<Future<T>> futureList, boolean allMustSucceed) {
        this.futureList = futureList;
        this.allMustSucceed = allMustSucceed;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean result = true;
        for (Future future: futureList) {
            result = result && future.cancel(mayInterruptIfRunning);
        }
        return result;
//        return futureList.stream().map(future -> future.cancel(mayInterruptIfRunning)).reduce((f1, f2) -> f1 && f2).get();
    }

    @Override
    public boolean isCancelled() {
        return futureList.stream().allMatch(Future::isCancelled);
    }

    @Override
    public boolean isDone() {
        return futureList.stream().allMatch(Future::isDone);
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        for (Future future : futureList) {
            future.get();
        }
        return null;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        //used to calculate remaining time for timeout
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            for (Future future : futureList) {
                if (timeout <= 0) {
                    break;
                }
                try {
                    future.get(timeout, unit);
                } catch (Exception e) {
                    if (allMustSucceed) {
                        throw e;
                    }
                }
                timeout = timeout - stopwatch.elapsed(TimeUnit.MILLISECONDS);
            }
        } finally {
            stopwatch.stop();
        }
        return null;
    }
}
