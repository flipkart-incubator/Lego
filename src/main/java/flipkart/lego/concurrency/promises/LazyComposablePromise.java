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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * this promise is constructed using a bunch of promises on which it depends on
 * This promise is realized either when all it's constituent promises are successfully
 * fullfilled or when any one of it's constituent promises is broken.
 * <p/>
 * get returns a list of values of the constituent promises when all are successfully realized.
 * if any of them is broken, the get throws a promise broken exception
 * <p/>
 * the promise can only be fullfilled if we call an await/get on this promise.
 * This is what makes this promise lazy.
 */
public class LazyComposablePromise<T> implements Promise<List<T>> {

    private final List value = new ArrayList();
    private final List<PromiseListener> promiseListeners = new ArrayList<>();
    private final List<Promise> promises = new ArrayList<>();
    private boolean broken = false;
    private boolean realized = false;
    private PromiseBrokenException promiseBrokenException;

    private LazyComposablePromise() {
    } //cannot be constructed like this.

    public LazyComposablePromise(Promise... promises) {
        Collections.addAll(this.promises, promises);
    }

    //not exposed out due to lazy evaluation
    @Override
    public boolean isRealized() {
        return realized;
    }

    //not exposed out due to lazy evaluation
    @Override
    public boolean isFullfilled() throws IllegalStateException {
        if (isRealized()) {
            return !broken;
        } else {
            throw new IllegalStateException("Promise hasn't been realized yet");
        }
    }

    //not exposed out due to lazy evaluation
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
        try {
            getPromises();
        } catch (PromiseBrokenException exception) {
            promiseBrokenException = new PromiseBrokenException(exception);
        }
    }

    @Override
    public void await(long timeout, TimeUnit timeUnit) throws InterruptedException {
        try {
            getPromises(timeout, timeUnit);
        } catch (PromiseBrokenException exception) {
            promiseBrokenException = new PromiseBrokenException(exception);
        } catch (TimeoutException exception) {

        }
    }

    @Override
    public List<T> get() throws PromiseBrokenException, InterruptedException {
        getPromises();
        return value;
    }

    @Override
    public List<T> get(long timeout, TimeUnit timeUnit) throws PromiseBrokenException, TimeoutException, InterruptedException {
        getPromises(timeout, timeUnit);
        return value;
    }

    @Override
    public synchronized void addListener(PromiseListener promiseListener) {
        if (isRealized()) {
            triggerListener(promiseListener);
        } else {
            promiseListeners.add(promiseListener);
        }
    }

    //code block is synchronized to avoid concurrency issues during add listener
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

    private void getPromises() throws PromiseBrokenException, InterruptedException {
        for (Promise promise : promises) {
            try {
                value.add(promise.get());
            } catch (PromiseBrokenException exception) {
                broken = true;
                realized = true;
                promiseBrokenException = new PromiseBrokenException(exception);
                triggerListeners();
                throw exception;
            } catch (InterruptedException exception) {
                broken = true;
                realized = true;
                triggerListeners();
                throw exception;
            }
        }
        realized = true;
        triggerListeners();
    }

    private void getPromises(long timeout, TimeUnit timeUnit) throws PromiseBrokenException, InterruptedException, TimeoutException {

        for (Promise promise : promises) {
            try {
                value.add(promise.get(timeout, timeUnit));
            } catch (PromiseBrokenException exception) {
                broken = true;
                realized = true;
                promiseBrokenException = new PromiseBrokenException(exception);
                triggerListeners();
                throw exception;
            } catch (InterruptedException exception) {
                broken = true;
                realized = true;
                triggerListeners();
                throw exception;
            } catch (TimeoutException exception) {
                throw exception;
            }
            realized = true;
            triggerListeners();
        }

    }
}
