package icurves.util

import javafx.geometry.Point2D
import math.geom2d.polygon.Polygon2D
import java.util.*

/**
 * Adapted from https://github.com/mapbox/polylabel
 * under the ICS license.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class Polylabel {

    companion object {
        val precision = 1.0

        @JvmStatic fun findCenter(polygon: Polygon2D): Point2D {

            // a priority queue of cells in order of their "potential" (max distance to polygon)
            val cellQueue = PriorityQueue<Cell>(Comparator { a, b -> (b.max - a.max).toInt() })

            // find the bounding box of the outer ring
            val bbox = polygon.boundingBox()

            val minX = bbox.minX
            val minY = bbox.minY
            val maxX = bbox.maxX
            val maxY = bbox.maxY

            var width = maxX - minX;
            var height = maxY - minY;
            var cellSize = Math.min(width, height);
            var h = cellSize / 2;

            if (cellSize == 0.0)
                return Point2D(minX, minY)

            var x = minX
            var y = minY

            // cover polygon with initial cells
            while (x < maxX) {
                while (y < maxY) {
                    cellQueue.add(Cell(x + h, y + h, h, polygon))

                    y += cellSize
                }

                x += cellSize
            }

            // take centroid as the first best guess
            val centroid = polygon.centroid()
            var bestCell = Cell(centroid.x(), centroid.y(), 0.0, polygon)

            // special case for rectangular polygons
            val bboxCell = Cell(minX + width / 2, minY + height / 2, 0.0, polygon)

            if (bboxCell.d > bestCell.d)
                bestCell = bboxCell

            var numProbes = cellQueue.size

            while (cellQueue.isNotEmpty()) {
                // pick the most promising cell from the queue
                val cell = cellQueue.poll()

                // update the best cell if we found a better one
                if (cell.d > bestCell.d) {
                    bestCell = cell;
                }

                // do not drill down further if there's no chance of a better solution
                if (cell.max - bestCell.d <= precision)
                    continue;

                // split the cell into four cells
                h = cell.h / 2;

                cellQueue.add(Cell(cell.x - h, cell.y - h, h, polygon))
                cellQueue.add(Cell(cell.x + h, cell.y - h, h, polygon))
                cellQueue.add(Cell(cell.x - h, cell.y + h, h, polygon))
                cellQueue.add(Cell(cell.x + h, cell.y + h, h, polygon))

                numProbes += 4;
            }

            return Point2D(bestCell.x, bestCell.y)
        }
    }

    private object SQRT2 {
        val value by lazy { Math.sqrt(2.0) }
    }

    private class Cell(
            // cell center
            val x: Double,
            val y: Double,

            // half cell size
            val h: Double,
            val polygon: Polygon2D) {

        // distance from cell center to polygon
        val d: Double

        // max distance to polygon within a cell
        val max: Double

        init {
            // signed distance from point to polygon outline (negative if point is outside)
            d = -polygon.boundary().signedDistance(x, y)

            max = d + h * SQRT2.value
        }
    }
}