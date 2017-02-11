package icurves.recomposition;

import icurves.description.AbstractBasicRegion;
import icurves.description.AbstractCurve;
import icurves.abstractdescription.AbstractDescription;
import icurves.decomposition.DecompositionStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class BasicRecomposer implements Recomposer {

    private static final Logger log = LogManager.getLogger(BasicRecomposer.class);

    private RecompositionStrategy strategy;

    BasicRecomposer(RecompositionStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public List<RecompositionStep> recompose(List<DecompositionStep> decompSteps) {
        Map<AbstractBasicRegion, AbstractBasicRegion> matchedZones = new TreeMap<>(AbstractBasicRegion::compareTo);

        int numSteps = decompSteps.size();

        List<RecompositionStep> result = new ArrayList<>(numSteps);

        for (int i = numSteps - 1; i >= 0; i--) {
            if (i < numSteps - 1) {
                result.add(recomposeStep(decompSteps.get(i), result.get(numSteps - 2 - i), matchedZones));
            } else {
                result.add(recomposeFirstStep(decompSteps.get(i), matchedZones));
            }
        }

        log.info("Recomposition begin");
        result.forEach(log::info);
        log.trace("Matched zones: " + matchedZones);
        log.info("Recomposition end");

        return result;
    }

    /**
     * Recompose first step, which is also the last decomposition step.
     *
     * @param decompStep last decomposition step
     * @param matchedZones matched zones
     * @return first recomposition step
     */
    private RecompositionStep recomposeFirstStep(DecompositionStep decompStep,
                                                 Map<AbstractBasicRegion, AbstractBasicRegion> matchedZones) {

        AbstractCurve was_removed = decompStep.removed();
        List<RecompositionData> added_contour_data = new ArrayList<>();

        // make a new Abstract Description
        Set<AbstractCurve> contours = new TreeSet<>();
        AbstractBasicRegion outside_zone = AbstractBasicRegion.OUTSIDE;

        List<AbstractBasicRegion> split_zone = new ArrayList<>();
        List<AbstractBasicRegion> added_zone = new ArrayList<>();
        split_zone.add(outside_zone);
        added_contour_data.add(new RecompositionData(was_removed, split_zone, added_zone));

        contours.add(was_removed);
        AbstractBasicRegion new_zone = new AbstractBasicRegion(contours);

        Set<AbstractBasicRegion> new_zones = new TreeSet<>();
        new_zones.add(new_zone);
        new_zones.add(outside_zone);
        added_zone.add(new_zone);

        matchedZones.put(outside_zone, outside_zone);
        matchedZones.put(new_zone, new_zone);

        AbstractDescription from = decompStep.to();
        AbstractDescription to = new AbstractDescription(contours, new_zones);

        return new RecompositionStep(from, to, added_contour_data);
    }

    /**
     *
     * @param decompStep decomposition step
     * @param previous previous recomposition step
     * @param matchedZones matched zones
     * @return recomposition step
     */
    protected RecompositionStep recomposeStep(DecompositionStep decompStep, RecompositionStep previous,
            Map<AbstractBasicRegion, AbstractBasicRegion> matchedZones) {

        throw new UnsupportedOperationException("BasicRecomposer");

//        log.trace("Matched Zones: " + matchedZones);
//
//        // find the resulting zones in the previous step got to
//        List<AbstractBasicRegion> zonesToSplit = new ArrayList<>();
//
//        Map<AbstractBasicRegion, AbstractBasicRegion> zones_moved_during_decomp = decompStep.zonesMoved();
//        Collection<AbstractBasicRegion> zones_after_moved = zones_moved_during_decomp.values();
//
//        Map<AbstractBasicRegion, AbstractBasicRegion> matched_inverse = new HashMap<>();
//
//        Iterator<AbstractBasicRegion> moved_it = zones_after_moved.iterator();
//        while (moved_it.hasNext()) {
//            AbstractBasicRegion moved = moved_it.next();
//            AbstractBasicRegion to_split = matchedZones.get(moved);
//
//            matched_inverse.put(to_split, moved);
//
//            if (to_split != null) {
//                zonesToSplit.add(to_split);
//            } else {
//                throw new RuntimeException("match not found");
//            }
//        }
//
//        log.trace("Matched Inverse: " + matched_inverse);
//
//
//        AbstractDescription from = previous.to();
//        // Partition zonesToSplit
//        List<Cluster> clusters = strategy.makeClusters(zonesToSplit, from);
//
//        clusters.forEach(c -> log.trace("Cluster for recomposition: " + c));
//
//        Set<AbstractBasicRegion> newZoneSet = new TreeSet<>(from.getZones());
//        Set<AbstractCurve> newCurveSet = new TreeSet<>(from.getCurves());
//
//        AbstractCurve removedCurve = decompStep.removed();
//        List<RecompositionData> addedContourData = new ArrayList<>();
//
//        // for each cluster, make a curve with label
//        int i = 0;
//        for (Cluster cluster : clusters) {
//
//            List<AbstractBasicRegion> splitZones = new ArrayList<>();
//            List<AbstractBasicRegion> addedZones = new ArrayList<>();
//
//            AbstractCurve newCurve = (i > 0) ? removedCurve.split() : new AbstractCurve(removedCurve.getLabel());
//
//            //AbstractCurve newCurve = new AbstractCurve(removedCurve.getLabel());
//            newCurveSet.add(newCurve);
//
//            for (AbstractBasicRegion z : cluster.zones()) {
//                splitZones.add(z);
//                AbstractBasicRegion new_zone = z.moveInside(newCurve);
//
//                newZoneSet.add(new_zone);
//                addedZones.add(new_zone);
//
//                AbstractBasicRegion decomp_z = matched_inverse.get(z);
//
//                // TODO: adhoc solves problem but what does it do?
//                if (decomp_z == null) {
//                    decomp_z = z;
//                }
//
//                matchedZones.put(decomp_z.moveInside(removedCurve), new_zone);
//            }
//
//            addedContourData.add(new RecompositionData(newCurve, splitZones, addedZones));
//            i++;
//        }
//
//        AbstractDescription to = new AbstractDescription(newCurveSet, newZoneSet);
//
//        return new RecompositionStep(from, to, addedContourData);
    }
}
