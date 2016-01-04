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

import flipkart.lego.api.exceptions.InternalErrorException;
import flipkart.lego.api.exceptions.LegoException;
import flipkart.lego.api.helpers.Identifiable;

import java.util.LinkedHashSet;
import java.util.Map;


/**
 * Buildable interface
 * <p/>
 * basic unit in lego which has associated required and optional datasources and can build itself when the data
 * is fetched from the data sources.
 * Buildables can be anything from webpages, webmodules to apiresponses.
 */
public interface Buildable extends Identifiable{

    /**
     * This method returns a long value that will be considered as the timeout value by lego when
     * the data sources are dispatched to fetch relevant data.
     *
     * @return long
     * @throws LegoException
     */
    long getTimeout() throws LegoException;

    /**
     * This method takes fetches the required {@link DataSource}s for a particular buildable for a particular {@link Request}.
     * A required data source implies that the data fetched from this data source is essential to build the buildable.
     * If the data is not fetched from a required data source is not fetched in a timely manner then building the buildable
     * is abandoned with an appropriate exception.
     *
     * @param request
     * @return Map of String and Datasource
     * @throws InternalErrorException
     */
    Map<String, DataSource> getRequiredDataSources(final Request request) throws InternalErrorException;

    /**
     * This method takes fetches the optional {@link DataSource}s for a particular buildable for a particular {@link Request}
     * An optional data source implies that the data fetched from this data source is not essential to build the buildable.
     * If the data is not fetched from an optional data source in a timely manner then the buildable is realized/built
     * without the data.
     *
     * @param request
     * @return Map of String and DataSource
     * @throws LegoException
     */
    Map<String, DataSource> getOptionalDataSources(final Request request) throws LegoException;

    /**
     * This method takes as input the data model (a map of String, Object) and {@link Request} and sets the response
     * object by building the appropriate response.
     *
     * @param request
     * @param response
     * @param model
     * @throws InternalErrorException
     */
    void build(final Request request, Response response, final Map<String, Object> model) throws InternalErrorException;

    /**
     * Returns a {@link LinkedHashSet} of {@link Filter}s for the given {@link Buildable}
     *
     * @param request
     * @return {@link LinkedHashSet} of {@link Filter}s
     * @throws LegoException
     */
    LinkedHashSet<Filter> getFilters(Request request) throws InternalErrorException;
}
