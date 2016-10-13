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

package flipkart.lego.engine.filters;

import com.google.common.base.Stopwatch;
import flipkart.lego.api.entities.Filter;
import flipkart.lego.api.entities.Request;
import flipkart.lego.api.entities.Response;
import flipkart.lego.api.exceptions.BadRequestException;
import flipkart.lego.api.exceptions.InternalErrorException;
import flipkart.lego.api.exceptions.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.*;

/**
 * {@link FilterChain} is an entity that processes a given {@link flipkart.lego.api.entities.Request}
 * and {@link flipkart.lego.api.entities.Response}
 * through a chain of Filters. We use a callable to process the filter chain because
 * we need Timeout semantics around the processing.
 */
public class FilterChain {
    private LinkedHashSet<Filter> filterChain;
    private final ExecutorService filterTPE;
    private final Logger exceptionLogger = LoggerFactory.getLogger("ExceptionLogger");


    public FilterChain(LinkedHashSet<Filter> filterChain, ExecutorService filterTPE) {
        this.filterChain = filterChain;
        this.filterTPE = filterTPE;
    }

    /**
     * Processes a given filter chain and returns remaining time for timeout
     *
     * @param request
     * @param response
     * @param filterExecutionPhase
     * @param remainingTime
     * @return remaining time for timeout after filter processing
     * @throws java.util.concurrent.TimeoutException
     * @throws flipkart.lego.api.exceptions.InternalErrorException
     * @throws flipkart.lego.api.exceptions.BadRequestException
     * @throws flipkart.lego.api.exceptions.ProcessingException
     */
    public long process(Request request, Response response, FilterExecutionPhase filterExecutionPhase, long remainingTime) throws TimeoutException, InternalErrorException, BadRequestException, ProcessingException {
        Stopwatch filterChainStopwatch = Stopwatch.createStarted();

        Future filterProcessing = this.filterTPE.submit(new FilterChainExecutor(this, filterExecutionPhase, request, response));

        try {
            filterProcessing.get(remainingTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            filterProcessing.cancel(true);
            exceptionLogger.error("Filter Processing timed out: ", filterChain);
            throw new TimeoutException("Timed out while executing filterChain");
        } catch (ExecutionException e) {
            filterProcessing.cancel(true);
            if (e.getCause() instanceof InternalErrorException) {
                exceptionLogger.error("InternalErrorException when processing filters", filterChain);
                throw (InternalErrorException) e.getCause();
            } else if (e.getCause() instanceof BadRequestException) {
                exceptionLogger.error("BadRequestException when processing filters", filterChain);
                throw (BadRequestException) e.getCause();
            } else if (e.getCause() instanceof ProcessingException) {
                throw (ProcessingException) e.getCause();
            } else {
                exceptionLogger.error("Execution Exception when processing filters", filterChain);
                throw new InternalErrorException(e);
            }
        }

        filterChainStopwatch.stop();
        return remainingTime - filterChainStopwatch.elapsed(TimeUnit.MILLISECONDS);
    }

    private class FilterChainExecutor implements Callable {

        private final FilterChain filterChain;
        private final FilterExecutionPhase filterExecutionPhase;
        private final Request request;
        private final Response response;

        private FilterChainExecutor(FilterChain filterChain, FilterExecutionPhase filterExecutionPhase, Request request, Response response) {
            this.filterChain = filterChain;
            this.filterExecutionPhase = filterExecutionPhase;
            this.request = request;
            this.response = response;
        }

        @Override
        public Object call() throws Exception {
            List<Filter> executedFilters = new ArrayList<>();
            try {

                for (Filter filter : filterChain.filterChain) {
                    switch (filterExecutionPhase) {
                        case REQUEST:
                            filter.filterRequest(request, response);
                            executedFilters.add(filter);
                            break;
                        case RESPONSE:
                            filter.filterResponse(request, response);
                            break;
                        default:
                            break;
                    }
                }
            } finally {
                Collections.reverse(executedFilters);
                filterChain.filterChain = new LinkedHashSet<>(executedFilters);
            }
            return executedFilters;
        }
    }

}
