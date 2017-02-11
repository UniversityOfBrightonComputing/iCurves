package icurves.description;

import icurves.description.AbstractBasicRegion;
import icurves.description.AbstractCurve;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class AbstractBasicRegionTest {

    private AbstractBasicRegion zone1;
    private AbstractBasicRegion zone2;
    private AbstractBasicRegion zone3;

    @Before
    public void setUp() {
        zone1 = new AbstractBasicRegion(makeCurves("a"));
        zone2 = new AbstractBasicRegion(makeCurves("a", "b"));
        zone3 = new AbstractBasicRegion(makeCurves("a"));
    }

    @Test
    public void testEquals() {
        assertEquals(zone1, zone3);
        assertNotEquals(zone1, zone2);
        assertNotEquals(zone3, zone2);
    }

    @Test
    public void testGetNumContours() throws Exception {
        assertEquals(0, AbstractBasicRegion.OUTSIDE.getNumCurves());
        assertEquals(1, zone1.getNumCurves());
        assertEquals(2, zone2.getNumCurves());
        assertEquals(1, zone3.getNumCurves());
    }

    @Test
    public void testContains() throws Exception {
        assertTrue(zone1.contains(new AbstractCurve("a")));
        assertTrue(zone2.contains(new AbstractCurve("a")));
        assertTrue(zone3.contains(new AbstractCurve("a")));

        assertTrue(!zone1.contains(new AbstractCurve("b")));
        assertTrue(zone2.contains(new AbstractCurve("b")));
        assertTrue(!zone3.contains(new AbstractCurve("b")));
    }

    @Test
    public void testMoveInside() throws Exception {
        assertEquals(zone1, new AbstractBasicRegion(new TreeSet<>()).moveInside(new AbstractCurve("a")));
        assertEquals(zone3, new AbstractBasicRegion(new TreeSet<>()).moveInside(new AbstractCurve("a")));

        assertEquals(zone2, zone1.moveInside(new AbstractCurve("b")));
        assertEquals(zone2, zone3.moveInside(new AbstractCurve("b")));
    }

    @Test
    public void testMoveOutside() {
        assertEquals(zone1, zone2.moveOutside(new AbstractCurve("b")));
        assertEquals(zone3, zone2.moveOutside(new AbstractCurve("b")));

        assertEquals(new AbstractBasicRegion(makeCurves("b")), zone2.moveOutside(new AbstractCurve("a")));
        assertEquals(new AbstractBasicRegion(new TreeSet<>()), zone1.moveOutside(new AbstractCurve("a")));
        assertEquals(new AbstractBasicRegion(new TreeSet<>()), zone3.moveOutside(new AbstractCurve("a")));

        assertEquals(new AbstractBasicRegion(new TreeSet<>()), new AbstractBasicRegion(new TreeSet<>()).moveOutside(new AbstractCurve("a")));
    }

    @Test
    public void testStraddledContour() {
        assertTrue(!zone1.getStraddledContour(zone3).isPresent());
        assertTrue(!zone2.getStraddledContour(AbstractBasicRegion.OUTSIDE).isPresent());

        assertEquals(new AbstractCurve("a"), zone1.getStraddledContour(AbstractBasicRegion.OUTSIDE).get());
        assertEquals(new AbstractCurve("a"), AbstractBasicRegion.OUTSIDE.getStraddledContour(zone1).get());

        assertEquals(new AbstractCurve("b"), zone1.getStraddledContour(zone2).get());
        assertEquals(new AbstractCurve("b"), zone2.getStraddledContour(zone1).get());
        assertEquals(new AbstractCurve("b"), zone3.getStraddledContour(zone2).get());
    }

    @Test
    public void testToString() throws Exception {
        assertNotEquals(zone1.toString(), zone2.toString());
        assertEquals(zone1.toString(), zone3.toString());

        assertEquals("{a}", zone1.toString());
        assertEquals("{a,b}", zone2.toString());
    }

    private Set<AbstractCurve> makeCurves(String... curveLabels) {
        return Arrays.asList(curveLabels)
                .stream()
                .map(AbstractCurve::new)
                .collect(Collectors.toSet());
    }
}