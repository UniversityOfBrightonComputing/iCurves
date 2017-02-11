package icurves.recomposition;

import icurves.abstractdescription.AbstractDescription;

import java.util.Iterator;
import java.util.List;

/**
 * Single recomposition step. A valid step has following features:
 *
 * <ul>
 *     <li>The added contour data cannot be empty, so each step adds at least 1 curve</li>
 *     <li>Added curves (if more than 1) have to have same label</li>
 *     <li>Added curve must NOT be present in previous description and must be present in the next one</li>
 * </ul>
 *
 * Note: the last feature implies that all components (duplicates) of the curve are added in 1 step.
 * This in turn means number of steps == number of curves.
 */
public final class RecompositionStep {

    private final AbstractDescription from;
    private final AbstractDescription to;
    private final List<RecompositionData> addedContourData;

    public RecompositionStep(AbstractDescription from, AbstractDescription to, List<RecompositionData> addedContourData) {
        this.from = from;
        this.to = to;
        this.addedContourData = addedContourData;

        if (this.addedContourData.isEmpty()) {
            throw new IllegalArgumentException("No added curve in recomp");
        }

        String label = addedContourData.get(0).getAddedCurve().getLabel();
        for (RecompositionData data : addedContourData) {
            if (!data.getAddedCurve().matchesLabel(addedContourData.get(0).getAddedCurve()))
                throw new IllegalArgumentException("Mixed curves added in recomp");
        }

        if (from.includesLabel(label))
            throw new IllegalArgumentException("Added curve already present");
        if (!to.includesLabel(label))
            throw new IllegalArgumentException("Added curve not present in next description");
    }

    /**
     * @return abstract description before this step
     */
    public AbstractDescription from() {
        return from;
    }

    /**
     * @return abstract description after this step
     */
    public AbstractDescription to() {
        return to;
    }

    public List<RecompositionData> getAddedContourData() {
        return addedContourData;
    }

    public Iterator<RecompositionData> getRecompIterator() {
        return addedContourData.iterator();
    }

    @Override
    public String toString() {
        return "R_Step[Data=" + addedContourData + ",From=" + from + " To=" + to + "]";
    }
}
