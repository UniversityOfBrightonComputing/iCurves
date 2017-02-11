package icurves.recomposition;

import icurves.description.AbstractBasicRegion;
import icurves.description.AbstractCurve;
import icurves.abstractdescription.AbstractDescription;
import icurves.abstractdual.AbstractDualGraph;
import icurves.abstractdual.AbstractDualNode;
import icurves.decomposition.DecompositionStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class BetterBasicRecomposer extends BasicRecomposer {

    private static final Logger log = LogManager.getLogger(BetterBasicRecomposer.class);

    public BetterBasicRecomposer(RecompositionStrategy strategy) {
        super(strategy);
    }

    @Override
    protected RecompositionStep recomposeStep(DecompositionStep decompStep, RecompositionStep previous,
                                              Map<AbstractBasicRegion, AbstractBasicRegion> matchedZones) {

        log.trace("Matched Zones: " + matchedZones);

        // find the resulting zones in the previous step got to
        List<AbstractBasicRegion> zonesToSplit = new ArrayList<>();

        Map<AbstractBasicRegion, AbstractBasicRegion> zones_moved_during_decomp = decompStep.zonesMoved();
        Collection<AbstractBasicRegion> zones_after_moved = zones_moved_during_decomp.values();

        Map<AbstractBasicRegion, AbstractBasicRegion> matched_inverse = new HashMap<>();

        Iterator<AbstractBasicRegion> moved_it = zones_after_moved.iterator();
        while (moved_it.hasNext()) {
            AbstractBasicRegion moved = moved_it.next();

            //System.out.println("Moved: " + moved);

            AbstractBasicRegion to_split = matchedZones.get(moved);

            matched_inverse.put(to_split, moved);

            if (to_split != null) {
                zonesToSplit.add(to_split);
            } else {
                throw new RuntimeException("match not found");
                //zonesToSplit.add(moved);
            }
        }

        log.trace("Matched Inverse: " + matched_inverse);


        AbstractDescription from = previous.to();

        // zonesToSplit, from == in(k,D), D

        log.debug("Recomposing curve: " + decompStep.removed());
        log.debug("Zones to split (ORIGINAL): " + zonesToSplit);



//        AbstractDualGraph subGraph = new AbstractDualGraph(zonesToSplit);
//
//        if (subGraph.isConnected()) {
//            if (subGraph.getNodes().size() == 1 || subGraph.getNodes().size() == 2) {
//                // OK
//            } else if (isCycle(zonesToSplit)) {
//                // OK
//            } else if (isMultiPiercing(subGraph.getNodes().stream().collect(Collectors.toList()))) {
//                // OK
//            } else {
//                // FIX
//                //zonesToSplit = fix(zonesToSplit, from);
//                new AbstractDualGraph(new ArrayList<>(from.getZones()))
//                        .computeCycle(zonesToSplit)
//                        .ifPresent(cycle -> {
//                            System.out.println("Found appopriate cycle: " + cycle);
//
//                            zonesToSplit.clear();
//                            zonesToSplit.addAll(cycle.getNodes()
//                                    .stream()
//                                    .map(AbstractDualNode::getZone)
//                                    .collect(Collectors.toList()));
//                        });
//            }
//        } else {
//            // FIX
//            //zonesToSplit = fix(zonesToSplit, from);
//            new AbstractDualGraph(new ArrayList<>(from.getZones()))
//                    .computeCycle(zonesToSplit)
//                    .ifPresent(cycle -> {
//                        System.out.println("Found appopriate cycle: " + cycle);
//
//                        zonesToSplit.clear();
//                        zonesToSplit.addAll(cycle.getNodes()
//                                .stream()
//                                .map(AbstractDualNode::getZone)
//                                .collect(Collectors.toList()));
//                    });
//        }

        log.debug("Zones to split (FIXED): " + zonesToSplit);

        // MAKE STEP

        Set<AbstractBasicRegion> newZoneSet = new TreeSet<>(from.getZones());
        Set<AbstractCurve> newCurveSet = new TreeSet<>(from.getCurves());

        AbstractCurve removedCurve = decompStep.removed();
        List<RecompositionData> addedContourData = new ArrayList<>();

        List<AbstractBasicRegion> splitZones = new ArrayList<>();
        List<AbstractBasicRegion> addedZones = new ArrayList<>();

        AbstractCurve newCurve = new AbstractCurve(removedCurve.getLabel());
        newCurveSet.add(newCurve);

        for (AbstractBasicRegion z : zonesToSplit) {
            splitZones.add(z);
            AbstractBasicRegion new_zone = z.moveInside(newCurve);

            newZoneSet.add(new_zone);
            addedZones.add(new_zone);

            AbstractBasicRegion decomp_z = matched_inverse.get(z);

            // TODO: adhoc solves problem but what does it do?
            if (decomp_z == null) {
                decomp_z = z;
            }

            matchedZones.put(decomp_z.moveInside(removedCurve), new_zone);
        }

        addedContourData.add(new RecompositionData(newCurve, splitZones, addedZones));

        AbstractDescription to = new AbstractDescription(newCurveSet, newZoneSet);
        return new RecompositionStep(from, to, addedContourData);
    }

//    private RecompositionStep makeStep(AbstractCurve removedCurve, List<AbstractBasicRegion> zonesToSplit, AbstractDescription from) {
//
//    }

    private List<AbstractBasicRegion> fix(List<AbstractBasicRegion> zonesToSplit, AbstractDescription from) {
        AbstractDualGraph graph = new AbstractDualGraph(new ArrayList<>(from.getZones()));

        List<AbstractDualNode> nodesToSplit = new ArrayList<>();

        for (int i = 0; i < zonesToSplit.size(); i++) {
            int j = i + 1 < zonesToSplit.size() ? i + 1 : 0;

            AbstractBasicRegion zone1 = zonesToSplit.get(i);
            AbstractBasicRegion zone2 = zonesToSplit.get(j);

            log.debug("Searching path zones: " + zone1 + " " + zone2);

            AbstractDualNode node1 = graph.getNodeByZone(zone1);
            AbstractDualNode node2 = graph.getNodeByZone(zone2);

            try {
                // if last step then we check normally, else add busy zones
                List<AbstractDualNode> nodePath = j == 0
                        ? graph.findShortestVertexPath(node1, node2) : graph.findShortestVertexPath(node1, node2, nodesToSplit);

                log.debug("Found path between " + node1 + " and " + node2 + " Path: " + nodePath);

                nodesToSplit.addAll(nodePath);
                nodesToSplit.remove(node2);

                // if first, we keep it
                if (i == 0) {
                    nodePath.remove(node1);
                }

                nodePath.remove(node2);

                // remove visited edges and nodes
                graph.findShortestEdgePath(node1, node2).forEach(graph::removeEdge);
                nodePath.forEach(graph::removeNode);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());

                if (j == 0) {
                    // we know nodes are connected with a single chain, NOT a cycle
                    nodesToSplit.add(node1);

                    if (isMultiPiercing(nodesToSplit)) {

                        break;
                    }
                } else {
                    throw new RuntimeException(e.getMessage() + " Failed to chain: " + nodesToSplit);
                }
            }
        }

        return nodesToSplit.stream()
                .map(AbstractDualNode::getZone)
                .collect(Collectors.toList());
    }

    // TODO: this is only partially correct
    // we also need to ensure each zone is nested within previous
    private boolean isMultiPiercing(List<AbstractDualNode> nodes) {
        // they are ordered
        for (int i = 0; i < nodes.size() - 1; i++) {
            AbstractBasicRegion zone1 = nodes.get(i).getZone();
            AbstractBasicRegion zone2 = nodes.get(i+1).getZone();

            if (zone1.equals(AbstractBasicRegion.OUTSIDE)) {
                continue;
            }

            if (zone2.getNumCurves() != zone1.getNumCurves() + 1)
                return false;
        }

        System.out.println(nodes);

        return true;
    }

    private boolean isCycle(List<AbstractBasicRegion> zones) {
        // TODO: check cycle
        // NPE produce if none
        if (zones.size() == 4)
            return new AbstractDualGraph(zones).getFourTuple().stream().filter(n -> !zones.contains(n.getZone())).count() == 0;
        else
            return false;
    }
}
