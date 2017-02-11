package icurves.recomposition;

import icurves.abstractdescription.AbstractBasicRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Cluster {

    private List<AbstractBasicRegion> zones;

    public Cluster(AbstractBasicRegion z) {
        zones = new ArrayList<>();
        zones.add(z);
    }

    public Cluster(AbstractBasicRegion z1,
            AbstractBasicRegion z2) {

        if (!z1.getStraddledContour(z2).isPresent())
            throw new IllegalArgumentException("Non-adjacent cluster pair");

        zones = new ArrayList<>();
        zones.add(z1);
        zones.add(z2);
    }

    public Cluster(AbstractBasicRegion z1,
            AbstractBasicRegion z2,
            AbstractBasicRegion z3,
            AbstractBasicRegion z4) {

        if (!z1.getStraddledContour(z2).isPresent())
            throw new IllegalArgumentException("Non-adjacent cluster pair");
        if (!z1.getStraddledContour(z3).isPresent())
            throw new IllegalArgumentException("Non-adjacent cluster pair");

        // TODO: check references?
        if (z2.getStraddledContour(z4).orElse(null) != z1.getStraddledContour(z3).orElse(null))
            throw new IllegalArgumentException("Non-adjacent cluster pair");
        if (z3.getStraddledContour(z4).orElse(null) != z1.getStraddledContour(z2).orElse(null))
            throw new IllegalArgumentException("Non-adjacent cluster pair");

        zones = new ArrayList<>();
        zones.add(z1);
        zones.add(z2);
        zones.add(z3);
        zones.add(z4);
    }

    public Cluster(AbstractBasicRegion... regions) {
        zones = new ArrayList<>();
        zones.addAll(Arrays.asList(regions));
    }

    public List<AbstractBasicRegion> zones() {
        return zones;
    }

    @Override
    public boolean equals(Object obj) {
        return zones.containsAll(((Cluster)obj).zones);
    }

    @Override
    public int hashCode() {
        return zones.stream().mapToInt(z -> z.hashCode()).sum();
    }

    @Override
    public String toString() {
        return zones.toString();
    }
}
