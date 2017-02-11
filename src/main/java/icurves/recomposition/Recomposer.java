package icurves.recomposition;

import icurves.decomposition.DecompositionStep;

import java.util.List;

public interface Recomposer {
    List<RecompositionStep> recompose(List<DecompositionStep> decompositionSteps);
}
