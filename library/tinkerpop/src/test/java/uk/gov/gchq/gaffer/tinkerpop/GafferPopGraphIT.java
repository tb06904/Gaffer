/*
 * Copyright 2017-2023 Crown Copyright
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

package uk.gov.gchq.gaffer.tinkerpop;

import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.accumulostore.AccumuloProperties;
import uk.gov.gchq.gaffer.commonutil.StreamUtil;
import uk.gov.gchq.gaffer.data.elementdefinition.view.View;
import uk.gov.gchq.gaffer.graph.Graph;
import uk.gov.gchq.gaffer.tinkerpop.util.GafferPopTestUtil;
import uk.gov.gchq.gaffer.user.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import static uk.gov.gchq.gaffer.tinkerpop.util.GafferPopTestUtil.AUTH_1;
import static uk.gov.gchq.gaffer.tinkerpop.util.GafferPopTestUtil.AUTH_2;
import static uk.gov.gchq.gaffer.tinkerpop.util.GafferPopTestUtil.TEST_CONFIGURATION_1;
import static uk.gov.gchq.gaffer.tinkerpop.util.GafferPopTestUtil.TEST_CONFIGURATION_2;
import static uk.gov.gchq.gaffer.tinkerpop.util.GafferPopTestUtil.TEST_CONFIGURATION_3;
import static uk.gov.gchq.gaffer.tinkerpop.util.GafferPopTestUtil.getTestUser;

public class GafferPopGraphIT {
    public static final String VERTEX_1 = "1";
    public static final String VERTEX_2 = "2";
    public static final String SOFTWARE_NAME_GROUP = "software";
    public static final String PERSON_GROUP = "person";
    public static final String DEPENDS_ON_EDGE_GROUP = "dependsOn";
    public static final String CREATED_EDGE_GROUP = "created";
    public static final String NAME_PROPERTY = "name";
    public static final String WEIGHT_PROPERTY = "weight";

    private static final AccumuloProperties PROPERTIES = AccumuloProperties.loadStoreProperties(StreamUtil.openStream(GafferPopGraphIT.class, "/gaffer/store.properties"));

    @Test
    public void shouldConstructGafferPopGraphWithOnlyConfig() {
        // Given
        final User expectedUser = getTestUser(AUTH_1, AUTH_2);

        // When
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_2);

        // Then
        final Map<String, Object> variables = graph.variables().asMap();
        assertThat(variables.get(GafferPopGraphVariables.USER)).isEqualTo(expectedUser);

        final Map<String, String> opOptions = (Map<String, String>) variables.get(GafferPopGraphVariables.OP_OPTIONS);
        assertThat(opOptions).containsEntry("key1", "value1").containsEntry("key2", "value2").hasSize(2);
        assertThat(variables.size()).isEqualTo(3);
    }

    @Test
    public void shouldConstructGafferPopGraphWithConfigFile() {
        // Given
        final User expectedUser = getTestUser();

        // when
        final GafferPopGraph graph = GafferPopGraph.open(GafferPopGraphIT.class.getClassLoader().getResource("gafferpop-test.properties").getPath());

        // Then
        final Map<String, Object> variables = graph.variables().asMap();
        assertThat(variables.get(GafferPopGraphVariables.USER)).isEqualTo(expectedUser);

        final Map<String, String> opOptions = (Map<String, String>) variables.get(GafferPopGraphVariables.OP_OPTIONS);
        assertThat(opOptions).containsEntry("key1", "value1").hasSize(1);
        assertThat(variables.size()).isEqualTo(3);
    }

    @Test
    public void shouldConstructGafferPopGraph() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final User expectedUser = getTestUser(AUTH_1, AUTH_2);

        // When
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);

        // Then
        final Map<String, Object> variables = graph.variables().asMap();
        assertThat(variables.get(GafferPopGraphVariables.SCHEMA)).isEqualTo(gafferGraph.getSchema());
        assertThat(variables.get(GafferPopGraphVariables.USER)).isEqualTo(expectedUser);

        final Map<String, String> opOptions = (Map<String, String>) variables.get(GafferPopGraphVariables.OP_OPTIONS);
        assertThat(opOptions).containsEntry("key1", "value1").containsEntry("key2", "value2").hasSize(2);
        assertThat(variables.size()).isEqualTo(3);
    }

    @Test
    public void shouldThrowUnsupportedExceptionForNoGraphId() {

        // Given/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> new GafferPopGraph(TEST_CONFIGURATION_3))
            .withMessageMatching("gaffer.graphId property is required");
    }

    @Test
    public void shouldThrowUnsupportedExceptionForCompute() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);

        // When / Then
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> graph.compute());
    }

    @Test
    public void shouldThrowUnsupportedExceptionForComputeWithClass() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);

        // When / Then
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> graph.compute(GraphComputer.class));
    }

    @Test
    public void shouldThrowUnsupportedExceptionForTx() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);

        // When / Then
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> graph.tx());
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionForNoVertexLabel() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);

        //Then
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> graph.addVertex(T.id, VERTEX_1))
            .withMessageMatching("Label is required");
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionForNoVertexId() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);

        //Then
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> graph.addVertex(T.label, SOFTWARE_NAME_GROUP))
            .withMessageMatching("ID is required");
    }

    @Test
    public void shouldAddAndGetVertex() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);

        // When
        addSoftwareVertex(graph);
        final Iterator<GafferPopVertex> vertices = graph.vertices(Arrays.asList(VERTEX_1, VERTEX_2), SOFTWARE_NAME_GROUP);

        // Then
        testSoftwareVertex(vertices);
    }

    @Test
    public void shouldAddAndGetVertexWithNullViewAndVertexList() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);
        final Vertex vertex1 = addSoftwareVertex(graph);

        // When
        final Iterator<GafferPopVertex> vertices = graph.verticesWithView(Arrays.asList(vertex1), null);

        // Then
        testSoftwareVertex(vertices);
    }

    @Test
    public void shouldAddAndGetVertexWithNullView() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);

        // When
        addSoftwareVertex(graph);
        final Iterator<GafferPopVertex> vertices = graph.verticesWithView(Arrays.asList(VERTEX_1), null);

        // Then
        testSoftwareVertex(vertices);
    }

    @Test
    public void shouldAddAndGetVertexWithViewWithEdges() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);
        final View view = new View.Builder()
                .edge(SOFTWARE_NAME_GROUP)
                .build();

        // When
        addSoftwareVertex(graph);
        final Iterator<GafferPopVertex> vertices = graph.verticesWithView(Arrays.asList(), view);

        // Then
        testSoftwareVertex(vertices);
    }

    @Test
    public void shouldGetAllVertices() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);

        // When
        addSoftwareVertex(graph);
        graph.addVertex(T.label, PERSON_GROUP, T.id, VERTEX_2, NAME_PROPERTY, "Gaffer");
        final Iterator<Vertex> vertices = graph.vertices();

        // Then
        final List<Vertex> verticesList = new ArrayList<>();
        while (vertices.hasNext()) {
            verticesList.add(vertices.next());
        }
        assertThat(verticesList).contains(
                new GafferPopVertex(SOFTWARE_NAME_GROUP, VERTEX_1, graph),
                new GafferPopVertex(SOFTWARE_NAME_GROUP, VERTEX_2, graph)
        );
    }

    @Test
    public void shouldGetVerticesById() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);

        // When
        addSoftwareVertex(graph);
        final Iterator<Vertex> vertices = graph.vertices(VERTEX_1);

        // Then
        testSoftwareVertex(vertices);
    }

    @Test
    public void shouldGetAllVerticesInGroup() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);

        // When
        addSoftwareVertex(graph);
        graph.addVertex(T.label, PERSON_GROUP, T.id, VERTEX_2, NAME_PROPERTY, "Gaffer");
        final Iterator<GafferPopVertex> vertices = graph.vertices(null, SOFTWARE_NAME_GROUP);

        // Then
        final List<GafferPopVertex> verticesList = new ArrayList<>();
        while (vertices.hasNext()) {
            verticesList.add(vertices.next());
        }
        assertThat(verticesList).contains(
                    new GafferPopVertex(SOFTWARE_NAME_GROUP, VERTEX_1, graph)
        );
    }

    @Test
    public void shouldGetVertexWithJsonView() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);
        final View view = new View.Builder()
                .entity(SOFTWARE_NAME_GROUP)
                .build();

        // When
        graph.addVertex(T.label, SOFTWARE_NAME_GROUP, T.id, VERTEX_1);
        graph.addVertex(T.label, PERSON_GROUP, T.id, VERTEX_2);

        final Iterator<GafferPopVertex> vertices = graph.verticesWithView(Arrays.asList(VERTEX_1, VERTEX_2), view);

        // Then
        assertThat(vertices)
                .toIterable()
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("id", VERTEX_1)
                .hasFieldOrPropertyWithValue("label", SOFTWARE_NAME_GROUP);
    }

    @Test
    public void shouldAddAndGetEdge() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);
        final GafferPopVertex gafferPopOutVertex = new GafferPopVertex(GafferPopGraph.ID_LABEL, VERTEX_1, graph);
        final GafferPopVertex gafferPopInVertex = new GafferPopVertex(GafferPopGraph.ID_LABEL, VERTEX_2, graph);
        final GafferPopEdge edgeToAdd = new GafferPopEdge(CREATED_EDGE_GROUP, gafferPopOutVertex, gafferPopInVertex, graph);
        edgeToAdd.property(WEIGHT_PROPERTY, 1.5);

        // When
        graph.addEdge(edgeToAdd);
        final Iterator<Edge> edges = graph.edges(Arrays.asList(VERTEX_1, VERTEX_2));

        // Then
        final Edge edge = edges.next();
        assertThat(edges).isExhausted(); // there is only 1 vertex
        assertThat(((List) edge.id()).get(0)).isEqualTo(VERTEX_1);
        assertThat(((List) edge.id()).get(1)).isEqualTo(VERTEX_2);
        assertThat(edge.label()).isEqualTo(CREATED_EDGE_GROUP);
        assertThat(edge.inVertex()).isEqualTo(gafferPopInVertex);
        assertThat(edge.outVertex()).isEqualTo(gafferPopOutVertex);
        assertThat(edge.property(WEIGHT_PROPERTY).value()).isEqualTo(1.5);
    }

    @Test
    public void shouldGetEdgeInGroup() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);
        final GafferPopEdge edgeToAdd1 = new GafferPopEdge(CREATED_EDGE_GROUP, VERTEX_1, VERTEX_2, graph);
        graph.addEdge(edgeToAdd1);

        // When
        final Iterator<GafferPopEdge> edges = graph.edges(VERTEX_1, Direction.OUT, CREATED_EDGE_GROUP);

        // Then
        assertThat(edges).toIterable().contains(edgeToAdd1);
    }

    @Test
    public void shouldGetEdgeInGroupWithViewWithEntity() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);
        final GafferPopEdge edgeToAdd1 = new GafferPopEdge(CREATED_EDGE_GROUP, VERTEX_1, VERTEX_2, graph);
        graph.addEdge(edgeToAdd1);
        final View view = new View.Builder()
                .entity(CREATED_EDGE_GROUP)
                .build();

        // When
        final Iterator<GafferPopEdge> edges = graph.edgesWithView(VERTEX_1, Direction.OUT, view);

        // Then
        assertThat(edges).toIterable().contains(edgeToAdd1);
    }

    @Test
    public void shouldGetEdgesWithEdgeIdsPassedIn() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);
        final GafferPopEdge edgeToAdd1 = new GafferPopEdge(CREATED_EDGE_GROUP, VERTEX_1, VERTEX_2, graph);
        final GafferPopEdge edgeToAdd2 = new GafferPopEdge(DEPENDS_ON_EDGE_GROUP, VERTEX_2, VERTEX_1, graph);
        graph.addEdge(edgeToAdd1);
        graph.addEdge(edgeToAdd2);
        final View view = new View.Builder()
                .entity(CREATED_EDGE_GROUP)
                .build();

        // When
        final Iterator<GafferPopEdge> edges = graph.edgesWithView(Arrays.asList(edgeToAdd1.id(), edgeToAdd2.id()), Direction.OUT, view);

        // Then
        assertThat(edges).toIterable().contains(edgeToAdd1, edgeToAdd2);
    }
    @Test
    public void shouldGetEdgeInGroupWithNullView() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);
        final GafferPopEdge edgeToAdd1 = new GafferPopEdge(CREATED_EDGE_GROUP, VERTEX_1, VERTEX_2, graph);
        graph.addEdge(edgeToAdd1);

        // When
        final Iterator<GafferPopEdge> edges = graph.edgesWithView(VERTEX_1, Direction.OUT, null);

        // Then
        assertThat(edges).toIterable().contains(edgeToAdd1);
    }

    @Test
    public void shouldGetEdgeInGroupWithNullViewAndEdgeList() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);
        final GafferPopEdge edgeToAdd1 = new GafferPopEdge(CREATED_EDGE_GROUP, VERTEX_1, VERTEX_2, graph);
        graph.addEdge(edgeToAdd1);

        // When
        final Iterator<GafferPopEdge> edges = graph.edgesWithView(Arrays.asList(edgeToAdd1), Direction.OUT, null);

        // Then
        assertThat(edges).toIterable().contains(edgeToAdd1);
    }

    @Test
    public void shouldGetAllEdges() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);
        final GafferPopEdge edgeToAdd1 = new GafferPopEdge(CREATED_EDGE_GROUP, VERTEX_1, VERTEX_2, graph);
        final GafferPopEdge edgeToAdd2 = new GafferPopEdge(DEPENDS_ON_EDGE_GROUP, VERTEX_2, VERTEX_1, graph);
        graph.addEdge(edgeToAdd1);
        graph.addEdge(edgeToAdd2);

        // When
        final Iterator<Edge> edges = graph.edges();

        // Then
        assertThat(edges).toIterable().contains(edgeToAdd1, edgeToAdd2);
    }

    @Test
    public void shouldGetAllEdgesInGroup() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);
        final GafferPopEdge edgeToAdd1 = new GafferPopEdge(CREATED_EDGE_GROUP, VERTEX_1, VERTEX_2, graph);
        final GafferPopEdge edgeToAdd2 = new GafferPopEdge(DEPENDS_ON_EDGE_GROUP, VERTEX_2, VERTEX_1, graph);
        graph.addEdge(edgeToAdd1);
        graph.addEdge(edgeToAdd2);

        // When
        final Iterator<GafferPopEdge> edges = graph.edges(null, Direction.OUT, CREATED_EDGE_GROUP);

        // Then
        assertThat(edges).toIterable().contains(edgeToAdd1);
    }

    @Test
    public void shouldGetAdjacentVertices() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);
        final Vertex vertex1 = addSoftwareVertex(graph);
        final Vertex vertex2 = graph.addVertex(T.label, SOFTWARE_NAME_GROUP, T.id, VERTEX_2, NAME_PROPERTY, "Gaffer");
        vertex1.addEdge(DEPENDS_ON_EDGE_GROUP, vertex2);

        // When
        final Iterator<GafferPopVertex> vertices = graph.adjVertices(VERTEX_1, Direction.BOTH);

        // Then
        final GafferPopVertex vertex = vertices.next();
        assertThat(vertices).isExhausted(); // there is only 1 vertex
        assertThat(vertex.id()).isEqualTo(VERTEX_2);
        assertThat(vertex.label()).isEqualTo(SOFTWARE_NAME_GROUP);
        assertThat(vertex.property(NAME_PROPERTY).value()).isEqualTo("Gaffer");
    }

    @Test
    public void shouldGetAdjacentVerticesWithList() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);
        final GafferPopVertex vertex1 = (GafferPopVertex) addSoftwareVertex(graph);
        final GafferPopVertex vertex2 = (GafferPopVertex) graph.addVertex(T.label, SOFTWARE_NAME_GROUP, T.id, VERTEX_2, NAME_PROPERTY, "Gaffer");
        vertex1.addEdge(DEPENDS_ON_EDGE_GROUP, vertex2);

        // When
        final Iterator<GafferPopVertex> vertices = graph.adjVertices(Arrays.asList(VERTEX_1, VERTEX_2), Direction.BOTH);

        // Then
        assertThat(vertices).toIterable()
                .contains(vertex1)
                .contains(vertex2);
    }

    @Test
    public void shouldThrowExceptionIfGetAdjacentVerticesWithNoSeeds() {
        // Given
        final Graph gafferGraph = getGafferGraph();
        final GafferPopGraph graph = GafferPopGraph.open(TEST_CONFIGURATION_1, gafferGraph);

        // When / Then
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> graph.adjVertices(Collections.emptyList(), Direction.BOTH));
    }

    private Graph getGafferGraph() {
        return GafferPopTestUtil.getGafferGraph(this.getClass(), PROPERTIES);
    }

    private static Vertex addSoftwareVertex(GafferPopGraph graph) {
        return graph.addVertex(T.label, SOFTWARE_NAME_GROUP, T.id, VERTEX_1, NAME_PROPERTY, "GafferPop");
    }

    private static void testSoftwareVertex(Iterator<? extends Vertex> vertices) {
        assertThat(vertices)
                .toIterable()
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("id", VERTEX_1)
                .hasFieldOrPropertyWithValue("label", SOFTWARE_NAME_GROUP)
                .extracting(v -> v.property(NAME_PROPERTY).value())
                .isEqualTo("GafferPop");
    }
}
