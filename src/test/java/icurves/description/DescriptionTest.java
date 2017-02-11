package icurves.description;

import icurves.description.AbstractCurve;
import icurves.description.Description;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class DescriptionTest {

    private Description ad1, ad2, ad3;

    @Before
    public void setUp() {
        ad1 = Description.from("a ab abc bc ac");
        ad2 = Description.from("abc bc ab ac a");
        ad3 = Description.from("a ad abc bc ac");
    }
//
//    @Test
//    public void testConstructorCondition1Valid() {
//        Set<AbstractCurve> curves = new TreeSet<>();
//        Set<AbstractBasicRegion> zones = new TreeSet<>();
//
//        AbstractCurve curve1 = new AbstractCurve("a");
//        AbstractCurve curve2 = new AbstractCurve("b");
//
//        curves.add(curve1);
//        curves.add(curve2);
//
//        zones.add(AbstractBasicRegion.get(curves));
//        zones.add(AbstractBasicRegion.OUTSIDE);
//
//        // Condition 1 holds
//        //curves.remove(curve2);
//
//        new Description(curves, zones);
//    }
//
//    @Test
//    public void testConstructorCondition1Invalid() {
//        Set<AbstractCurve> curves = new TreeSet<>();
//        Set<AbstractBasicRegion> zones = new TreeSet<>();
//
//        AbstractCurve curve1 = new AbstractCurve("a");
//        AbstractCurve curve2 = new AbstractCurve("b");
//
//        curves.add(curve1);
//        curves.add(curve2);
//
//        zones.add(AbstractBasicRegion.get(curves));
//
//        // Condition 1 fails because we have zones with curves not in the curve set
//        curves.remove(curve2);
//
//        String error = "";
//        try {
//            new Description(curves, zones);
//        } catch (IllegalArgumentException e) {
//            error = e.getMessage();
//        }
//
//        assertThat(error, containsString("Condition1"));
//    }
//
//    @Test
//    public void testConstructorCondition2Invalid() {
//        Set<AbstractCurve> curves = new TreeSet<>();
//        Set<AbstractBasicRegion> zones = new TreeSet<>();
//
//        AbstractCurve curve1 = new AbstractCurve("a");
//        AbstractCurve curve2 = new AbstractCurve("b");
//
//        curves.add(curve1);
//        curves.add(curve2);
//
//        zones.add(AbstractBasicRegion.get(curves));
//
//        // Condition 2 fails because we have no outside zones
//        //zones.add(AbstractBasicRegion.OUTSIDE);
//
//        String error = "";
//        try {
//            new Description(curves, zones);
//        } catch (IllegalArgumentException e) {
//            error = e.getMessage();
//        }
//
//        assertThat(error, containsString("Condition2"));
//    }
//
//    @Test
//    public void testConstructorCondition3Invalid() {
//        Set<AbstractCurve> curves = new TreeSet<>();
//        Set<AbstractBasicRegion> zones = new TreeSet<>();
//
//        AbstractCurve curve1 = new AbstractCurve("a");
//        AbstractCurve curve2 = new AbstractCurve("b");
//        AbstractCurve curve3 = new AbstractCurve("c");
//
//        curves.add(curve1);
//        curves.add(curve2);
//
//        zones.add(AbstractBasicRegion.get(curves));
//        zones.add(AbstractBasicRegion.OUTSIDE);
//
//        // Condition 3 fails because we have curves but no corresponding zones
//        curves.add(curve3);
//
//        String error = "";
//        try {
//            new Description(curves, zones);
//        } catch (IllegalArgumentException e) {
//            error = e.getMessage();
//        }
//
//        assertThat(error, containsString("Condition3"));
//    }
//
//    @Test
//    public void testGetInformalDescription() {
//        manualSetUp();
//        assertEquals("a ab ac bc abc", ad1.getInformalDescription());
//        assertEquals("a ab ac bc abc", ad2.getInformalDescription());
//        assertEquals("a ac ad bc abc", ad3.getInformalDescription());
//    }
//
    @Test
    public void testToString() {
        assertEquals(ad1.toString(), ad2.toString());
        assertNotEquals(ad1.toString(), ad3.toString());

        assertEquals("{},{a},{a,b},{a,c},{b,c},{a,b,c}", ad1.toString());
        assertEquals("{},{a},{a,b},{a,c},{b,c},{a,b,c}", ad2.toString());
        assertEquals("{},{a},{a,c},{a,d},{b,c},{a,b,c}", ad3.toString());
    }

    @Test
    public void testNumZonesIn() {
        assertEquals(4, ad1.getNumZonesIn(new AbstractCurve("a")));
        assertEquals(3, ad1.getNumZonesIn(new AbstractCurve("b")));
        assertEquals(3, ad1.getNumZonesIn(new AbstractCurve("c")));
    }
//
//    @Test
//    public void testZonesIn() {
//        manualSetUp();
//
//        // A
//        Set<AbstractBasicRegion> actualZonesInA = ad1.getZonesIn(getCurve(ad1, "a"));
//
//        Set<AbstractBasicRegion> expectedZonesInA = new TreeSet<>();
//        expectedZonesInA.add(getZone(ad1, "a"));
//        expectedZonesInA.add(getZone(ad1, "ab"));
//        expectedZonesInA.add(getZone(ad1, "ac"));
//        expectedZonesInA.add(getZone(ad1, "abc"));
//
//        assertEquals(expectedZonesInA, actualZonesInA);
//        assertEquals(4, actualZonesInA.size());
//
//        // B
//        Set<AbstractBasicRegion> actualZonesInB = ad1.getZonesIn(getCurve(ad1, "b"));
//
//        Set<AbstractBasicRegion> expectedZonesInB = new TreeSet<>();
//        expectedZonesInB.add(getZone(ad1, "ab"));
//        expectedZonesInB.add(getZone(ad1, "bc"));
//        expectedZonesInB.add(getZone(ad1, "abc"));
//
//        assertEquals(expectedZonesInB, actualZonesInB);
//        assertEquals(3, actualZonesInB.size());
//
//        // C
//        Set<AbstractBasicRegion> actualZonesInC = ad1.getZonesIn(getCurve(ad1, "c"));
//
//        Set<AbstractBasicRegion> expectedZonesInC = new TreeSet<>();
//        expectedZonesInC.add(getZone(ad1, "ac"));
//        expectedZonesInC.add(getZone(ad1, "bc"));
//        expectedZonesInC.add(getZone(ad1, "abc"));
//
//        assertEquals(expectedZonesInC, actualZonesInC);
//        assertEquals(3, actualZonesInC.size());
//    }
//
//    private AbstractCurve getCurve(Description description, String label) {
//        for (AbstractCurve curve : description.getCurvesUnmodifiable()) {
//            if (curve.hasLabel(label)) {
//                return curve;
//            }
//        }
//
//        throw new IllegalArgumentException("No curve with label: " + label + " in "
//            + description);
//    }
//
//    private AbstractBasicRegion getZone(Description description, String zoneLabel) {
//        Set<AbstractCurve> curves = Arrays.stream(zoneLabel.split(""))
//                .map(String::valueOf)
//                .map(curveLabel -> getCurve(description, curveLabel))
//                .sorted()
//                .collect(Collectors.toSet());
//
//        return AbstractBasicRegion.get(curves);
//    }
}
