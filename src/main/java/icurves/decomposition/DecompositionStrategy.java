package icurves.decomposition;

import icurves.description.AbstractCurve;
import icurves.description.Description;

import java.util.List;

/**
 * Defines a strategy used to choose which curves to remove in the next step
 * given abstract description.
 */
public interface DecompositionStrategy {

    /**
     * Returns a list of curves to be removed from abstract description in the next step.
     *
     * @param description the abstract description
     * @return list of curves to remove
     */
    List<AbstractCurve> curvesToRemove(Description description);
}
