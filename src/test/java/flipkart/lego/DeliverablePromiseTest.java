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
import flipkart.lego.concurrency.exceptions.PromiseRealizedException;
import flipkart.lego.concurrency.promises.DeliverablePromise;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class DeliverablePromiseTest {

    @Test(enabled = true)
    public void testIsRealized() {
        DeliverablePromise promiseToKeep = new DeliverablePromise();
        DeliverablePromise promiseToBreak = new DeliverablePromise();
        boolean exceptionThrown = false;

        assert !promiseToKeep.isRealized();
        assert !promiseToBreak.isRealized();

        try {
            promiseToKeep.fullFillPromise(100l);
            promiseToBreak.breakPromise(new PromiseBrokenException("just felt like it"));
        } catch (PromiseRealizedException exception) {
            exceptionThrown = true;
        }

        assert !exceptionThrown;

        assert promiseToKeep.isRealized();
        assert promiseToBreak.isRealized();
    }

    @Test(enabled = true)
    public void testIsFullFilledAndIsBroken() throws Exception {
        DeliverablePromise promiseToKeep = new DeliverablePromise();
        DeliverablePromise promiseToBreak = new DeliverablePromise();
        boolean IllegalStateExceptionThrown = false;

        try {
            promiseToKeep.isFullfilled();
        } catch (IllegalStateException exception) {
            IllegalStateExceptionThrown = true;
        }

        assert IllegalStateExceptionThrown;

        IllegalStateExceptionThrown = false;

        try {
            promiseToBreak.isBroken();
        } catch (IllegalStateException exception) {
            IllegalStateExceptionThrown = true;
        }

        assert IllegalStateExceptionThrown;

        promiseToBreak.breakPromise(new PromiseBrokenException("just felt like it"));
        promiseToKeep.fullFillPromise(100l);

        assert promiseToKeep.isFullfilled();
        assert !promiseToBreak.isFullfilled();

        assert !promiseToKeep.isBroken();
        assert promiseToBreak.isBroken();
    }

    @Test(enabled = true)
    public void testAddListener() throws Exception {
        DeliverablePromise promiseToKeep = new DeliverablePromise();
        DeliverablePromise promiseToBreak = new DeliverablePromise();

        DeliverablePromise higherLevelPromiseToKeep = new DeliverablePromise(promiseToKeep);
        DeliverablePromise higherLevelPromiseToBreak = new DeliverablePromise(promiseToBreak);

        boolean promiseBrokenExceptionThrown = false;

        promiseToKeep.fullFillPromise(100l);
        assert (Long) higherLevelPromiseToKeep.get(100000l, TimeUnit.SECONDS) == 100l;


        promiseToBreak.breakPromise(new PromiseBrokenException("just felt like it"));

        try {
            higherLevelPromiseToBreak.get(100000l, TimeUnit.SECONDS);
        } catch (PromiseBrokenException exception) {
            promiseBrokenExceptionThrown = true;
        }

        assert promiseBrokenExceptionThrown;
    }
}
