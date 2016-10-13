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

package flipkart.lego.concurrency.promises;

import flipkart.lego.concurrency.api.Promise;
import flipkart.lego.concurrency.api.PromiseListener;
import flipkart.lego.concurrency.exceptions.PromiseBrokenException;
import flipkart.lego.concurrency.exceptions.PromiseRealizedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * A deliverable promise is a promise that can be explicitly full-filled or broken
 * Deliverable promise provides two methods to do the same.
 * <p/>
 * one is a fullfillPromise(T value)
 * and another is breakPromise(promiseBrokenException)
 */
public class DeliverablePromise<T> implements Promise<T> {

    // we use a CountDownLatch at the core of a deliverable promise to coordinate promise delivery
    private final CountDownLatch countDownLatch;
    private T value = null;
    private final List<PromiseListener> promiseListeners = new ArrayList<>();
    private boolean broken = false;
    private PromiseBrokenException promiseBrokenException;

    /**
     * A deliverable promise can be constructed using this constructor and can later
     * be explicitly realized.
     */
    public DeliverablePromise() {
        this.countDownLatch = new CountDownLatch(1);
    }

    /**
     * A deliverable promise can be constructed using this constructor and is realized
     * when the base promise is realized.
     *
     * @param promise
     */
    public DeliverablePromise(Promise<T> promise) {
        this.countDownLatch = new CountDownLatch(1);
        PromiseListener promiseListener = new ComposingPromiseListener<>(this);
        promise.addListener(promiseListener);
    }

    @Override
    public boolean isRealized() {
        return countDownLatch.getCount() == 0;
    }


    @Override
    public boolean isFullfilled() throws IllegalStateException {
        if (isRealized()) {
            return !broken;
        } else {
            throw new IllegalStateException("Promise hasn't been realized yet");
        }
    }


    @Override
    public boolean isBroken() throws IllegalStateException {
        if (isRealized()) {
            return broken;
        } else {
            throw new IllegalStateException("Promise hasn't been realized yet");
        }
    }


    @Override
    public void await() throws InterruptedException {
        countDownLatch.await();
    }


    @Override
    public void await(long timeout, TimeUnit timeUnit) throws InterruptedException {
        countDownLatch.await(timeout, timeUnit);
    }


    @Override
    public T get() throws PromiseBrokenException, InterruptedException {
        await();
        if (isFullfilled()) {
            return value;
        } else {
            throw new PromiseBrokenException(promiseBrokenException);
        }
    }


    @Override
    public T get(long timeout, TimeUnit timeUnit) throws PromiseBrokenException, TimeoutException, InterruptedException {
        await(timeout, timeUnit);
        if (isRealized())

        {
            if (isFullfilled()) {
                return value;
            } else {
                throw new PromiseBrokenException(promiseBrokenException);
            }
        } else {
            throw new TimeoutException("Timed out waiting for the promise");
        }

    }

    /*This method is synchronized to avoid the read-examine-write race conditions*/
    @Override
    public synchronized void addListener(PromiseListener promiseListener) {
        if (isRealized()) {
            triggerListener(promiseListener);
        } else {
            promiseListeners.add(promiseListener);
        }
    }

    /*
    * sets the value of the promise and the promise is said to have been realized
    * once this is done. It subsequently calls the promise listeners
    *
    * @arg value to be set
    * @throws PromiseRealizedException if the promise was already set.
    */
    public void fullFillPromise(T value) throws PromiseRealizedException {
        if (!isRealized()) {
            this.value = value;
            countDownLatch.countDown();
            triggerListeners();
        } else {
            throw new PromiseRealizedException("Promise already fulfilled");
        }
    }

    /*
    * breaks the promise and the promise is said to have been realized
    * once this is done. It subsequently calls the promise listeners
    *
    * @throws PromiseRealizedException if the promise was already set.
    */
    public void breakPromise(PromiseBrokenException promiseBrokenException) throws PromiseRealizedException {
        if (!isRealized()) {
            countDownLatch.countDown();
            broken = true;
            this.promiseBrokenException = new PromiseBrokenException(promiseBrokenException);
            triggerListeners();
        } else {
            throw new PromiseRealizedException("Promise already fulfilled");
        }
    }

    /*This method is synchronized to avoid race conditions*/
    private synchronized void triggerListeners() {
        for (PromiseListener promiseListener : promiseListeners) {
            triggerListener(promiseListener);
        }
    }

    private void triggerListener(PromiseListener promiseListener) {
        if (isFullfilled()) {
            promiseListener.whenFullfilled(value);
        } else {
            promiseListener.whenBroken(promiseBrokenException);
        }
    }
}
