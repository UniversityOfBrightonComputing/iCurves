package icurves.util

import icurves.concrete.CircleCurve
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import math.geom2d.polygon.Polygon2D
import math.geom2d.polygon.SimplePolygon2D
import java.util.*

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class Converter {

    companion object {
        @JvmStatic fun circleToPolygon(circle: CircleCurve): Polygon2D {
            val fxPoly = makePolygon(circle.radius.toInt(), 16)
            fxPoly.translateX = circle.minX.toDouble()
            fxPoly.translateY = circle.minY.toDouble()

            return toPolygon2D(fxPoly)
        }

        @JvmStatic fun toPolygon2D(polygon: Polygon): Polygon2D {
            val points = ArrayList<math.geom2d.Point2D>()

            var i = 0
            while (i < polygon.points.size) {
                val x = polygon.points[i] + polygon.translateX
                val y = polygon.points[i + 1] + polygon.translateY

                points.add(math.geom2d.Point2D(x, y))
                i += 2
            }

            return SimplePolygon2D(points)
        }

        @JvmStatic fun toPolygonFX(polygon: Polygon2D): Polygon {
            val points = DoubleArray(2 * polygon.vertexNumber())

            var i = 0
            for (p in polygon.vertices()) {
                points[i++] = p.x()
                points[i++] = p.y()
            }

            val polygonFX = Polygon(*points)

            polygonFX.fill = Color.TRANSPARENT
            polygonFX.stroke = Color.BLACK

            return polygonFX
        }

        @JvmStatic fun toPolygonFX(vertices: List<Point2D>): Polygon {
            val points = DoubleArray(2 * vertices.size)

            var i = 0
            for (p in vertices) {
                points[i++] = p.x
                points[i++] = p.y
            }

            val polygonFX = Polygon(*points)

            polygonFX.fill = Color.TRANSPARENT
            polygonFX.stroke = Color.BLACK

            return polygonFX
        }

        @JvmStatic fun makePolygon(radius: Int, vertices: Int): Polygon {

            val diameter = (radius * 2).toDouble()

            val side = diameter / (Math.cos(Math.PI / vertices) / Math.sin(Math.PI / vertices))

            val inAngle = (vertices - 2) * Math.PI / vertices

            var vector = Point2D(1.0, 0.0).multiply(side)

            val pointsList = ArrayList<Point2D>()
            val R = side / (2 * Math.sin(Math.PI / vertices))
            pointsList.add(Point2D(radius - R, radius.toDouble()))

            val p0 = pointsList[0]

            vector = rotate(vector, -inAngle / 2)
            val p1 = p0.add(vector)

            pointsList.add(p1)

            var prevPoint = p1

            for (i in 2..vertices - 1) {
                vector = rotate(vector, Math.PI - inAngle)

                prevPoint = prevPoint.add(vector)

                pointsList.add(prevPoint)
            }

            val points = DoubleArray(2 * vertices)

            var i = 0
            for (p in pointsList) {
                points[i++] = p.x
                points[i++] = p.y
            }

            val polygon = Polygon(*points)

            polygon.fill = Color.TRANSPARENT
            polygon.stroke = Color.BLACK

            return polygon
        }

        /**
         *
         * @param angle in radians
         */
        private fun rotate(vector: Point2D, angle: Double): Point2D {
            return Point2D(vector.x * Math.cos(angle) - vector.y * Math.sin(angle),
                    vector.y * Math.cos(angle) + vector.x * Math.sin(angle))
        }
    }
}