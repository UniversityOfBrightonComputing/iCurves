package icurves.decomposition;

import icurves.description.Description;

import java.util.List;

/**
 * Defines how an abstract description should be decomposed.
 */
public interface Decomposer {

    /**
     * Decomposes an abstract description into steps.
     *
     * @param description the abstract description
     * @return list of steps
     */
    List<DecompositionStep> decompose(Description description);
}
