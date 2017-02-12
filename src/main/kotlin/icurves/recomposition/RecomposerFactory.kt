package icurves.recomposition

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class RecomposerFactory {

    companion object {
        @JvmStatic fun newRecomposer(strategy: RecompositionStrategy): Recomposer {
            return BasicRecomposer()
        }

        @JvmStatic fun newRecomposer(): Recomposer {
            return BasicRecomposer()
        }
    }
}