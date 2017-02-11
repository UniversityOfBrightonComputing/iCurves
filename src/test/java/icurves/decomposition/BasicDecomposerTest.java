package icurves.decomposition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BasicDecomposerTest {

//    private Decomposer decomposer;
//
//    @Before
//    public void setUp() {
//        decomposer = DecomposerFactory.newDecomposer(DecompositionStrategyType.PIERCED_FIRST);
//    }
//
//    // TODO: create test data to check required methods (removed, from, to, etc)
//    // e.g.
//    // abstract description: a b ab
//    // steps: 2
//    // -a, a b ab, b
//    // -b, b, 0
//    // the data is then to be parsed and fed into this method for automated testing
//    @Test
//    public void decompose() {
//        List<DecompositionStep> steps = decomposer.decompose(new AbstractDescription("a b ab"));
//
//        // a b ab -> b
//        // b      -> 0
//        assertEquals(2, steps.size());
//
//        DecompositionStep step1 = steps.get(0);
//        assertTrue(step1.removed().hasLabel("a"));
//        assertTrue(step1.from().hasSameAbstractDescription(new AbstractDescription("a b ab")));
//        assertTrue(step1.to().hasSameAbstractDescription(new AbstractDescription("b")));
//
//        DecompositionStep step2 = steps.get(1);
//        assertTrue(step2.removed().hasLabel("b"));
//        assertTrue(step2.from().hasSameAbstractDescription(new AbstractDescription("b")));
//        assertTrue(step2.to().hasSameAbstractDescription(new AbstractDescription(" ")));
//
//        // the following bit was in the original "manual" test, so need to refactor as above
//
////        System.out.println("example 1: ____________ a b ab ac ad de");
////        steplist = decomposer.decompose(
////                AbstractDescription.makeForTesting("a b ab ac ad de"));
////        for(DecompositionStep step : steplist)
////            System.out.println("step : "+step.toDebugString());
////
////        System.out.println("example 1: ____________ a(1) b a(2)b");
////        // an example with multiple curves with the same label
////        CurveLabel a = CurveLabel.get("a");
////        CurveLabel b = CurveLabel.get("b");
////
////        TreeSet<AbstractCurve> tsc = new TreeSet<AbstractCurve>();
////        TreeSet<AbstractBasicRegion> tsz = new TreeSet<AbstractBasicRegion>();
////        AbstractCurve ca1 = new AbstractCurve(a);
////        AbstractCurve ca2 = new AbstractCurve(a);
////        AbstractCurve cb = new AbstractCurve(b);
////        tsz.add(AbstractBasicRegion.get(tsc)); // empty
////        tsc.add(ca1);
////        tsz.add(AbstractBasicRegion.get(tsc)); // in a(1)
////        tsc.clear();
////        tsc.add(cb);
////        tsz.add(AbstractBasicRegion.get(tsc)); // in b
////        tsc.add(ca2);
////        tsz.add(AbstractBasicRegion.get(tsc)); // in a(2) and b
////        tsc.add(ca1);
////        AbstractDescription ad = new AbstractDescription(tsc, tsz);
////        steplist = decomposer.decompose(ad);
////        for(DecompositionStep step : steplist)
////            System.out.println("step : "+step.toDebugString());
//    }
}
