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

package flipkart.lego.concurrency.exceptions;

/*
    This exception is thrown on all invocations of get for a promise
    that will be realized by breaking it.
 */
public class PromiseBrokenException extends Exception {

    public PromiseBrokenException() {
        super();
    }

    public PromiseBrokenException(String message) {
        super(message);
    }

    public PromiseBrokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public PromiseBrokenException(Throwable cause) {
        super(cause);
    }
}
