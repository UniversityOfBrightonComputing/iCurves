package icurves.diagram.curve

import icurves.description.AbstractCurve
import icurves.diagram.Curve
import icurves.util.Converter
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape

/**
 * A curve whose shape is a simple polygon.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class PolygonCurve(
        abstractCurve: AbstractCurve,

        /**
         * Ordered list of polygon points.
         */
        points: List<Point2D>) : Curve(abstractCurve) {

    private val polygonFX: Polygon

    init {
        polygonFX = Converter.toPolygonFX(points)
    }

    override fun computeShape(): Shape {
        val bbox = Rectangle(10000.0, 10000.0)
        bbox.translateX = -3000.0
        bbox.translateY = -3000.0

        val shape = Shape.intersect(bbox, polygonFX)
        shape.fill = Color.TRANSPARENT
        shape.stroke = Color.DARKBLUE
        shape.strokeWidth = 2.0

        return shape
    }

    override fun computePolygon() = Converter.toPolygon2D(polygonFX)
}