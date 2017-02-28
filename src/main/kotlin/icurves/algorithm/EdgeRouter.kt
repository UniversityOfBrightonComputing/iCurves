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

    private val TILES = 50

    fun route(region1: BasicRegion, region2: BasicRegion): Polyline {

        val union = Polygons2D.union(region1.getPolygonShape(), region2.getPolygonShape())



        val bbox = union.boundingBox()

        val TILE_SIZE = (Math.min(bbox.width, bbox.height) / TILES).toInt()

        val grid = AStarGrid(bbox.width.toInt() / TILE_SIZE, bbox.height.toInt() / TILE_SIZE)



        println("Grid size: ${grid.width}x${grid.height}")




        val boundary = union.boundary()

        // signed, so - if inside
        val maxDistance: Double = try {
            -Math.min(boundary.signedDistance(region1.center.x, region1.center.y), boundary.signedDistance(region2.center.x, region2.center.y))
        } catch (e: Exception) {
            1000.0
        }

        for (y in 0 until grid.height) {
            for (x in 0 until grid.width) {
                val tileCenter = Point2D(x.toDouble() * TILE_SIZE + TILE_SIZE / 2 + bbox.minX, y.toDouble() * TILE_SIZE + TILE_SIZE / 2 + bbox.minY)

                val node = grid.getNode(x, y)

                try {
                    if (union.contains(tileCenter)) {
                        val dist = -boundary.signedDistance(tileCenter).toInt()

                        if (dist < TILE_SIZE) {
                            node.state = NodeState.NOT_WALKABLE
                            continue
                        }


                        node.state = NodeState.WALKABLE
                        //node.gCost = 100000 - dist * 1000
                        node.gCost = ((2 - dist / maxDistance) * 2500).toInt()

                        if (node.gCost < 0) {
                            println("Distance: $dist, gCost: ${node.gCost}")

                            node.gCost = 0
                        }


                    } else {
                        node.state = NodeState.NOT_WALKABLE
                    }
                } catch (e: Exception) {
                    node.state = NodeState.NOT_WALKABLE
                }
            }
        }

        val startX = (region1.center.x - bbox.minX) / TILE_SIZE
        val startY = (region1.center.y - bbox.minY) / TILE_SIZE
        val targetX = (region2.center.x - bbox.minX) / TILE_SIZE
        val targetY = (region2.center.y - bbox.minY) / TILE_SIZE

        println("$startX,$startY - $targetX,$targetY")

        val path = grid.getPath(startX.toInt(), startY.toInt(), targetX.toInt(), targetY.toInt())

        if (path.isEmpty()) {
            println("Edge routing A* not found")
        }

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