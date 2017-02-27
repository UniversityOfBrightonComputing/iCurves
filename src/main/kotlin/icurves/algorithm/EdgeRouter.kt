package icurves.algorithm

import icurves.algorithm.astar.AStarGrid
import icurves.algorithm.astar.NodeState
import icurves.diagram.BasicRegion
import javafx.scene.shape.Polyline
import math.geom2d.Point2D
import math.geom2d.polygon.Polygons2D

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
object EdgeRouter {

    private val TILE_SIZE = 50

    fun route(region1: BasicRegion, region2: BasicRegion): Polyline {

        val union = Polygons2D.union(region1.getPolygonShape(), region2.getPolygonShape())

        //val maxDistance = union.distance(region1.center.x, region1.center.y)

        val bbox = union.boundingBox()

        println("${bbox.minX}  ${bbox.minY}")

        val grid = AStarGrid(bbox.width.toInt() / TILE_SIZE, bbox.height.toInt() / TILE_SIZE)

        println("Grid size: ${grid.width}x${grid.height}")

        val boundary = union.boundary()

        for (y in 0 until grid.height) {
            for (x in 0 until grid.width) {
                val tileCenter = Point2D(x.toDouble() * TILE_SIZE + TILE_SIZE / 2 + bbox.minX, y.toDouble() * TILE_SIZE + TILE_SIZE / 2 + bbox.minY)

                try {
                    if (union.contains(tileCenter)) {
                        grid.setNodeState(x, y, NodeState.WALKABLE)

                        val dist = -boundary.signedDistance(tileCenter).toInt()

                        grid.getNode(x, y).gCost = 100000 - dist * 1000

                        if (grid.getNode(x, y).gCost < 0) {
                            println("Distance: $dist, gCost: ${grid.getNode(x, y).gCost}")

                            grid.getNode(x, y).gCost = 0
                        }


                    } else {
                        grid.setNodeState(x, y, NodeState.NOT_WALKABLE)
                    }
                } catch (e: Exception) {
                    grid.setNodeState(x, y, NodeState.NOT_WALKABLE)
                }
            }
        }

        val startX = (region1.center.x - bbox.minX) / TILE_SIZE
        val startY = (region1.center.y - bbox.minY) / TILE_SIZE
        val targetX = (region2.center.x - bbox.minX) / TILE_SIZE
        val targetY = (region2.center.y - bbox.minY) / TILE_SIZE

        println("$startX,$startY - $targetX,$targetY")

        val path = grid.getPath(startX.toInt(), startY.toInt(), targetX.toInt(), targetY.toInt())

        // so that start and end vertices are exactly the same as requested
        val points = arrayListOf<Double>(region1.center.x, region1.center.y)

        points.addAll(path.map { arrayListOf(it.x, it.y) }
                .flatten()
                .mapIndexed { index, value -> value.toDouble() * TILE_SIZE + TILE_SIZE / 2 + (if (index % 2 == 0) bbox.minX else bbox.minY) }
                .dropLast(2)
        )

        // so that start and end vertices are exactly the same as requested
        points.add(region2.center.x)
        points.add(region2.center.y)

        return Polyline(*points.toDoubleArray())
    }
}