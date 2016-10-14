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

/**
 * A PromiseListener listens in on a promise for it's realization.
 * once a promise has been realized the promise calls all the
 * listeners associated with it using the appropriate handler.
 * <p/>
 * How the promise invokes it's listeners is dependent on whether
 * the promise has been full-filled or broken.
 * <p/>
 * If the promise has been full-filled then the whenFullfilled method
 * of the listener is invoked with the value of the full-filled promise
 * <p/>
 * If the promise has been broken then the whenBroken method of the
 * listener is invoked.
 */
public interface PromiseListener<V> {

    /**
     * this is called when the promise this listener is listening in
     * on has been full-filled.
     *
     * @param value
     * @args it takes the value of the full-filled promise as an argument
     */
    void whenFullfilled(V value);

    /**
     * this is called when the promise this listener is listening in
     * on has been broken.
     *
     * @param promiseBrokenException
     */
    void whenBroken(PromiseBrokenException promiseBrokenException);
}
