package icurves.decomposition;

import icurves.abstractdescription.AbstractDescription;

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
    List<DecompositionStep> decompose(AbstractDescription description);
}
