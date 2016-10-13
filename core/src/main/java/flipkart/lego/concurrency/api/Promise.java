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

package flipkart.lego.concurrency.api;

import flipkart.lego.concurrency.exceptions.PromiseBrokenException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * A Promise is a construct that "promises" to eventually hold some value which is usually the
 * result of some computation. It provides no guarantees on the timeliness of the computation.
 * When someone tries to get the value of a promise, the promise blocks until the value is realized.
 * A promise can be realized by either full-filling it with a value or breaking it.
 */
public interface Promise<V> {

    /**
     * Checks if the promise has been realized, if a promise has been realized then it has either
     * been full-filled or broken. This function returns false until the promise has been either
     * full-filled or broken. Will return true after the promise is full-filled or broken.
     * <p/>
     * NonBlocking
     *
     * @return true if promise is realized by either full-filling it or breaking it.
     */
    boolean isRealized();

    /**
     * Checks if the promise has been full-filled. A promise is said to have been full-filled
     * if the value of a promise is realized. It returns true if the promise has been realized
     * and the promise has been full-filled, false if the promise has been realized and the
     * promise has been broken. This should usually only be called once the promise has been
     * realized. If it's called before the promise has been realized, it throws an exception.
     * <p/>
     * NonBlocking
     *
     * @return true if a promise is realized and full-filled, false if promise is realized and broken
     * @throws IllegalStateException if isRealized()==false
     */
    boolean isFullfilled() throws IllegalStateException;

    /**
     * Checks to see if the promise has been broken. A promise is said to have been broken
     * if the promise is realized but is in a broken state. It returns true if the promise
     * is realized and a it's in a broken state, false if the promise is realized and it's
     * in a full-filled state.
     * <p/>
     * NonBlocking
     *
     * @return true if promise is broken false if promise is full-filled
     * @throws IllegalStateException if isRealized()==false
     */
    boolean isBroken() throws IllegalStateException;

    /**
     * Puts the calling thread in a wait state until the promise has been realized.
     * <p/>
     * Blocking: Puts the thread in a wait state.
     *
     * @throws InterruptedException if the calling/waiting thread is interrupted
     *                              before the promise has been realized
     */
    void await() throws InterruptedException;

    /**
     * Puts the calling thread in a wait state until either the promise has been
     * realized or "timeout" units of type "timeUnit" have passed, whichever
     * happens sooner. if the calling/waiting thread is interrupted
     * before either the promise is fullfilled or the timeout hasn't happened.
     * <p/>
     * Blocking: Puts thread in a wait state
     *
     * @param timeout
     * @param timeUnit
     * @throws InterruptedException
     */
    void await(long timeout, TimeUnit timeUnit) throws InterruptedException;

    /**
     * Puts the calling thread in a wait state until the promise has been realized
     * and either returns the value of the promise once it has been full-filled or
     * throws a promise broken exception once the promis is broken.
     * <p/>
     * Blocking: Puts thread in a wait state
     *
     * @return value of the promise once it has been set
     * @throws PromiseBrokenException of isRealized==true and promise is broken
     * @throws InterruptedException   if the calling/waiting thread is interrupted
     */
    V get() throws PromiseBrokenException, InterruptedException;

    /**
     * Puts the calling thread in a wait state until the promise has either been
     * realized or the timeout period has expired. If the promise has been realized
     * then it either returns the value of the promise once it has been full-filled or
     * throws a promise broken exception once the promis is broken. If the call has
     * timed out a timeout exception is thrown.
     * <p/>
     * Blocking: puts the thread in a wait state
     *
     * @param timeout
     * @param timeUnit
     * @return value of promise if it has been set before timeout expires
     * @throws PromiseBrokenException if isRealized==true and promise is broken
     * @throws InterruptedException   if the calling/waiting thread is interrupted
     * @throws TimeoutException       if isRealized==false until timeout, timeunits
     */
    V get(long timeout, TimeUnit timeUnit) throws PromiseBrokenException, TimeoutException, InterruptedException;

    /**
     * Adds a PromiseListener to the promise, whose whenFullfilled or whenBroken
     * method is invoked when the promise is realized.
     * <p/>
     * NonBlocking: it's a callback mechanism
     *
     * @param promiseListener
     */
    void addListener(PromiseListener promiseListener);
}
