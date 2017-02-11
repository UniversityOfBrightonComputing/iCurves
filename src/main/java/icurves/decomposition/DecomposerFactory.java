package icurves.decomposition;

import icurves.description.AbstractBasicRegion;
import icurves.description.AbstractCurve;
import icurves.abstractdescription.AbstractDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Creates decomposers.
 */
public final class DecomposerFactory {

    /**
     * Instantiates a new decomposer that will use given strategy type.
     * The type of the decomposer is implementation-dependent.
     *
     * @param type strategy type
     * @return decomposer
     */
    public static Decomposer newDecomposer(DecompositionStrategyType type) {
        switch (type) {
            case ALPHABETICAL:
                return new BasicDecomposer(alphabetical());
            case REVERSE_ALPHABETICAL:
                return new BasicDecomposer(reverseAlphabetical());
            case INNERMOST:
                return new BasicDecomposer(innermost());
            case PIERCED_FIRST:
                return new BasicDecomposer(piercing());
            default:
                throw new IllegalArgumentException("Unknown strategy type: " + type);
        }
    }

    /**
     * An innermost abstract contour has the fewest abstract basic regions inside.
     *
     * @return innermost decomposition strategy
     */
    private static DecompositionStrategy innermost() {
        return ad -> {
            List<AbstractCurve> result = new ArrayList<>();

            ad.getCurves()
                    .stream()
                    .reduce((curve1, curve2) -> ad.getNumZonesIn(curve1) <= ad.getNumZonesIn(curve2) ? curve1 : curve2)
                    .ifPresent(result::add);

            return result;
        };
    }

    private static DecompositionStrategy alphabetical() {
        //return ad -> Collections.singletonList(ad.getFirstContour());
        return null;
    }

    private static DecompositionStrategy reverseAlphabetical() {
        //return ad -> Collections.singletonList(ad.getLastContour());
        return null;
    }

    private static DecompositionStrategy piercing() {
        return DecomposerFactory::getContoursToRemovePiercing;
    }

    private static List<AbstractCurve> getContoursToRemovePiercing(AbstractDescription ad) {
        List<AbstractCurve> result = new ArrayList<>();

        int bestNZ = Integer.MAX_VALUE;

        for (AbstractCurve curve : ad.getCurves()) {
            if (isPiercingCurve(curve, ad)) {
                int nz = ad.getNumZonesIn(curve);
                if (nz < bestNZ) {
                    result.clear();
                    result.add(curve);
                    bestNZ = nz;
                } else if (nz == bestNZ) {
                    result.add(curve);
                }
            }
        }

        if (result.isEmpty()) {
            for (AbstractCurve curve : ad.getCurves()) {
                int nz = ad.getNumZonesIn(curve);
                if (nz < bestNZ) {
                    result.clear();
                    result.add(curve);
                    bestNZ = nz;
                } else if (nz == bestNZ) {
                    result.add(curve);
                }
            }
        }

        return result;
    }

    private static boolean isPiercingCurve(AbstractCurve ac, AbstractDescription ad) {
        // every abstract basic region in ad which is in ac
        // must have a corresponding abr which is not in ac
        ArrayList<AbstractBasicRegion> zonesInContour = new ArrayList<>();

        abrLoop:
        for (AbstractBasicRegion zone : ad.getZones()) {
            if (zone.contains(ac)) {
                zonesInContour.add(zone);

                // look for a partner zone
                for (AbstractBasicRegion zone2 : ad.getZones()) {
                    if (zone.getStraddledContour(zone2).orElse(null) == ac) {
                        continue abrLoop;
                    }
                }

                // never found a partner zone
                return false;
            }
        }

        // check that the zones in C form a cluster - we need 2^n zones
        int power = powerOfTwo(zonesInContour.size());
        if (power < 0) {
            return false;
        }

        // find the smallest zone (one in fewest contours)
        AbstractBasicRegion smallestZone = zonesInContour.stream()
                .reduce((zone1, zone2) -> zone1.getNumCurves() <= zone2.getNumCurves() ? zone1 : zone2)
                .orElseThrow(() -> new RuntimeException("There are no zones in given contour"));

        // every other zone in ac must be a superset of that zone
        for (AbstractBasicRegion zone : zonesInContour) {
            for (AbstractCurve curve : smallestZone.getInSet()) {
                if (!zone.contains(curve)) {
                    return false;
                }
            }
        }

        // We have 2^n zones which are all supersets of smallestZone.
        // Check that they use exactly n contours from smallestZone.
        Set<AbstractCurve> addedContours = new TreeSet<>();

        for (AbstractBasicRegion zone : zonesInContour) {
            for (AbstractCurve curve : zone.getInSet()) {
                if (!smallestZone.contains(curve)) {
                    addedContours.add(curve);
                    if (addedContours.size() > power) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Computes log2(n).
     *
     * @param n logarithm of
     * @return result where n = 2^(result)
     */
    private static int powerOfTwo(int n) {
        int result = 0;
        while (n % 2 == 0) {
            result++;
            n /= 2;
        }
        if (n != 1) {
            return -1;
        } else {
            return result;
        }
    }
}
