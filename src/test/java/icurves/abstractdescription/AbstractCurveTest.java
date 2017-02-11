package icurves.abstractdescription;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class AbstractCurveTest {
//
//    private static final String alphanum = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
//
//    @Test
//    public void testConstructorValid() {
//        for (char c : alphanum.toCharArray()) {
//            new AbstractCurve(String.valueOf(c));
//        }
//    }
//
//    @Test
//    public void testConstructorInvalid() {
//        int count = 0;
//
//        for (char c = Character.MIN_VALUE; c < Character.MAX_VALUE; c++) {
//            if (alphanum.contains(String.valueOf(c)))
//                continue;
//
//            try {
//                new AbstractCurve(String.valueOf(c));
//            } catch (IllegalArgumentException e) {
//                count++;
//            }
//        }
//
//        assertEquals(Character.MAX_VALUE - alphanum.length(), count);
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void testConstructorNullLabel() {
//        String label = null;
//        new AbstractCurve(label);
//    }
//
//    @Test
//    public void testToString() {
//        AbstractCurve curve1 = new AbstractCurve("a");
//        AbstractCurve curve2 = new AbstractCurve("b");
//        AbstractCurve curve3 = new AbstractCurve("a");
//
//        assertNotEquals(curve1.toString(), curve2.toString());
//        assertEquals(curve1.toString(), curve3.toString());
//    }
//
//    @Test
//    public void sameLabel() {
//        AbstractCurve curve1 = new AbstractCurve("a");
//        AbstractCurve curve2 = new AbstractCurve("b");
//        AbstractCurve curve3 = new AbstractCurve("a");
//
//        assertFalse(curve1.matchesLabel(curve2));
//        assertTrue(curve1.matchesLabel(curve3));
//
//        assertTrue(curve1.hasLabel("a"));
//        assertFalse(curve2.hasLabel("a"));
//        assertTrue(curve3.hasLabel("a"));
//
//        assertEquals("a", curve1.getLabel());
//        assertEquals(curve1.getLabel(), curve3.getLabel());
//        assertNotEquals("a", curve2.getLabel());
//    }
}
