package icurves.abstractdual;

import icurves.abstractdescription.AbstractBasicRegion;
import icurves.abstractdescription.AbstractCurve;
import icurves.graph.GraphCycle;
import icurves.graph.cycles.CycleFinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the (there can only be one at abstract level) abstract dual graph of an Euler diagram.
 */
public class AbstractDualGraph {

    private static final Logger log = LogManager.getLogger(AbstractDualGraph.class);

    // we do not specify the edge factory because we instantiate edges ourselves
    private final UndirectedGraph<AbstractDualNode, AbstractDualEdge> graph = new SimpleGraph<>(AbstractDualEdge.class);

    private List<GraphCycle<AbstractDualNode, AbstractDualEdge> > cycles = new ArrayList<>();

    /**
     * Constructs a dual graph from given zones.
     *
     * @param zones the zones
     */
    public AbstractDualGraph(List<AbstractBasicRegion> zones) {
        // each zone becomes a node. We do not add directly to graph
        // because lists are easier to obtain powersets from for edges
        List<AbstractDualNode> nodes = zones.stream()
                .map(AbstractDualNode::new)
                .collect(Collectors.toList());

        // add nodes to the underlying graph
        nodes.forEach(graph::addVertex);

        // go through all pairs and see if they are neighbors.
        // Neighboring zones get edges added between them.
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                AbstractDualNode n = nodes.get(i);
                AbstractDualNode n2 = nodes.get(j);

                n.getZone().getStraddledContour(n2.getZone()).ifPresent(curve -> {
                    AbstractDualEdge e = new AbstractDualEdge(n, n2, curve);

                    graph.addEdge(e.from, e.to, e);
                });
            }
        }

        CycleFinder<AbstractDualNode, AbstractDualEdge> cycleFinder = new CycleFinder<>(AbstractDualEdge.class);
        graph.vertexSet().forEach(cycleFinder::addVertex);
        graph.edgeSet().forEach(edge -> cycleFinder.addEdge(edge.from, edge.to, edge));

        cycles = cycleFinder.computeCycles();
    }

    private AbstractDualGraph(Set<AbstractDualNode> nodes, Set<AbstractDualEdge> edges) {
        nodes.forEach(graph::addVertex);
        edges.forEach(e -> graph.addEdge(e.from, e.to, e));
    }

    public Optional<GraphCycle<AbstractDualNode, AbstractDualEdge> > computeCycle(List<AbstractBasicRegion> zones) {
        return cycles.stream()
                .filter(c -> c.getNodes().stream()
                        .map(AbstractDualNode::getZone)
                        .collect(Collectors.toList())
                        .containsAll(zones))
                .findFirst();
    }

    /**
     * @return true iff this graph is connected
     */
    public boolean isConnected() {
        return getNodes().size() <= 1 || !getNodes().stream().anyMatch(n -> graph.degreeOf(n) == 0);
    }

    public AbstractDualNode getNodeByZone(AbstractBasicRegion zone) {
        for (AbstractDualNode node : getNodes()) {
            if (node.getZone().equals(zone))
                return node;
        }

        throw new IllegalArgumentException("Graph does not have zone: " + zone);
    }

    /**
     * @return number of edges in this graph
     */
    public int getNumEdges() {
        return graph.edgeSet().size();
    }

    /**
     * @return set of nodes of this graph
     */
    public Set<AbstractDualNode> getNodes() {
        return graph.vertexSet();
    }

    /**
     * Removes edge from the graph.
     *
     * @param edge to remove
     */
    public void removeEdge(AbstractDualEdge edge) {
        graph.removeEdge(edge);
    }

    /**
     * Removes node from the graph.
     * Edges incident with the node are also removed.
     *
     * @param node to remove
     */
    public void removeNode(AbstractDualNode node) {
        graph.removeVertex(node);
    }

    public List<AbstractDualEdge> findShortestEdgePath(AbstractDualNode start, AbstractDualNode target) {
        List<AbstractDualEdge> path = DijkstraShortestPath.findPathBetween(graph, start, target);
        if (path == null)
            throw new RuntimeException("Failed to find path between: " + start + " and " + target);

        return path;
    }

    public List<AbstractDualNode> findShortestVertexPath(AbstractDualNode start, AbstractDualNode target) {
        if (start == null || target == null)
            throw new IllegalArgumentException("start and target cannot be null");

        List<AbstractDualNode> result = new ArrayList<>();
        result.add(start);

        AbstractDualNode prev = start;

        for (AbstractDualEdge edge : findShortestEdgePath(start, target)) {
            if (edge.from == prev) {
                prev = edge.to;
            } else {
                prev = edge.from;
            }

            result.add(prev);
        }

        // here prev == target

        //result.add(target);
        return result;
    }

    public List<AbstractDualNode> findShortestVertexPath(AbstractDualNode start, AbstractDualNode target, List<AbstractDualNode> busy) {
        Set<AbstractDualNode> nodes = new HashSet<>(getNodes());    // copy to be sure
        nodes.removeAll(busy);

        return new AbstractDualGraph(nodes, graph.edgeSet()
                .stream()
                .filter(e -> nodes.contains(e.from) && nodes.contains(e.to))
                .collect(Collectors.toSet()))
                .findShortestVertexPath(start, target);
    }

    public List<AbstractDualNode> findCycle(AbstractDualNode start, AbstractDualNode target) {
        List<AbstractDualNode> firstVertexPath = findShortestVertexPath(start, target);

        findShortestEdgePath(start, target).forEach(this::removeEdge);

        List<AbstractDualNode> secondVertexPath = findShortestVertexPath(start, target);

        secondVertexPath.remove(start);
        secondVertexPath.remove(target);

        Collections.reverse(secondVertexPath);
        firstVertexPath.addAll(secondVertexPath);

        return firstVertexPath;
    }

    /**
     * Returns edge from lowest degree node to its lowest degree neighbor.
     * Note this ignores isolated vertices since they do not have incident edges.
     *
     * @return edge
     */
    public AbstractDualEdge getLowDegreeEdge() {
        log.trace("Graph: " + graph);

        // find the lowest-degree vertex, and from that ...
        Optional<AbstractDualNode> lowestDegreeNode = graph.vertexSet()
                .stream()
                .filter(node -> graph.degreeOf(node) != 0)  // ignore isolated nodes when picking a low-degree edge
                .reduce((node1, node2) -> graph.degreeOf(node2) < graph.degreeOf(node1) ? node2 : node1);

        if (!lowestDegreeNode.isPresent())
            return null;

        // choose the edge to its lowest-degree neighbour
        AbstractDualNode node = lowestDegreeNode.get();

        Optional<AbstractDualEdge> lowestDegreeEdge = graph.edgesOf(node)
                .stream()
                .map(edge -> edge.from == node ? edge.to : edge.from)
                .reduce((node1, node2) -> graph.degreeOf(node2) < graph.degreeOf(node1) ? node2 : node1)
                .map(n -> graph.getEdge(n, node));

        return lowestDegreeEdge.get();
    }

    /**
     * @return 4 nodes that form a square with their edges
     */
    public List<AbstractDualNode> getFourTuple() {
        for (AbstractDualNode n : graph.vertexSet()) {
            for (AbstractDualEdge e : graph.edgesOf(n)) {
                if (e.from != n) {
                    continue;
                }

                AbstractDualNode n2 = e.to;
                for (AbstractDualEdge e2 : graph.edgesOf(n2)) {
                    if (e2.from != n2) {
                        continue;
                    }

                    // we have edges e and e2 - are these part of a square?
                    log.trace("Edges: " + e.from.getZone() + "->" + e.to.getZone() + " and " + e2.from.getZone() + "->" + e2.to.getZone());

                    // look for an edge from n with the same label (curve) as e2
                    for (AbstractDualEdge e3 : graph.edgesOf(n)) {

                        System.out.println(e3.curve + " VS " + e2.curve);

                        if (e3.curve.equals(e2.curve)) {
                            // found a square
                            ArrayList<AbstractDualNode> result = new ArrayList<>();
                            result.add(n);
                            result.add(n2);
                            result.add(e3.to);
                            result.add(e2.to);

                            System.out.println("Returning result: " + result);

                            return result;
                        }
                    }
                }
            }
        }

        return null;
    }

    public List<AbstractDualNode> getPotentialFourTuple(Set<AbstractBasicRegion> zones) {
        // search for a connected 4 node graph
        List<AbstractDualNode> tuple = getFourTuple();
        if (tuple != null)
            return tuple;

        List<AbstractDualNode> result = new ArrayList<>();

        List<AbstractDualNode> nodes = new ArrayList<>(graph.vertexSet());

        // search for a connected 3 node graph
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                AbstractDualNode n1 = nodes.get(i);
                AbstractDualNode n2 = nodes.get(j);

                List<AbstractDualNode> adjacent1 = getAdjacentNodes(n1);
                List<AbstractDualNode> adjacent2 = getAdjacentNodes(n2);

                adjacent1.retainAll(adjacent2);

                // we have found a node adjacent to both
                if (adjacent1.size() == 1) {
                    AbstractDualNode n3 = adjacent1.get(0);

                    getMissingZone(n1.getZone(), n2.getZone(), n3.getZone(), zones).ifPresent(zone -> {
                        result.add(n3);
                        result.add(n1);
                        result.add(n2);
                        result.add(new AbstractDualNode(zone));
                    });

                    if (result.size() == 4) {
                        return result;
                    }
                } else if (!adjacent1.isEmpty()) {
                    log.warn("Suspicious adjacency list for: " + n1 + " " + n2 + " list: " + adjacent1);
                }
            }
        }

        // search for a disconnected 2 node graph
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                AbstractDualNode n1 = nodes.get(i);
                AbstractDualNode n2 = nodes.get(j);

                List<AbstractDualNode> adjacent1 = getAdjacentNodes(n1);
                List<AbstractDualNode> adjacent2 = getAdjacentNodes(n2);

                adjacent1.retainAll(adjacent2);

                if (adjacent1.isEmpty()) {
                    for (AbstractBasicRegion zone : zones) {
                        Optional<AbstractCurve> curve1 = zone.getStraddledContour(n1.getZone());
                        Optional<AbstractCurve> curve2 = zone.getStraddledContour(n2.getZone());

                        // if there is a zone adjacent to both
                        // see if we can find 2nd to form a 4 cluster
                        if (curve1.isPresent() && curve2.isPresent()) {
                            getMissingZone(n1.getZone(), n2.getZone(), zone, zones).ifPresent(zone2 -> {
                                result.add(new AbstractDualNode(zone));
                                result.add(n1);
                                result.add(n2);
                                result.add(new AbstractDualNode(zone2));
                            });

                            if (result.size() == 4) {
                                return result;
                            }
                        }
                    }
                } else {
                    log.warn("Suspicious adjacency list for: " + n1 + " " + n2 + " list: " + adjacent1);
                }
            }
        }

        return null;
    }

    private List<AbstractDualNode> getAdjacentNodes(AbstractDualNode node) {
        return graph.edgesOf(node).stream()
                .map(edge -> edge.from == node ? edge.to : edge.from)
                .collect(Collectors.toList());
    }

    public boolean isAdjacent(AbstractDualNode node1, AbstractDualNode node2) {
        return graph.getEdge(node1, node2) != null;
    }

    public AbstractDualEdge getEdge(AbstractDualNode node1, AbstractDualNode node2) {
        return graph.getEdge(node1, node2);
    }

    private Optional<AbstractBasicRegion> getMissingZone(AbstractBasicRegion zone1, AbstractBasicRegion zone2, AbstractBasicRegion sameZone, Set<AbstractBasicRegion> zones) {
        log.trace("Checking for missing zone with " + zone1 + " " + zone2);

        for (AbstractBasicRegion zone : zones) {
            if (zone.equals(sameZone))
                continue;

            Optional<AbstractCurve> curve1 = zone.getStraddledContour(zone1);
            Optional<AbstractCurve> curve2 = zone.getStraddledContour(zone2);

            if (curve1.isPresent() && curve2.isPresent()) {
                return Optional.of(zone);
            }
        }

        return Optional.empty();
    }

    @Override
    public String toString() {
        return graph.toString();
    }
}
