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


import flipkart.lego.api.exceptions.ElementNotFoundException;
import flipkart.lego.api.exceptions.LegoSetException;

/**
 * A legoset holds instances of {@link Buildable} entities.
 */
public interface LegoSet {

    /**
     * For a given request it returns the appropriate buildable contained in the legoset.
     * If the appropriate buildable is not found then an ElementNotFoundException is thrown.
     * If there was some other exception due to which the buildabe could not be returned then
     * a LegoSetException is thrown
     *
     * @param request
     * @return {@link Buildable}
     * @throws LegoSetException
     * @throws ElementNotFoundException
     */
    Buildable getBuildable(Request request) throws LegoSetException, ElementNotFoundException;
}
