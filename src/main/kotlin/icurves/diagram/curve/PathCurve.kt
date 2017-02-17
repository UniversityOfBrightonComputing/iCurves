package icurves.diagram.curve

import icurves.description.AbstractCurve
import icurves.diagram.Curve
import javafx.scene.paint.Color
import javafx.scene.shape.*
import math.geom2d.Point2D
import math.geom2d.polygon.Polygon2D
import math.geom2d.polygon.SimplePolygon2D

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class PathCurve(abstractCurve: AbstractCurve,
                val path: Path) : Curve(abstractCurve) {

    init {
        path.elements.addAll(ClosePath())
        path.fill = Color.TRANSPARENT
    }

    override fun computeShape(): Shape {
        val bbox = Rectangle(10000.0, 10000.0)
        bbox.translateX = -3000.0
        bbox.translateY = -3000.0

        val shape = Shape.intersect(bbox, path)
        shape.fill = Color.TRANSPARENT
        shape.stroke = Color.DARKBLUE
        shape.strokeWidth = 2.0

        return shape
    }

    override fun computePolygon(): Polygon2D {
        val moveTo = path.elements[0] as MoveTo

        val polygonPoints = arrayListOf<Point2D>()

        val p0 = Point2D(moveTo.x, moveTo.y)
        polygonPoints.add(p0)

        // drop moveTo and close()
        path.elements.drop(1).dropLast(1).forEach {
            when (it) {
                is QuadCurveTo -> {
                    val smoothFactor = 10
                    val p1 = polygonPoints.last()
                    val p2 = Point2D(it.controlX, it.controlY)
                    val p3 = Point2D(it.x, it.y)

                    var t = 0.01
                    while (t < 1.01) {

                        polygonPoints.add(getQuadValue(p1, p2, p3, t))
                        t += 1.0 / smoothFactor
                    }

                    polygonPoints.add(p3)
                }

                is CubicCurveTo -> {
                    val smoothFactor = 10
                    val p1 = polygonPoints.last()
                    val p2 = Point2D(it.controlX1, it.controlY1)
                    val p3 = Point2D(it.controlX2, it.controlY2)
                    val p4 = Point2D(it.x, it.y)

                    var t = 0.01
                    while (t < 1.01) {

                        polygonPoints.add(getCubicValue(p1, p2, p3, p4, t))
                        t += 1.0 / smoothFactor
                    }

                    polygonPoints.add(p4)
                }

                is LineTo -> {
                    polygonPoints.add(Point2D(it.x, it.y))
                }

                is ClosePath -> {
                    // ignore
                }

                else -> {
                    throw IllegalArgumentException("Unknown path element: $it")
                }
            }
        }

        return SimplePolygon2D(polygonPoints)
    }

    private fun getQuadValue(p1: Point2D, p2: Point2D, p3: Point2D, t: Double): Point2D {
        val x = (1 - t) * (1 - t) * p1.x() + 2 * (1 - t) * t * p2.x() + t * t * p3.x()
        val y = (1 - t) * (1 - t) * p1.y() + 2 * (1 - t) * t * p2.y() + t * t * p3.y()

        return Point2D(x, y)
    }

    private fun getCubicValue(p1: Point2D, p2: Point2D, p3: Point2D, p4: Point2D, t: Double): Point2D {
        val x = Math.pow(1 - t, 3.0) * p1.x + 3 * t * Math.pow(1 - t, 2.0) * p2.x + 3 * t*t * (1 - t) * p3.x + t*t*t*p4.x
        val y = Math.pow(1 - t, 3.0) * p1.y + 3 * t * Math.pow(1 - t, 2.0) * p2.y + 3 * t*t * (1 - t) * p3.y + t*t*t*p4.y
        return Point2D(x, y)
    }
}