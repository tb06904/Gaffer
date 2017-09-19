/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.gaffer.store.operation.handler.function;

import org.junit.Test;
import uk.gov.gchq.gaffer.commonutil.TestGroups;
import uk.gov.gchq.gaffer.commonutil.TestPropertyNames;
import uk.gov.gchq.gaffer.commonutil.stream.Streams;
import uk.gov.gchq.gaffer.data.element.Edge;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.data.element.Entity;
import uk.gov.gchq.gaffer.data.element.function.ElementFilter;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.operation.impl.function.Filter;
import uk.gov.gchq.gaffer.store.Context;
import uk.gov.gchq.gaffer.store.Store;
import uk.gov.gchq.koryphe.impl.predicate.IsMoreThan;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class FilterHandlerTest {

    @Test
    public void shouldFilterByGroup() throws OperationException {
        // Given
        final List<Element> input = new ArrayList<>();
        final List<Element> expected = new ArrayList<>();

        final Store store = mock(Store.class);
        final Context context = new Context();
        final FilterHandler handler = new FilterHandler();

        final Edge edge = new Edge.Builder()
                .group(TestGroups.EDGE)
                .source("junctionA")
                .dest("junctionB")
                .directed(true)
                .property(TestPropertyNames.COUNT, 2L)
                .build();

        final Edge edge1 = new Edge.Builder()
                .group(TestGroups.EDGE_2)
                .source("junctionA")
                .dest("junctionB")
                .directed(true)
                .property(TestPropertyNames.COUNT, 1L)
                .build();

        final Edge edge2 = new Edge.Builder()
                .group(TestGroups.EDGE)
                .source("junctionB")
                .dest("junctionA")
                .directed(true)
                .property(TestPropertyNames.COUNT, 4L)
                .build();

        final Edge edge3 = new Edge.Builder()
                .group(TestGroups.EDGE_3)
                .source("junctionC")
                .dest("junctionB")
                .directed(true)
                .property(TestPropertyNames.COUNT, 3L)
                .build();

        input.add(edge);
        input.add(edge1);
        input.add(edge2);
        input.add(edge3);

        expected.add(edge1);

        final Filter filter = new Filter.Builder()
                .input(input)
                .edge(TestGroups.EDGE_2)
                .build();

        // When
        final Iterable<? extends Element> results = handler.doOperation(filter, context, store);
        final List<Element> resultsList = Streams.toStream(results).collect(Collectors.toList());

        // Then
        assertEquals(expected, resultsList);
    }

    @Test
    public void shouldFilterInputBasedOnGroupAndCount() throws OperationException {
        // Given
        final List<Element> input = new ArrayList<>();
        final List<Element> expected = new ArrayList<>();

        final Store store = mock(Store.class);
        final Context context = new Context();
        final FilterHandler handler = new FilterHandler();

        final Edge edge = new Edge.Builder()
                .group(TestGroups.EDGE)
                .source("junctionA")
                .dest("junctionB")
                .directed(true)
                .property(TestPropertyNames.COUNT, 2L)
                .build();

        final Edge edge1 = new Edge.Builder()
                .group(TestGroups.EDGE)
                .source("junctionA")
                .dest("junctionB")
                .directed(true)
                .property(TestPropertyNames.COUNT, 1L)
                .build();

        final Edge edge2 = new Edge.Builder()
                .group(TestGroups.EDGE)
                .source("junctionB")
                .dest("junctionA")
                .directed(true)
                .property(TestPropertyNames.COUNT, 4L)
                .build();

        final Edge edge3 = new Edge.Builder()
                .group(TestGroups.EDGE_2)
                .source("junctionC")
                .dest("junctionD")
                .directed(true)
                .property(TestPropertyNames.COUNT, 3L)
                .build();

        input.add(edge);
        input.add(edge1);
        input.add(edge2);
        input.add(edge3);

        expected.add(edge);
        expected.add(edge2);

        final Filter filter = new Filter.Builder()
                .input(input)
                .edge(TestGroups.EDGE, new ElementFilter.Builder()
                        .select(TestPropertyNames.COUNT)
                        .execute(new IsMoreThan(1L))
                        .build())
                .build();

        // When
        final Iterable<? extends Element> result = handler.doOperation(filter, context, store);
        final List<Element> resultList = Streams.toStream(result).collect(Collectors.toList());

        // Then
        assertEquals(expected, resultList);
    }

    @Test
    public void shouldReturnNoValuesWithNullElementFilter() throws OperationException {
        // Given
        final List<Element> input = new ArrayList<>();
        final List<Element> expected = new ArrayList<>();

        final Store store = mock(Store.class);
        final Context context = new Context();
        final FilterHandler handler = new FilterHandler();

        final Edge edge = new Edge.Builder()
                .group(TestGroups.EDGE)
                .source("junctionA")
                .dest("junctionB")
                .directed(true)
                .property(TestPropertyNames.COUNT, 2L)
                .build();

        final Edge edge1 = new Edge.Builder()
                .group(TestGroups.EDGE)
                .source("junctionA")
                .dest("junctionB")
                .directed(true)
                .property(TestPropertyNames.COUNT, 1L)
                .build();

        final Edge edge2 = new Edge.Builder()
                .group(TestGroups.EDGE)
                .source("junctionB")
                .dest("junctionA")
                .directed(true)
                .property(TestPropertyNames.COUNT, 4L)
                .build();

        input.add(edge);
        input.add(edge1);
        input.add(edge2);

        final Filter filter = new Filter.Builder()
                .input(input)
                .build();

        // When
        final Iterable<? extends Element> results = handler.doOperation(filter, context, store);
        final List<Element> resultsList = Streams.toStream(results).collect(Collectors.toList());

        // Then
        assertEquals(expected, resultsList);
    }

    @Test
    public void shouldFilterEntitiesAndEdges() throws OperationException {
        // Given
        final List<Element> input = new ArrayList<>();
        final List<Element> expected = new ArrayList<>();

        final Store store = mock(Store.class);
        final Context context = new Context();
        final FilterHandler handler = new FilterHandler();

        final Edge edge = new Edge.Builder()
                .group(TestGroups.EDGE)
                .source("junctionA")
                .dest("junctionB")
                .directed(true)
                .property(TestPropertyNames.COUNT, 2L)
                .build();

        final Edge edge1 = new Edge.Builder()
                .group(TestGroups.EDGE_2)
                .source("junctionA")
                .dest("junctionB")
                .directed(true)
                .property(TestPropertyNames.COUNT, 1L)
                .build();

        final Edge edge2 = new Edge.Builder()
                .group(TestGroups.EDGE)
                .source("junctionB")
                .dest("junctionA")
                .directed(true)
                .property(TestPropertyNames.COUNT, 4L)
                .build();

        final Entity entity = new Entity.Builder()
                .group(TestGroups.ENTITY)
                .property(TestPropertyNames.COUNT, 3L)
                .build();

        final Entity entity1 = new Entity.Builder()
                .group(TestGroups.ENTITY_2)
                .property(TestPropertyNames.COUNT, 4L)
                .build();

        input.add(edge);
        input.add(edge1);
        input.add(edge2);
        input.add(entity);
        input.add(entity1);

        expected.add(edge2);
        expected.add(entity);
        expected.add(entity1);

        final Filter filter = new Filter.Builder()
                .input(input)
                .globalElements(new ElementFilter.Builder()
                                        .select(TestPropertyNames.COUNT)
                                        .execute(new IsMoreThan(2L))
                                        .build())
                .build();

        // When
        final Iterable<? extends Element> results = handler.doOperation(filter, context, store);
        final List<Element> resultsList = Streams.toStream(results).collect(Collectors.toList());

        // Then
        assertEquals(expected, resultsList);
    }

    @Test
    public void shouldHandleComplexFiltering() throws OperationException {
        // Given
        final List<Element> input = new ArrayList<>();
        final List<Element> expected = new ArrayList<>();

        final Store store = mock(Store.class);
        final Context context = new Context();
        final FilterHandler handler = new FilterHandler();

        final Edge edge = new Edge.Builder()
                .group(TestGroups.EDGE)
                .source("junctionA")
                .dest("junctionB")
                .directed(true)
                .property(TestPropertyNames.COUNT, 2L)
                .build();

        final Edge edge1 = new Edge.Builder()
                .group(TestGroups.EDGE_2)
                .source("junctionA")
                .dest("junctionB")
                .directed(true)
                .property(TestPropertyNames.COUNT, 1L)
                .build();

        final Edge edge2 = new Edge.Builder()
                .group(TestGroups.EDGE)
                .source("junctionB")
                .dest("junctionA")
                .directed(true)
                .property(TestPropertyNames.COUNT, 4L)
                .build();

        final Entity entity = new Entity.Builder()
                .group(TestGroups.ENTITY)
                .property(TestPropertyNames.COUNT, 3L)
                .build();

        final Entity entity1 = new Entity.Builder()
                .group(TestGroups.ENTITY_2)
                .property(TestPropertyNames.COUNT, 4L)
                .build();

        final Entity entity2 = new Entity.Builder()
                .group(TestGroups.ENTITY_3)
                .property(TestPropertyNames.COUNT, 6L)
                .build();

        final Filter filter = new Filter.Builder()
                .input(input)
                .globalElements(new ElementFilter.Builder()
                                        .select(TestPropertyNames.COUNT)
                                        .execute(new IsMoreThan(1L))
                                        .build())
                .edge(TestGroups.EDGE, new ElementFilter.Builder()
                        .select(TestPropertyNames.COUNT)
                        .execute(new IsMoreThan(2L))
                        .build())
                .entity(TestGroups.ENTITY_2)
                .build();

        input.add(edge);
        input.add(edge1);
        input.add(edge2);
        input.add(entity);
        input.add(entity1);
        input.add(entity2);

        expected.add(edge2);
        expected.add(entity1);

        // When
        final Iterable<? extends Element> results = handler.doOperation(filter, context, store);
        final List<Element> resultsList = Streams.toStream(results).collect(Collectors.toList());

        // Then
        assertEquals(expected, resultsList);
    }

    @Test
    public void shouldThrowErrorForNullInput() {
        // Given
        final Store store = mock(Store.class);
        final Context context = new Context();
        final FilterHandler handler = new FilterHandler();

        final Filter filter = new Filter.Builder()
                .globalElements(new ElementFilter())
                .build();

        // When / Then
        try {
            final Iterable<? extends Element> results = handler.doOperation(filter, context, store);
            fail("Exception expected");
        } catch (OperationException e) {
            assertEquals("Filter operation has null iterable of elements", e.getMessage());
        }
    }

    // todo - another test where a global fails but subsequent "should" pass

    @Test
    public void shouldReturnNoResultsWhenGlobalElementsFails() throws OperationException {
        // Given
        final List<Element> input = new ArrayList<>();
        final List<Element> expected = new ArrayList<>();

        final Store store = mock(Store.class);
        final Context context = new Context();
        final FilterHandler handler = new FilterHandler();

        final Edge edge = new Edge.Builder()
                .group(TestGroups.EDGE)
                .source("junctionA")
                .dest("junctionB")
                .directed(true)
                .property(TestPropertyNames.COUNT, 2L)
                .build();

        final Edge edge1 = new Edge.Builder()
                .group(TestGroups.EDGE_2)
                .source("junctionA")
                .dest("junctionB")
                .directed(true)
                .property(TestPropertyNames.COUNT, 1L)
                .build();

        final Edge edge2 = new Edge.Builder()
                .group(TestGroups.EDGE)
                .source("junctionB")
                .dest("junctionA")
                .directed(true)
                .property(TestPropertyNames.COUNT, 4L)
                .build();

        final Entity entity = new Entity.Builder()
                .group(TestGroups.ENTITY)
                .property(TestPropertyNames.COUNT, 3L)
                .build();

        final Entity entity1 = new Entity.Builder()
                .group(TestGroups.ENTITY_2)
                .property(TestPropertyNames.COUNT, 4L)
                .build();

        final Entity entity2 = new Entity.Builder()
                .group(TestGroups.ENTITY_3)
                .property(TestPropertyNames.COUNT, 6L)
                .build();

        input.add(edge);
        input.add(edge1);
        input.add(edge2);
        input.add(entity);
        input.add(entity1);
        input.add(entity2);

        final Filter filter = new Filter.Builder()
                .input(input)
                .globalElements(new ElementFilter.Builder()
                                .select(TestPropertyNames.COUNT)
                                .execute(new IsMoreThan(10L))
                                .build())
                .globalEdges(new ElementFilter.Builder()
                             .select(TestPropertyNames.COUNT)
                             .execute(new IsMoreThan(2L))
                             .build())
                .build();

        // When
        final Iterable<? extends Element> results = handler.doOperation(filter, context, store);
        final List<Element> resultsList = Streams.toStream(results).collect(Collectors.toList());

        // Then
        assertEquals(expected, resultsList);
    }
}
