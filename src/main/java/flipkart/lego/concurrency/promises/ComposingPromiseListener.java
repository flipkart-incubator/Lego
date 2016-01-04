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

import flipkart.lego.concurrency.api.PromiseListener;
import flipkart.lego.concurrency.exceptions.PromiseBrokenException;
import flipkart.lego.concurrency.exceptions.PromiseRealizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * This listener facilitates composing of any promise with a one or more deliverable promises.
 * one or more deliverable promises can subscribe to a promise and all of them will be realized
 * appropriately (either full-filled or broken) when the base promise is realized.
 */
public class ComposingPromiseListener<T> implements PromiseListener<T> {

    public final List<DeliverablePromise<T>> promises = new ArrayList<>();

    /**
     * The constructor takes one or more promises that should be realized if the promise that
     * the listener instance is listening in on is realized.
     *
     * @param promises
     */
    public ComposingPromiseListener(DeliverablePromise<T>... promises) {
        Collections.addAll(this.promises, promises);
    }

    /**
     * If the promise that the listener is listening in on is full-filled then all the promises
     * that are subscribed to this listener are attempted to be full-filled by this function.
     *
     * @param value
     */
    @Override
    public void whenFullfilled(T value) {
        Logger exceptionLogger = LoggerFactory.getLogger("ExceptionLogger");
        for (DeliverablePromise<T> promise : this.promises) {
            try {
                promise.fullFillPromise(value);
            } catch (PromiseRealizedException exception) {
                exceptionLogger.error(exception.getMessage());
            }
        }
    }

    /**
     * If the promise that the listener is listening in on is broken then all the promises
     * that are subscribed to this listener are attempted to be broken by this function.
     *
     * @param promiseBrokenException
     */
    @Override
    public void whenBroken(PromiseBrokenException promiseBrokenException) {
        Logger exceptionLogger = LoggerFactory.getLogger("ExceptionLogger");
        for (DeliverablePromise<T> promise : this.promises) {
            try {
                promise.breakPromise(promiseBrokenException);
            } catch (PromiseRealizedException exception) {
                exceptionLogger.error(exception.getMessage());
            }
        }
    }

}
