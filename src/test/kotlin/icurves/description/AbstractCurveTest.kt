package icurves.description

import org.junit.Test

import org.junit.Assert.*
import org.hamcrest.CoreMatchers.*

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class AbstractCurveTest {

    @Test
    fun `Test abstract curve label`() {
        val curve1 = AbstractCurve("P")
        val curve2 = AbstractCurve("Q")

        assertThat(curve1.label, `is`("P"))
        assertThat(curve2.label, `is`("Q"))
    }
}