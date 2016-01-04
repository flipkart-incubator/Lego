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

package flipkart.lego.api.entities;

import flipkart.lego.api.exceptions.LegoServiceException;
import flipkart.lego.api.helpers.Describable;
import flipkart.lego.api.helpers.Identifiable;

/**
 * A ServiceClient extends {@link Identifiable} interface
 * and is used as a base for implementing lego Service Clients
 */
public interface ServiceClient extends Identifiable, Describable {

    /**
     * Initializes the service client and any resources required.
     *
     * @throws LegoServiceException
     */
    void init() throws LegoServiceException;

    /**
     * ShutsDown service clients and frees any resources used by the service
     * client
     *
     * @throws LegoServiceException
     */
    void shutDown() throws LegoServiceException;
}
