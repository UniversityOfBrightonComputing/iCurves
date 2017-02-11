package icurves.util;

import icurves.decomposition.DecompositionStrategyType;
import icurves.recomposition.RecompositionStrategyType;

public class ExampleDiagram {

    public String description;
    public DecompositionStrategyType decompStrategy;
    public RecompositionStrategyType recompStrategy;
    public double expectedChecksum;

    public ExampleDiagram(String string,
                          DecompositionStrategyType decompStrategy,
                          RecompositionStrategyType recompStrategy,
                          double checksum) {
        description = string;
        this.decompStrategy = decompStrategy;
        this.recompStrategy = recompStrategy;
        expectedChecksum = checksum;
    }

    @Override
    public String toString() {
        return description;
    }
}
