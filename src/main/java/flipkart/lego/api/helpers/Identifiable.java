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

package flipkart.lego.api.helpers;


/**
 * This interface implies that an implementing entity can be identified using it's name(human readable), Id and version.
 */
public interface Identifiable extends Versioned{

    /**
     * Gets the Id of the entity as a String
     *
     * @return Id of the entity
     * @throws UnsupportedOperationException
     */
    String getId() throws UnsupportedOperationException;

    /**
     * Gets the Name of an entity
     *
     * @return Name of an entity
     * @throws UnsupportedOperationException
     */
    String getName() throws UnsupportedOperationException;

}
