package icurves;

import icurves.decomposition.DecompositionStrategyType;
import icurves.recomposition.RecompositionStrategyType;

public class TestDatum {

    public String description;
    public DecompositionStrategyType decomp_strategy;
    public RecompositionStrategyType recomp_strategy;
    public double expectedChecksum;

    public TestDatum(String string,
                     DecompositionStrategyType decomp_strategy,
                     RecompositionStrategyType recomp_strategy,
                     double checksum) {
        description = string;
        this.decomp_strategy = decomp_strategy;
        this.recomp_strategy = recomp_strategy;
        expectedChecksum = checksum;
    }
}
