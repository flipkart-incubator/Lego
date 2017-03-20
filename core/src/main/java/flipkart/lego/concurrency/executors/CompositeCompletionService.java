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

package flipkart.lego.concurrency.executors;

import java.util.List;
import java.util.concurrent.*;

/**
 * Created by mohan.pandian on 15/03/17.
 */
public interface CompositeCompletionService {
     Future submit(Callable callable);

     List<Future> submit(List<Callable> callables);

    void wait(List<Future> futures, long timeout) throws TimeoutException, InterruptedException, ExecutionException;
}
