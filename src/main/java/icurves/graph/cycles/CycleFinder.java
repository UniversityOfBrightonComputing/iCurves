package icurves.graph.cycles;

import java.util.*;
import java.util.stream.Collectors;

import icurves.graph.GraphCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;

/**
 * This is an adapted version from the following stackoverflow answer:
 * http://stackoverflow.com/questions/14146165/find-all-the-paths-forming-simple-cycles-on-an-undirected-graph
 * from 13 Jan 2013.
 */
public class CycleFinder<V, E> {

    private static final Logger log = LogManager.getLogger(CycleFinder.class);

    private UndirectedGraph<V, E> graph;
    private List<V> vertexList;
    private boolean adjMatrix[][];

    public CycleFinder(Class<E> type) {
        this.graph = new SimpleGraph<>(type);
        this.vertexList = new ArrayList<>();
    }

    public void addVertex(V vertex) {
        this.graph.addVertex(vertex);
        this.vertexList.add(vertex);
    }

    public void addEdge(V vertex1, V vertex2, E edge) {
        this.graph.addEdge(vertex1, vertex2, edge);
    }

    public UndirectedGraph<V, E> getGraph() {
        return graph;
    }

    public List<GraphCycle<V, E>> computeCycles() {
        List<GraphCycle<V, E>> graphCycles = new ArrayList<>();

        List<List<V> > cycles = getAllCycles();

        for (List<V> cycle : cycles) {
            List<E> edges = new ArrayList<>();

            for (int i = 0; i < cycle.size(); i++) {
                int j = i + 1 < cycle.size() ? i + 1 : 0;

                V v1 = cycle.get(i);
                V v2 = cycle.get(j);

                edges.add(graph.getEdge(v1, v2));
            }

            graphCycles.add(new GraphCycle<V, E>(cycle, edges));
        }

        Collections.sort(graphCycles, (c1, c2) -> c1.length() - c2.length());

        return graphCycles;
    }

    public List<List<V>> getAllCycles() {
        this.buildAdjancyMatrix();

        @SuppressWarnings("unchecked")
        V[] vertexArray = (V[]) this.vertexList.toArray();
        ElementaryCyclesSearch ecs = new ElementaryCyclesSearch(this.adjMatrix, vertexArray);

        @SuppressWarnings("unchecked")
        List<List<V>> cycles0 = ecs.getElementaryCycles();

        // remove cycles of size 2
        Iterator<List<V>> listIt = cycles0.iterator();
        while (listIt.hasNext()) {
            List<V> cycle = listIt.next();

            if (cycle.size() == 2) {
                listIt.remove();
            }
        }

        log.trace("Cleaned cycles of size 2");

        // remove repeated cycles (two cycles are repeated if they have the same vertex (no matter the order)
        List<List<V>> cycles1 = removeRepeatedLists(cycles0);

        return cycles1;
    }

    private void buildAdjancyMatrix() {
        Set<E> edges = this.graph.edgeSet();
        Integer nVertex = this.vertexList.size();
        this.adjMatrix = new boolean[nVertex][nVertex];

        for (E edge : edges) {
            V v1 = this.graph.getEdgeSource(edge);
            V v2 = this.graph.getEdgeTarget(edge);

            int i = this.vertexList.indexOf(v1);
            int j = this.vertexList.indexOf(v2);

            this.adjMatrix[i][j] = true;
            this.adjMatrix[j][i] = true;
        }
    }

    /* Here repeated lists are those with the same elements, no matter the order,
     * and it is assumed that there are no repeated elements on any of the lists*/
    private List<List<V>> removeRepeatedLists(List<List<V>> listOfLists) {
        log.trace("Removing repeated cycles");

        return listOfLists.stream()
                .map(Cycle::new)
                .distinct()
                .map(c -> c.vertices)
                .collect(Collectors.toList());

//        List<List<V>> inputListOfLists = new ArrayList<>(listOfLists);
//        List<List<V>> outputListOfLists = new ArrayList<>();

//        while (!inputListOfLists.isEmpty()) {
//            // get the first element
//            List<V> thisList = inputListOfLists.get(0);
//            // remove it
//            inputListOfLists.remove(0);
//            outputListOfLists.add(thisList);
//            // look for duplicates
//            Integer nEl = thisList.size();
//            Iterator<List<V>> listIt = inputListOfLists.iterator();
//            while (listIt.hasNext()) {
//                List<V> remainingList = listIt.next();
//
//                if (remainingList.size() == nEl) {
//                    if (remainingList.containsAll(thisList)) {
//                        listIt.remove();
//                    }
//                }
//            }
//
//        }
//
//        log.trace("Finished removing repeated cycles");
//
//        return outputListOfLists;

        //return listOfLists;
    }

    private static class Cycle<V> {
        private List<V> vertices;

        public Cycle(List<V> vertices) {
            this.vertices = vertices;
        }

        @Override
        public boolean equals(Object obj) {
            Cycle<V> other = (Cycle<V>) obj;

            if (vertices.size() != other.vertices.size())
                return false;

            List<V> copyVertices = new ArrayList<V>(vertices);

            for (V vertex : other.vertices) {
                copyVertices.removeIf(v -> v == vertex);
            }

            return copyVertices.isEmpty();
        }
    }
}