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

import flipkart.lego.api.exceptions.BadRequestException;
import flipkart.lego.api.exceptions.InternalErrorException;
import flipkart.lego.api.exceptions.ProcessingException;

/**
 * A simple {@link Filter} interface that
 * filters based on {@link Request}s and {@link Response}s
 */
public interface Filter {

    /**
     * Triggers processing of the request by this filter entity
     *
     * @param request
     * @param response
     * @throws InternalErrorException
     * @throws BadRequestException
     * @throws ProcessingException
     */
    public void filterRequest(Request request, Response response) throws InternalErrorException, BadRequestException, ProcessingException;

    /**
     * Triggers processing of the response by this filter
     *
     * @param request
     * @param response
     * @throws InternalErrorException
     * @throws BadRequestException
     * @throws ProcessingException
     */
    public void filterResponse(Request request, Response response) throws InternalErrorException, BadRequestException, ProcessingException;

}
