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

package flipkart.lego.engine;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.*;
import flipkart.lego.api.entities.*;
import flipkart.lego.api.exceptions.*;
import flipkart.lego.engine.filters.FilterChain;
import flipkart.lego.engine.filters.FilterExecutionPhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * This is main engine for building responses for requests
 */
public class Lego {

    private final LegoSet legoSet;
    private final ListeningExecutorService dataSourceTPE;
    private final ExecutorService filterTPE;

    private final Logger exceptionLogger = LoggerFactory.getLogger("ExceptionLogger");

    public Lego(final LegoSet legoSet, final ExecutorService dataSourceExecutorService, final ExecutorService filterExecutorService) {
        this.legoSet = legoSet;
        this.dataSourceTPE = getListeningExecutorService(dataSourceExecutorService);
        this.filterTPE = filterExecutorService;
    }

    public void buildResponse(final Request request, Response response) throws ElementNotFoundException, BadRequestException, InternalErrorException, ProcessingException {
        final Buildable buildable;
        Map<String, DataSource> requiredDataSources = null;
        Map<String, DataSource> optionalDataSources = null;

        //this is the model that will passed on to the buildable element during build time
        Map<String, ListenableFuture> requiredFutureHashMap = new HashMap<>();
        Map<String, ListenableFuture> optionalFutureHashMap = new HashMap<>();
        Map<String, Object> modelHashMap = new HashMap<>();
        long elementTimeout = 0;


        /********************************************************************************
         Lego first tries to get an Buildable from legoset to build it.
         Every request that lego receives should resolve to one Buildable.
         */
        buildable = getBuildable(request);

        /*******************************************************************************
         * Now lego will get the element specific timeout
         */
        try {
            elementTimeout = buildable.getTimeout();
        } catch (LegoException exception) {
            exceptionLogger.warn("LegoException: {}", exception);
        }

        /**********************************************************************************
         * Now lego will execute the request {@link FilterChain} which is a chain of
         * {@link RequestFilter}s that enhance/modify the {@link Request} or cause
         * side effects based on it. elementTimeout is modified to remove elapsed time
         * in request filter chain processing.
         */
        long remainingTimeBeforeTimeout = elementTimeout;

        LinkedHashSet<Filter> filters = buildable.getFilters(request);
        FilterChain filterChain = new FilterChain(filters, filterTPE);
        remainingTimeBeforeTimeout = filterRequest(request, response, remainingTimeBeforeTimeout, filterChain);

        /*******************************************************************************
         * Once you have a buildable element get all the the required and optional data
         * sources.
         */

        try {
            requiredDataSources = buildable.getRequiredDataSources(request);
            optionalDataSources = buildable.getOptionalDataSources(request);
        } catch (LegoException exception) {
            exceptionLogger.warn("LegoException When fetching optional data sources: {}", exception);
        } catch (Throwable t) {
            exceptionLogger.error("Exception When fetching required data sources: {}", t);
            throw new InternalErrorException(t);
        }

        /********************************************************************************
         * Dispatch dataSources to threadpoolexecutor to get the relevant values from them
         * wait on required datasources until timeout or until required data and optional
         * data is available. We accomplish this by using futures returned by the dispatched
         * tasks.
         */
        if (requiredDataSources != null) {
            dispatchDataSourceTasks(requiredDataSources, requiredFutureHashMap);
        }
        if (optionalDataSources != null) {
            dispatchDataSourceTasks(optionalDataSources, optionalFutureHashMap);
        }

        //wait until timeout or until data is realized
        try {
            remainingTimeBeforeTimeout = waitUntilAvailableOrTimeout(requiredFutureHashMap, optionalFutureHashMap, request, remainingTimeBeforeTimeout);
        } catch (TimeoutException timeOutException) {
            throw new InternalErrorException(timeOutException);
        }

        //fill model with required & optional data
        fillModel(modelHashMap, requiredFutureHashMap);
        fillModel(modelHashMap, optionalFutureHashMap);

        renderElement(buildable, request, response, modelHashMap);

        /************************************************************************************
         * Once Lego renders the element and updates the response. Execute the {@link Response}
         * {@link FilterChain}. This {@link FilterChain} enhances/modifies {@link Response} or
         * causes side effects based on the {@link Response}
         */
        filterResponse(request, response, remainingTimeBeforeTimeout, filterChain);
    }

    private Buildable getBuildable(Request request) throws ElementNotFoundException, InternalErrorException {
        try {
            return legoSet.getBuildable(request);
        } catch (ElementNotFoundException exception) {
            exceptionLogger.error("ElementNotFoundException When Fetching Buildable Element From LegoSet: {}", exception);
            throw exception;
        } catch (LegoSetException exception) {
            exceptionLogger.error("LegoSetException When Fetching Buildable Element: {}", exception);
            throw new InternalErrorException(exception);
        }
    }

    private long filterRequest(Request request, Response response, long remainingTimeBeforeTimeout, FilterChain filterChain) throws InternalErrorException, BadRequestException, ProcessingException {
        try {
            return filterChain.process(request, response, FilterExecutionPhase.REQUEST, remainingTimeBeforeTimeout);
        } catch (TimeoutException timeoutException) {
            throw new InternalErrorException(timeoutException);
        } catch (ProcessingException processingException) {
            filterResponse(request, response, remainingTimeBeforeTimeout, filterChain);
            throw new ProcessingException(processingException);
        }
    }

    private void filterResponse(Request request, Response response, long remainingTimeBeforeTimeout, FilterChain filterChain) throws InternalErrorException, BadRequestException, ProcessingException {
        try {
            filterChain.process(request, response, FilterExecutionPhase.RESPONSE, remainingTimeBeforeTimeout);
        } catch (TimeoutException timeoutException) {
            throw new InternalErrorException(timeoutException);
        }
    }

    private void fillModel(Map<String, Object> model, Map<String, ListenableFuture> futureMap) {
        for (Map.Entry<String, ListenableFuture> listenableFutureEntry : futureMap.entrySet()) {
            if (listenableFutureEntry.getValue().isDone() && !listenableFutureEntry.getValue().isCancelled()) {
                try {
                    model.put(listenableFutureEntry.getKey(), listenableFutureEntry.getValue().get());
                } catch (Exception e) {
                    exceptionLogger.error("Exception in FillModel: {}", e);
                }
            }
        }
    }

    private void renderElement(Buildable buildable, Request request, Response response, Map<String, Object> modelHashMap) throws InternalErrorException {
        try {
            buildable.build(request, response, modelHashMap);
        } catch (InternalErrorException internalErrorException) {
            exceptionLogger.error("InternalErrorException: rendering page failed request:{} \n model:{} exception:{}\n", request, modelHashMap, internalErrorException);
            throw internalErrorException;
        }
    }

    private long waitUntilAvailableOrTimeout(Map<String, ListenableFuture> requiredFutureHashMap, Map<String, ListenableFuture> optionalFutureHashMap, Request request, long elementTimeout) throws TimeoutException {
        //requiredFuture is only realized if all the futures are realized
        List<ListenableFuture<Object>> requireFutureList = new ArrayList<>();
        for (Map.Entry<String, ListenableFuture> listenableFutureEntry : requiredFutureHashMap.entrySet()) {
            requireFutureList.add(listenableFutureEntry.getValue());
        }
        ListenableFuture<List<Object>> requiredFuture = Futures.allAsList(requireFutureList);

        //requiredFuture is only realized if all the futures are realized
        List<ListenableFuture<Object>> optionalFutureList = new ArrayList<>();
        for (Map.Entry<String, ListenableFuture> listenableFutureEntry : optionalFutureHashMap.entrySet()) {
            optionalFutureList.add(listenableFutureEntry.getValue());
        }
        ListenableFuture<List<Object>> optionalFuture = Futures.successfulAsList(optionalFutureList);

        //used to calculate remaining time for timeout
        Stopwatch requiredDSStopWatch = Stopwatch.createStarted();

        //Wait until timeout to see if required data is realized, if it times out throw internalErrorException
        try {
            requiredFuture.get(elementTimeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException timeoutException) {
            exceptionLogger.error("TimeOutException: required data sources timed out {}, Timeout:{}, Exception:{}", request, elementTimeout, timeoutException);
            requiredFuture.cancel(true);
            cancelFutures((Collection) requireFutureList);
            throw timeoutException;
        } catch (InterruptedException interruptedException) {
            exceptionLogger.error("InterruptedException: required data sources were interrupted{}, Message:{}, Exception:{}", request, interruptedException.getMessage(), interruptedException);
            requiredFuture.cancel(true);
            cancelFutures((Collection) requireFutureList);
            throwTimeoutException(interruptedException);
        } catch (ExecutionException executionException) {
            exceptionLogger.error("ExcecutionException: {}", executionException);
            requiredFuture.cancel(true);
            cancelFutures((Collection) requireFutureList);
            throwTimeoutException(executionException);
        }

        //if time is still remaining before timeout wait until timeout for optional data to realize itself
        requiredDSStopWatch.stop();
        long remainingTimeForTimeout = elementTimeout - requiredDSStopWatch.elapsed(TimeUnit.MILLISECONDS); //calculates milliseconds remaining before elementTimeout
        Stopwatch optionalDsStopWatch = Stopwatch.createStarted();
        if (remainingTimeForTimeout > 0) {
            try {
                optionalFuture.get(1, TimeUnit.MILLISECONDS);
            } catch (Exception exception) {
                optionalFuture.cancel(true);
                cancelFutures((Collection) optionalFutureList);
                exceptionLogger.warn("Optional Data Sources Were Not Realized {}, Exception: {}", request, exception);
            }
        }

        optionalDsStopWatch.stop();
        remainingTimeForTimeout = remainingTimeForTimeout - optionalDsStopWatch.elapsed(TimeUnit.MILLISECONDS); //calculate time remaining for execution of response filters
        return remainingTimeForTimeout > 0 ? remainingTimeForTimeout : 0;
    }

    private void throwTimeoutException(Exception exception) throws TimeoutException {
        TimeoutException timeoutException = new TimeoutException(exception.getMessage());
        timeoutException.initCause(exception);
        throw timeoutException;
    }

    private void dispatchDataSourceTasks(Map<String, DataSource> dataSources, Map<String, ListenableFuture> futureHashMap) {
        for (Map.Entry<String, DataSource> dataSourceEntry : dataSources.entrySet()) {
            ListenableFuture result = dataSourceTPE.submit(dataSourceEntry.getValue());
            futureHashMap.put(dataSourceEntry.getKey(), result);
        }

    }

    private void cancelFutures(Collection<ListenableFuture> futures) {
        for (ListenableFuture future : futures) {
            try {
                if (!future.isCancelled() && !future.isDone()) {
                    future.cancel(true);
                }
            } catch (Exception ignored) {}
        }
    }

    private ListeningExecutorService getListeningExecutorService(ExecutorService executorService) {
        return MoreExecutors.listeningDecorator(executorService);
    }

}
