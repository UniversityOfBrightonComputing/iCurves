package icurves.recomposition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class RecomposerTest {

//    private Recomposer recomposer;
//
//    @Before
//    public void setUp() {
//        recomposer = RecomposerFactory.newRecomposer(RecompositionStrategyType.DOUBLY_PIERCED);
//    }
//
//    @Test
//    public void recompose() {
//        List<DecompositionStep> decompositionSteps = DecomposerFactory
//                .newDecomposer(DecompositionStrategyType.PIERCED_FIRST)
//                .decompose(new Description("a b ab"));
//        List<RecompositionStep> steps = recomposer.recompose(decompositionSteps);
//
//        // 0 + b -> b
//        // b + a -> a b ab
//        assertEquals(2, steps.size());
//
//        RecompositionStep step1 = steps.get(0);
//        assertTrue(step1.to().hasSameAbstractDescription(new Description("b")));
//
//        RecompositionStep step2 = steps.get(1);
//        assertTrue(step2.to().hasSameAbstractDescription(new Description("a b ab")));
//    }
}
