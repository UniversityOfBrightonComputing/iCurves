package icurves.recomposition;

import icurves.decomposition.DecompositionStep;
import icurves.description.AbstractBasicRegion;
import icurves.description.AbstractCurve;
import icurves.description.Description;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BasicRecomposer implements Recomposer {

    private static final Logger log = LogManager.getLogger(BasicRecomposer.class);

    BasicRecomposer() {}

    @NotNull
    @Override
    public List<RecompositionStep> recompose(List<? extends DecompositionStep> decompSteps) {
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

        // make a new Abstract Description
        Set<AbstractCurve> contours = new TreeSet<>();
        AbstractBasicRegion outside_zone = AbstractBasicRegion.OUTSIDE;

        List<AbstractBasicRegion> split_zone = new ArrayList<>();
        List<AbstractBasicRegion> added_zone = new ArrayList<>();
        split_zone.add(outside_zone);

        contours.add(was_removed);
        AbstractBasicRegion new_zone = new AbstractBasicRegion(contours);

        Set<AbstractBasicRegion> new_zones = new TreeSet<>();
        new_zones.add(new_zone);
        new_zones.add(outside_zone);
        added_zone.add(new_zone);

        matchedZones.put(outside_zone, outside_zone);
        matchedZones.put(new_zone, new_zone);

        Description from = decompStep.to();
        Description to = new Description(contours, new_zones);

        return new RecompositionStep(from, to, new RecompositionData(was_removed, split_zone, added_zone));
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

        log.trace("Matched Zones: " + matchedZones);

        // find the resulting zones in the previous step got to
        List<AbstractBasicRegion> zonesToSplit = new ArrayList<>();

        Map<AbstractBasicRegion, AbstractBasicRegion> zones_moved_during_decomp = decompStep.zonesMoved();
        Collection<AbstractBasicRegion> zones_after_moved = zones_moved_during_decomp.values();

        Map<AbstractBasicRegion, AbstractBasicRegion> matched_inverse = new HashMap<>();

        Iterator<AbstractBasicRegion> moved_it = zones_after_moved.iterator();
        while (moved_it.hasNext()) {
            AbstractBasicRegion moved = moved_it.next();

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

        Description from = previous.getTo();

        // zonesToSplit, from == in(k,D), D

        log.debug("Recomposing curve: " + decompStep.removed());
        log.debug("Zones to split (ORIGINAL): " + zonesToSplit);

        log.debug("Zones to split (FIXED): " + zonesToSplit);

        // MAKE STEP

        Set<AbstractBasicRegion> newZoneSet = new TreeSet<>(from.getZones());
        Set<AbstractCurve> newCurveSet = new TreeSet<>(from.getCurves());

        AbstractCurve removedCurve = decompStep.removed();

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

        Description to = new Description(newCurveSet, newZoneSet);
        return new RecompositionStep(from, to, new RecompositionData(newCurve, splitZones, addedZones));
    }
}
