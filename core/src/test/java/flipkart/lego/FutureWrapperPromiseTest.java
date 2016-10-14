/*
 * Copyright 2015 Flipkart Internet, pvt ltd.
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

package flipkart.lego;

import flipkart.lego.concurrency.exceptions.PromiseBrokenException;
import flipkart.lego.concurrency.promises.DeliverablePromise;
import flipkart.lego.concurrency.promises.FutureWrapperPromise;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FutureWrapperPromiseTest {

    private ExecutorService tpe = Executors.newSingleThreadExecutor();

    @Test
    public void isRealizedTest() throws Exception {
        Future future = tpe.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "dude";
            }
        });

        FutureWrapperPromise promise = new FutureWrapperPromise(future);
        promise.await();

        assert promise.isRealized();
        assert promise.isFullfilled();
        assert promise.get() == "dude";
    }

    @Test
    public void listenersTest() throws Exception {
        Future future = tpe.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "dude";
            }
        });

        FutureWrapperPromise promise = new FutureWrapperPromise(future);
        DeliverablePromise deliverablePromise = new DeliverablePromise(promise);

        promise.await();
        assert deliverablePromise.get() == "dude";
    }

    @Test
    public void futureCancellationTest() throws Exception {
        Future future = tpe.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(100000l);
                return "dude";
            }
        });

        FutureWrapperPromise promise = new FutureWrapperPromise(future);
        boolean promiseBrokenExceptionThrown = false;

        future.cancel(true);

        try {
            promise.get();
        } catch (PromiseBrokenException exception) {
            promiseBrokenExceptionThrown = true;
        }

        assert promiseBrokenExceptionThrown;
    }
}
