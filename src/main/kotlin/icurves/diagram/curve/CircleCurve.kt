package icurves.diagram.curve

import icurves.description.AbstractCurve
import icurves.diagram.Curve
import icurves.util.Converter
import javafx.scene.shape.Circle

/**
 * A curve whose shape is a circle.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class CircleCurve(
        abstractCurve: AbstractCurve,

        var centerX: Double,
        var centerY: Double,
        var radius: Double) : Curve(abstractCurve) {

    private val nudge = 0.1

    fun getSmallRadius() = radius - nudge

    fun getBigRadius() = radius + nudge

    fun shift(x: Double, y: Double) {
        centerX += x
        centerY += y
    }

    fun scaleAboutZero(scale: Double) {
        centerX *= scale
        centerY *= scale
        radius *= scale
    }

    fun getLabelXPosition() = centerX + 0.8 * radius

    fun getLabelYPosition() = centerY - 0.8 * radius

    fun getMinX() = (centerX - radius).toInt()

    fun getMaxX() = (centerX + radius).toInt() + 1

    fun getMinY() = (centerY - radius).toInt()

    fun getMaxY() = (centerY + radius).toInt() + 1

    override fun computeShape() = Circle(centerX, centerY, getBigRadius())

    override fun computePolygon() = Converter.circleToPolygon(this)
}