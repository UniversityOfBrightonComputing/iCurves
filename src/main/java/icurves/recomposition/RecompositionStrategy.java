package icurves.recomposition;

import icurves.description.AbstractBasicRegion;
import icurves.description.Description;

import java.util.List;

public interface RecompositionStrategy {

    /**
     *
     * @param zonesToSplit zones needed to split by the curve
     * @param description abstract description so far
     * @return clusters
     */
    List<Cluster> makeClusters(List<AbstractBasicRegion> zonesToSplit, Description description);
}
