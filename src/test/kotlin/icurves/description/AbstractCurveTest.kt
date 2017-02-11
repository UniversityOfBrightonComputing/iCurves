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

    @Test
    fun `Test equality`() {
        val curve1 = AbstractCurve("P")
        val curve2 = AbstractCurve("P")

        assertThat(curve1 === curve2, `is`(false))
        assertThat(curve1 == curve2, `is`(true))
        assertThat(curve1.hashCode(), `is`(curve2.hashCode()))
    }
}