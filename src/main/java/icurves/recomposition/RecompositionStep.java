package icurves.recomposition;

import icurves.description.Description;

import java.util.Iterator;
import java.util.List;

/**
 * Single recomposition step.
 * A valid step has following features:
 *
 * <ul>
 *     <li>The added contour data must be a single curve</li>
 *     <li>Added curve must NOT be present in previous description and must be present in the next one</li>
 * </ul>
 *
 * Note: umber of steps == number of curves.
 */
public final class RecompositionStep {

    private final Description from;
    private final Description to;
    private final List<RecompositionData> addedContourData;

    public RecompositionStep(Description from, Description to, List<RecompositionData> addedContourData) {
        this.from = from;
        this.to = to;
        this.addedContourData = addedContourData;

        if (this.addedContourData.isEmpty()) {
            throw new IllegalArgumentException("No added curve in recomp");
        }

        if (addedContourData.size() > 1)
            throw new IllegalArgumentException("More than 1 contour data in single step?");

        String label = addedContourData.get(0).getAddedCurve().getLabel();

        if (from.includesLabel(label))
            throw new IllegalArgumentException("Added curve already present");
        if (!to.includesLabel(label))
            throw new IllegalArgumentException("Added curve not present in next description");
    }

    /**
     * @return abstract description before this step
     */
    public Description from() {
        return from;
    }

    /**
     * @return abstract description after this step
     */
    public Description to() {
        return to;
    }

    public List<RecompositionData> getAddedContourData() {
        return addedContourData;
    }

    @Override
    public String toString() {
        return "R_Step[Data=" + addedContourData + ",From=" + from + " To=" + to + "]";
    }
}
