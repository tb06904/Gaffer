/*
 * Copyright 2016 Crown Copyright
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

package uk.gov.gchq.gaffer.data.element;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class LazyEdgeTest {
    @Test
    public void shouldLoadPropertyFromLazyProperties() {
        // Given
        final Edge edge = new Edge.Builder().build();
        final ElementValueLoader edgeLoader = mock(ElementValueLoader.class);
        final LazyEdge lazyEdge = new LazyEdge(edge, edgeLoader);
        final String propertyName = "property name";
        final String exceptedPropertyValue = "property value";

        given(edgeLoader.getProperty(propertyName, lazyEdge.getProperties())).willReturn(exceptedPropertyValue);

        // When
        Object propertyValue = lazyEdge.getProperty(propertyName);

        // Then
        assertEquals(exceptedPropertyValue, propertyValue);
    }

    @Test
    public void shouldLoadIdentifierWhenNotLoaded() {
        // Given
        final Edge edge = new Edge.Builder().build();
        final ElementValueLoader edgeLoader = mock(ElementValueLoader.class);
        final LazyEdge lazyEdge = new LazyEdge(edge, edgeLoader);
        final IdentifierType identifierType = IdentifierType.DESTINATION;
        final String exceptedIdentifierValue = "identifier value";

        given(edgeLoader.getIdentifier(Mockito.eq(identifierType), Mockito.any(LazyEdge.class))).willReturn(exceptedIdentifierValue);

        // When
        Object identifierValue = lazyEdge.getIdentifier(identifierType);

        // Then
        assertEquals(exceptedIdentifierValue, identifierValue);
        assertEquals(identifierValue, edge.getDestination());
    }

    @Test
    public void shouldNotLoadIdentifierWhenLoaded() {
        // Given
        final Edge edge = new Edge.Builder().build();
        final ElementValueLoader edgeLoader = mock(ElementValueLoader.class);
        final LazyEdge lazyEdge = new LazyEdge(edge, edgeLoader);
        final IdentifierType identifierType = IdentifierType.SOURCE;
        final String exceptedIdentifierValue = "identifier value";
        lazyEdge.setSource(exceptedIdentifierValue);

        // When - should use the loaded value
        Object identifierValue = lazyEdge.getIdentifier(identifierType);
        Object identifierValue2 = lazyEdge.getIdentifier(identifierType);

        // Then
        assertEquals(exceptedIdentifierValue, identifierValue);
        assertEquals(exceptedIdentifierValue, identifierValue2);
        assertEquals(exceptedIdentifierValue, edge.getSource());
        verify(edgeLoader, never()).getIdentifier(identifierType, lazyEdge);
    }

    @Test
    public void shouldNotLoadIsDirectedWhenLoaded() {
        // Given
        final Edge edge = new Edge.Builder().build();
        final ElementValueLoader edgeLoader = mock(ElementValueLoader.class);
        final LazyEdge lazyEdge = new LazyEdge(edge, edgeLoader);
        lazyEdge.setDirected(true); // call it to load the value.

        // When
        boolean isDirected = lazyEdge.isDirected();
        boolean isDirected2 = lazyEdge.isDirected();

        // Then
        assertTrue(isDirected);
        assertTrue(isDirected2);
        verify(edgeLoader, never()).getIdentifier(IdentifierType.DIRECTED, lazyEdge);
    }

    @Test
    public void shouldDelegatePutPropertyToLazyProperties() {
        // Given
        final Edge edge = new Edge.Builder().build();
        final ElementValueLoader edgeLoader = mock(ElementValueLoader.class);
        final LazyEdge lazyEdge = new LazyEdge(edge, edgeLoader);
        final String propertyName = "property name";
        final String propertyValue = "property value";

        // When
        lazyEdge.putProperty(propertyName, propertyValue);

        // Then
        assertEquals(propertyValue, edge.getProperty(propertyName));
        assertEquals(propertyValue, lazyEdge.getProperty(propertyName));
    }

    @Test
    public void shouldDelegateSetDestinationToEdge() {
        // Given
        final Edge edge = new Edge.Builder().build();
        final ElementValueLoader edgeLoader = mock(ElementValueLoader.class);
        final LazyEdge lazyEdge = new LazyEdge(edge, edgeLoader);
        final IdentifierType identifierType = IdentifierType.DESTINATION;
        final String destVertex = "dest vertex";

        // When
        lazyEdge.setDestination(destVertex);

        // Then
        verify(edgeLoader, never()).getIdentifier(identifierType, lazyEdge);
        assertEquals(destVertex, edge.getDestination());
    }

    @Test
    public void shouldDelegateSetSourceToEdge() {
        // Given
        final Edge edge = new Edge.Builder().build();
        final ElementValueLoader edgeLoader = mock(ElementValueLoader.class);
        final LazyEdge lazyEdge = new LazyEdge(edge, edgeLoader);
        final IdentifierType identifierType = IdentifierType.SOURCE;
        final String sourceVertex = "source vertex";

        // When
        lazyEdge.setSource(sourceVertex);

        // Then
        verify(edgeLoader, never()).getIdentifier(identifierType, lazyEdge);
        assertEquals(sourceVertex, edge.getSource());
    }

    @Test
    public void shouldDelegateSetDirectedToEdge() {
        // Given
        final Edge edge = new Edge.Builder().build();
        final ElementValueLoader edgeLoader = mock(ElementValueLoader.class);
        final LazyEdge lazyEdge = new LazyEdge(edge, edgeLoader);
        final IdentifierType identifierType = IdentifierType.DIRECTED;
        final boolean isDirected = true;

        // When
        lazyEdge.setDirected(isDirected);

        // Then
        verify(edgeLoader, never()).getIdentifier(identifierType, lazyEdge);
        assertEquals(isDirected, edge.isDirected());
    }

    @Test
    public void shouldDelegateGetGroupToEdge() {
        // Given
        final ElementValueLoader edgeLoader = mock(ElementValueLoader.class);
        final String group = "group";
        final Edge edge = new Edge.Builder().group(group).build();
        final LazyEdge lazyEdge = new LazyEdge(edge, edgeLoader);

        // When
        final String groupResult = lazyEdge.getGroup();

        // Then
        assertEquals(group, groupResult);
    }

    @Test
    public void shouldGetLazyProperties() {
        // Given
        final Edge edge = new Edge.Builder().build();
        final ElementValueLoader edgeLoader = mock(ElementValueLoader.class);
        final LazyEdge lazyEdge = new LazyEdge(edge, edgeLoader);

        // When
        final LazyProperties result = lazyEdge.getProperties();

        // Then
        assertNotNull(result);
        assertNotSame(edge.getProperties(), result);
    }

    @Test
    public void shouldUnwrapEdge() {
        // Given
        final Edge edge = new Edge.Builder().build();
        final ElementValueLoader edgeLoader = mock(ElementValueLoader.class);
        final LazyEdge lazyEdge = new LazyEdge(edge, edgeLoader);

        // When
        final Edge result = lazyEdge.getElement();

        // Then
        assertSame(edge, result);
    }
}
