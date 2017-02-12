package icurves.recomposition;

import icurves.description.Description;

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
    private final RecompositionData addedCurveData;

    public RecompositionStep(Description from, Description to, RecompositionData addedCurveData) {
        this.from = from;
        this.to = to;
        this.addedCurveData = addedCurveData;

        String label = addedCurveData.getAddedCurve().getLabel();

        if (from.includesLabel(label))
            throw new IllegalArgumentException("Added curve already present");
        if (!to.includesLabel(label))
            throw new IllegalArgumentException("Added curve not present in next description");
    }

    /**
     * @return description before this step
     */
    public Description from() {
        return from;
    }

    /**
     * @return description after this step
     */
    public Description to() {
        return to;
    }

    /**
     * @return how the curve was added
     */
    public RecompositionData getAddedCurveData() {
        return addedCurveData;
    }

    @Override
    public String toString() {
        return "R_Step[Data=" + addedCurveData + ",From=" + from + " To=" + to + "]";
    }
}
