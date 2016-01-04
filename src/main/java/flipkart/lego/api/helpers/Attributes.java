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


import java.util.Map;

/**
 * The Attributes interface sets and gets variable attributes for an entity.
 */
public interface Attributes {

    /**
     * set an attribute where each attribute is identified by a key and has an appropriate value
     * which is a Java Object.
     *
     * @param key
     * @param value
     */
    void setAttribute(String key, Object value);

    /**
     * Sets multiple attributes for the entity by taking a Map as input.
     *
     * @param attributes
     */
    void setAttributes(Map<String, Object> attributes);

    /**
     * Gets value of an attribute by giving the relevant key as input
     *
     * @param key
     * @return Object
     */
    Object getAttribute(String key);

    /**
     * Gets all attibute keys and values as a Map.
     *
     * @return Map
     */
    Map<String, Object> getAttributeMap();

    boolean hasAttribute(String key);
}
