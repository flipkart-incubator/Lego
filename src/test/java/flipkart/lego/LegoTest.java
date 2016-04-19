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

package flipkart.lego;

import flipkart.lego.api.entities.*;
import flipkart.lego.api.exceptions.*;
import flipkart.lego.engine.Lego;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.testng.Assert.assertTrue;

public class LegoTest {

    Request request;
    Response response;
    Buildable buildable;
    LegoSet legoSet;
    Lego lego;
    Map<String, DataSource> dataSourceMap;
    DataSource dataSource;
    ExecutorService executorService = Executors.newCachedThreadPool();

    class DummyDT implements DataType {
        @Override
        public String getShortDescription() {
            return null;
        }

        public String getResponse() {
            return "Some ds response";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    DataType dataType = new DummyDT();

    Filter sleepingFilter = new Filter() {
        @Override
        public String getShortDescription() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public void filterRequest(Request request, Response response1) throws InternalErrorException, BadRequestException {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new InternalErrorException(e);
            }
        }

        @Override
        public void filterResponse(Request request, Response response) throws InternalErrorException, BadRequestException, ProcessingException {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new InternalErrorException(e);
            }
        }

        @Override
        public String getId() throws UnsupportedOperationException {
            return null;
        }

        @Override
        public String getName() throws UnsupportedOperationException {
            return null;
        }

        @Override
        public List<Integer> getVersion() throws UnsupportedOperationException {
            return null;
        }
    };

    @BeforeMethod
    public void setUp() {
        legoSet = Mockito.mock(LegoSet.class);
        buildable = Mockito.mock(Buildable.class);
        lego = new Lego(legoSet, executorService, executorService);
        dataSourceMap = new HashMap<>();
        dataSource = Mockito.mock(DataSource.class);
        dataSourceMap.put("sample", dataSource);
    }

    //this tests the buildResponse function and it's response to LegoSet.getBuildable
    @Test(groups = "a")
    public void testGetBuildableElement() throws Exception {
        LegoSet legoSetWithoutElements = Mockito.mock(LegoSet.class);
        LegoSet legoSetFaulty = Mockito.mock(LegoSet.class);
        boolean elementNotFoundExceptionThrown = false;
        boolean internalErrorExceptionThrown = false;


        //First setup the case where everything is supposed to work fine.
        Mockito.when(legoSet.getBuildable(request)).thenReturn(buildable);
        Mockito.when(buildable.getTimeout()).thenReturn(300l);
        Lego lego = new Lego(legoSet, executorService, executorService);
        lego.buildResponse(request, response);
        Mockito.verify(buildable).getRequiredDataSources(request);


        //Next test the case where element is not found
        Mockito.when(legoSetWithoutElements.getBuildable(request)).thenThrow(new ElementNotFoundException());
        lego = new Lego(legoSetWithoutElements, executorService, executorService);
        try {
            lego.buildResponse(request, response);
        } catch (ElementNotFoundException exception) {
            elementNotFoundExceptionThrown = true;
        }
        assert elementNotFoundExceptionThrown;
        Mockito.verify(buildable, Mockito.atMost(1)).getRequiredDataSources(request);


        //Next test the case where legoSet screws up or is faulty
        Mockito.when(legoSetFaulty.getBuildable(request)).thenThrow(new LegoSetException());
        lego = new Lego(legoSetFaulty, executorService, executorService);
        try {
            lego.buildResponse(request, response);
        } catch (InternalErrorException exception) {
            internalErrorExceptionThrown = true;
        }
        assert internalErrorExceptionThrown;
        Mockito.verify(buildable, Mockito.atMost(1)).getRequiredDataSources(request);


    }

    //this tests the buildResponse function and it's response to LegoSet.getRequiredDataSources
    @Test(groups = "a")
    public void testGetRequiredDataSources() throws Exception {
        Lego lego = new Lego(legoSet, executorService, executorService);
        boolean internalErrorExceptionThrown = false;

        //Setting up the case where getRequireDataSources returns valid data
        Mockito.when(legoSet.getBuildable(request)).thenReturn(buildable);
        Mockito.when(buildable.getTimeout()).thenReturn(300l);
        Mockito.when(buildable.getRequiredDataSources(request)).thenReturn(dataSourceMap);
        try {
            lego.buildResponse(request, response);
        } catch (Exception e) {
        }
        Mockito.verify(buildable).getOptionalDataSources(request);

        //Setting up the case where getRequiredDataSources throws and internalErrorException
        Mockito.when(buildable.getRequiredDataSources(request)).thenThrow(new InternalErrorException());
        try {
            lego.buildResponse(request, response);
        } catch (InternalErrorException exception) {
            internalErrorExceptionThrown = true;
        }
        assert internalErrorExceptionThrown;
    }

    //this tests the buildResponse function and it's response to LegoSet.getOptionalDataSources
    @Test(groups = "a")
    public void testGetOptionalDataSources() throws Exception {
        Mockito.when(legoSet.getBuildable(request)).thenReturn(buildable);
        Mockito.when(buildable.getRequiredDataSources(request)).thenReturn(dataSourceMap);
        boolean legoExceptionThrown = false;

        //Setting up the case where optional data sources are given without issue
        Mockito.when(buildable.getOptionalDataSources(request)).thenReturn(dataSourceMap);
        try {
            lego.buildResponse(request, response);
        } catch (Exception e) {
        }
        Mockito.verify(buildable, Mockito.atLeastOnce()).getTimeout();

        //Setting up the case where optional data sources throws exception
        Mockito.when(buildable.getOptionalDataSources(request)).thenThrow(new LegoException());
        try {
            lego.buildResponse(request, response);
        } catch (Exception e) {
        }
        Mockito.verify(buildable, Mockito.atLeast(2)).getTimeout();
    }

    //this tests the buildResponse function and it's dispatch data sources function
    @Test(groups = "a")
    public void testDispatchDataSources() throws Exception {
        Mockito.when(legoSet.getBuildable(request)).thenReturn(buildable);
        Mockito.when(buildable.getTimeout()).thenReturn(200l);
        Mockito.when(buildable.getRequiredDataSources(request)).thenReturn(dataSourceMap);
        Mockito.when(buildable.getOptionalDataSources(request)).thenReturn(dataSourceMap);
        Mockito.when(buildable.getFilters(request)).thenReturn(new LinkedHashSet<Filter>());
        Mockito.when(dataSource.call()).thenReturn(dataType);
        Map<String, Object> stringMap = new HashMap<>();
        stringMap.put("sample", dataType);


        try {
            lego.buildResponse(request, response);
        } catch (Exception e) {
            System.out.println(e);
        }

        Mockito.verify(buildable, Mockito.atLeastOnce()).build(request, response, stringMap);
    }

    //Testing whether buildResponse function times out appropriately and throws internal server exception
    @Test(groups = "b", dependsOnGroups = "a")
    public void testDataSourceTimeout() throws Exception {
        class SampleDataSource implements DataSource {

            public DataType call() {
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                }
                return dataType;
            }

            @Override
            public String getId() throws UnsupportedOperationException {
                return null;
            }

            @Override
            public String getName() throws UnsupportedOperationException {
                return null;
            }

            @Override
            public List<Integer> getVersion() throws UnsupportedOperationException {
                return null;
            }

            @Override
            public String getShortDescription() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }
        }
        boolean internalErrorExceptionThrown = false;
        DataSource dataSource1 = new SampleDataSource();
        Map<String, DataSource> dataSourceMap1 = new HashMap<>();
        dataSourceMap1.put("sample", dataSource1);
        LegoSet legoSet1 = Mockito.mock(LegoSet.class);
        Buildable buildable1 = Mockito.mock(Buildable.class);
        lego = new Lego(legoSet1, executorService, executorService);

        Mockito.when(legoSet1.getBuildable(request)).thenReturn(buildable1);
        Mockito.when(buildable1.getRequiredDataSources(request)).thenReturn(dataSourceMap1);
        Mockito.when(buildable1.getOptionalDataSources(request)).thenReturn(dataSourceMap1);
        Mockito.when(buildable1.getTimeout()).thenReturn(5000l);

        try {
            lego.buildResponse(request, response);
        } catch (InternalErrorException exception) {
            internalErrorExceptionThrown = true;
        }
        assert !internalErrorExceptionThrown;

        Mockito.when(buildable1.getTimeout()).thenReturn(1000l);
        try {
            lego.buildResponse(request, response);
        } catch (InternalErrorException exception) {
            internalErrorExceptionThrown = true;
        }
        assert internalErrorExceptionThrown;

        Mockito.verify(buildable1, Mockito.atLeastOnce()).getRequiredDataSources(request);
        Mockito.verify(buildable1, Mockito.atLeastOnce()).getOptionalDataSources(request);

        internalErrorExceptionThrown = false;
        Mockito.reset(buildable1);
        LinkedHashSet<Filter> filters = new LinkedHashSet<>();
        filters.add(sleepingFilter);
        Mockito.when(buildable1.getRequiredDataSources(request)).thenReturn(dataSourceMap1);
        Mockito.when(buildable1.getOptionalDataSources(request)).thenReturn(dataSourceMap1);
        Mockito.when(buildable1.getTimeout()).thenReturn(1000l);
        Mockito.when(buildable1.getFilters(request)).thenReturn(filters);

        try {
            lego.buildResponse(request, response);
        } catch (InternalErrorException exception) {
            internalErrorExceptionThrown = true;
        }
        assertTrue(internalErrorExceptionThrown);
        Mockito.verify(buildable1, Mockito.never()).getRequiredDataSources(request);
    }


}
