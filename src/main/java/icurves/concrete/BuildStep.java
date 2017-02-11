package icurves.concrete;

import icurves.recomposition.RecompositionData;

import java.util.ArrayList;
import java.util.List;

public class BuildStep {

    public List<RecompositionData> recomp_data = new ArrayList<>();
    public BuildStep next = null;

    BuildStep(RecompositionData rd) {
        recomp_data.add(rd);
    }
}
