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

import flipkart.lego.concurrency.api.Promise;
import flipkart.lego.concurrency.exceptions.PromiseBrokenException;
import flipkart.lego.concurrency.promises.DeliverablePromise;
import flipkart.lego.concurrency.promises.LazyComposablePromise;
import org.testng.annotations.Test;

import java.util.List;

public class ComposablePromiseTest {

    @Test(enabled = true)
    public void testIsRealizedFullfilledBroken() throws Exception {
        DeliverablePromise promise1 = new DeliverablePromise();
        DeliverablePromise promise2 = new DeliverablePromise();
        DeliverablePromise promise3 = new DeliverablePromise();
        boolean illegalStateExceptionThrown = false;
        Promise composablePromise = new LazyComposablePromise(promise1, promise2, promise3);

        promise1.fullFillPromise(1);
        promise2.fullFillPromise(2);
        promise3.fullFillPromise(3);

        List<Integer> result = (List<Integer>) composablePromise.get();
        assert composablePromise.isRealized();
        assert composablePromise.isFullfilled();
        assert !composablePromise.isBroken();

        assert result.get(0) == 1;

        DeliverablePromise promise4 = new DeliverablePromise();
        DeliverablePromise promise5 = new DeliverablePromise();
        DeliverablePromise promise6 = new DeliverablePromise();
        illegalStateExceptionThrown = false;
        Promise composablePromise1 = new LazyComposablePromise(promise4, promise5, promise6);

        promise4.fullFillPromise(1);
        promise5.fullFillPromise(2);
        promise6.breakPromise(new PromiseBrokenException("just felt like it"));

        composablePromise1.await();
        assert composablePromise1.isRealized();
        assert !composablePromise1.isFullfilled();
        assert composablePromise1.isBroken();
    }

    @Test(enabled = true)
    public void testConstituentPromiseBroken() throws Exception {
        DeliverablePromise promise1 = new DeliverablePromise();
        DeliverablePromise promise2 = new DeliverablePromise();
        DeliverablePromise promise3 = new DeliverablePromise();
        boolean promiseBroken = false;
        Promise composablePromise = new LazyComposablePromise(promise1, promise2, promise3);

        promise1.breakPromise(new PromiseBrokenException("just felt like it"));

        composablePromise.await();

        try {
            composablePromise.get();
        } catch (PromiseBrokenException exception) {
            promiseBroken = true;
        }
        assert promiseBroken;
    }

}
