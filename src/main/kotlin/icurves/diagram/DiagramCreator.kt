package icurves.diagram

import icurves.decomposition.DecomposerFactory
import icurves.description.AbstractBasicRegion
import icurves.description.AbstractCurve
import icurves.description.Description
import icurves.diagram.curve.CircleCurve
import icurves.diagram.curve.PathCurve
import icurves.graph.EulerDualEdge
import icurves.graph.EulerDualNode
import icurves.graph.GraphCycle
import icurves.graph.MED
import icurves.guifx.SettingsController
import icurves.recomposition.RecomposerFactory
import icurves.recomposition.RecompositionData
import icurves.util.BezierApproximation
import icurves.util.Profiler
import javafx.collections.FXCollections
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import javafx.scene.shape.ClosePath
import javafx.scene.shape.LineTo
import javafx.scene.shape.Path
import javafx.scene.shape.Shape
import org.apache.logging.log4j.LogManager
import java.util.*

/**
 * Diagram creator that uses simple cycles to add curves.
 * At least 1 Hamiltonian is always present.
 * This ensures that any description is drawable.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class DiagramCreator(val settings: SettingsController) {

    companion object {
        private val log = LogManager.getLogger(DiagramCreator::class.java)

        @JvmField val BASE_CURVE_RADIUS = 1500.0
    }

    /**
     * Maps abstract curve to its concrete version.
     * This is a 1 to 1 map since there are no duplicates.
     */
    val curveToContour = FXCollections.observableMap(LinkedHashMap<AbstractCurve, Curve>())

    /**
     * Abstract basic regions we have processed so far.
     */
    private val abstractRegions = ArrayList<AbstractBasicRegion>()
    val shadedRegions = ArrayList<BasicRegion>()

    lateinit var modifiedDual: MED

    val debugPoints = arrayListOf<Point2D>()
    val debugShapes = arrayListOf<Shape>()

    fun createDiagram(description: Description) {

        // all we need is decomposition; recomposition is almost no-op
        val dSteps = DecomposerFactory.newDecomposer(settings.decompType).decompose(description)
        val rSteps = RecomposerFactory.newRecomposer().recompose(dSteps)

        for (i in rSteps.indices) {
            val data = rSteps[i].addedCurveData

            val curve = when (i) {

                // 0..2 are base cases for 1..3 curves
                0 -> CircleCurve(data.addedCurve, BASE_CURVE_RADIUS, BASE_CURVE_RADIUS, BASE_CURVE_RADIUS)
                1 -> CircleCurve(data.addedCurve, BASE_CURVE_RADIUS * 2, BASE_CURVE_RADIUS, BASE_CURVE_RADIUS)
                2 -> CircleCurve(data.addedCurve, BASE_CURVE_RADIUS * 1.5, BASE_CURVE_RADIUS * 2, BASE_CURVE_RADIUS)
                else -> embedCurve(data)
            }

            curveToContour[data.addedCurve] = curve

            if (i < 3) {
                // this is not entirely correct for say missing regions
                // a ab b ac c
                abstractRegions.addAll(data.newZones)
            }
        }

        if (settings.showMED())
            createMED()

        log.trace("Generating shaded zones")

        val shaded = abstractRegions.minus(description.zones)

        shadedRegions.addAll(shaded.map { BasicRegion(it, curveToContour) })
    }

    /**
     * Side-effects:
     *
     * 1. creates MED
     * 2. chooses a cycle
     * 3. creates a curve
     * 4. updates abstract regions
     */
    private fun embedCurve(data: RecompositionData): Curve {
        createMED()

        log.trace("Searching cycle with zones: ${data.splitZones}")

        val cycle = modifiedDual.computeCycle(
                data.splitZones
        ).orElseThrow { RuntimeException("Bug: Failed to find cycle") }

        var curve: Curve = PathCurve(data.addedCurve, cycle.path)

        //var curve: Curve = PolygonCurve(data.addedCurve, cycle.nodes.map { it.point })

        if (cycle.nodes.size == 4) {
            curve = embedDoublePiercing(data.addedCurve, cycle.nodes.map { it.zone })
        }

        if (settings.useSmooth() && curve !is CircleCurve) {
            val smoothedPath = smooth(cycle)

            curve = PathCurve(data.addedCurve, smoothedPath)
        }

        // we might've used more zones to get a cycle, so we make sure we capture all of the used ones
        // we also call distinct() to ensure we don't reuse the outside zone more than once
        abstractRegions.addAll(cycle.nodes.map { it.zone.abRegion.moveInside(data.addedCurve) }.distinct())

        return curve
    }

    private fun embedDoublePiercing(abstractCurve: AbstractCurve, regions: List<BasicRegion>): Curve {
        val center = regions.map { it.getPolygonShape().vertices() }
                .flatten()
                .groupBy({ it.asInt })
                // we search for a vertex that is present in all four regions
                .filter { it.value.size == 4 }
                .map { Point2D(it.key.getX(), it.key.getY()) }
                .firstOrNull() ?: throw RuntimeException("Bug: 2-piercing center not found")

//        val radius = regions.map { it.center.distance(center) }
//                .sorted()
//                .first()

        val radius = modifiedDual.allZones
                .minus(regions)
                .map { it.center.distance(center) }
                .sorted()
                .first()

        return CircleCurve(abstractCurve, center.x, center.y, radius / 3)
    }

    private fun smooth(cycle: GraphCycle<EulerDualNode, EulerDualEdge>): Path {
        Profiler.start("Smoothing")

        //val cycle = GraphCycle<EulerDualNode, EulerDualEdge>(cycle2.nodes.reversed(), cycle2.edges)

        val pathSegments = BezierApproximation.smoothPath2(cycle.nodes.map { it.point }.toMutableList(), settings.smoothFactor)

        val newPath = Path()

        // add moveTo
        newPath.elements.add(cycle.path.elements[0])

        for (j in cycle.nodes.indices) {
            val node1 = cycle.nodes[j]
            val node2 = if (j == cycle.nodes.size - 1) cycle.nodes[0] else cycle.nodes[j + 1]

            // this is to enable joining MED ring with internal edges at C2 continuity
            // remove first moveTo
//            pathSegments[j].elements.removeAt(0)
//
//            // add to new path
//            newPath.elements.addAll(pathSegments[j].elements)
//
//            if (true)
//                continue


            // check if this is the MED ring segment
            // No need to check if we use lines?
            if (node1.zone.abRegion == AbstractBasicRegion.OUTSIDE && node2.zone.abRegion == AbstractBasicRegion.OUTSIDE) {
                // j + 1 because we skip the first moveTo
//                            val arcTo = cycle.path.elements[j + 1] as ArcTo
//
//                            val start = settings.globalMap[arcTo] as Point2D
//
//                            var tmpPath = Path(MoveTo(start.x, start.y), arcTo)
//                            tmpPath.fill = null
//                            tmpPath.stroke = Color.BLACK
//
//                            //debugShapes.add(tmpPath)
//
//                            val ok = !intersects(tmpPath, curveToContour.values.toList())
//
//                            println("OK?: $ok")
//
//                            if (!ok) {
//                                arcTo.isSweepFlag = !arcTo.isSweepFlag
//
//                                tmpPath = Path(MoveTo(start.x, start.y), arcTo)
//                                tmpPath.fill = null
//                                tmpPath.stroke = Color.BLACK
//
//                                if (intersects(tmpPath, curveToContour.values.toList())) {
//                                    //debugShapes.add(tmpPath)
//                                    throw CannotDrawException("MED ring intersects with diagram")
//                                } else {
//                                    println("ALL GOOD")
//                                }
//                            }


                pathSegments[j].elements.removeAt(0)
                newPath.elements.addAll(pathSegments[j].elements)
//                val lineTo = cycle.path.elements[j + 1] as LineTo
//
//                newPath.elements.addAll(lineTo)
                continue
            }

            // the new curve segment must pass through the straddled curve
            // and only through that curve
            val abstractCurve = node1.zone.abRegion.getStraddledContour(node2.zone.abRegion).get()

            if (isOK(pathSegments[j], abstractCurve, curveToContour.values.toList())) {
                // remove first moveTo
                pathSegments[j].elements.removeAt(0)

                // add to new path
                newPath.elements.addAll(pathSegments[j].elements)
            } else {
                // j + 1 because we skip the first moveTo
                newPath.elements.addAll(cycle.path.elements[j + 1])
            }
        }

        newPath.fill = Color.TRANSPARENT
        newPath.elements.add(ClosePath())

        Profiler.end("Smoothing")

        return newPath
    }

    /**
     * Needs to be generated every time because contours change zones.
     *
     * TODO: we could potentially only compute zones that have been changed by the curve
     */
    private fun createMED() {
        log.trace("Creating MED")

        val concreteZones = abstractRegions.map { BasicRegion(it, curveToContour) }

        modifiedDual = MED(concreteZones, curveToContour)
    }

    /**
     * Does curve segment [q] only pass through [actual] curve.
     */
    fun isOK(q: Shape, actual: AbstractCurve, curves: List<Curve>): Boolean {
        val list = curves.filter {
            val s = it.computeShape()
            s.fill = null
            s.stroke = Color.BROWN

            !Shape.intersect(s, q).getLayoutBounds().isEmpty()
        }

        if (list.size != 1)
            return false

        return list.get(0).abstractCurve == actual
    }

    /**
     * Does curve segment [q] intersect with any other curves.
     */
    fun intersects(q: Shape, curves: List<Curve>): Boolean {
        val list = curves.filter {
            val s = it.computeShape()
            s.fill = null
            s.stroke = Color.BROWN

            !Shape.intersect(s, q).getLayoutBounds().isEmpty()
        }

        return list.isNotEmpty()
    }

    // 4 nodes can (maybe?) make a nice circle

    // a 4 node cluster is currently a minimum, since 2 nodes not a cycle
    //                if (cycle.nodes.size == 4) {
    //
    //                    // scale to make it larger to check intersection
    //                    val bounds = cycle.nodes
    //                            .map { it.zone.shape }
    //                            .map {
    //                                it.scaleX = 1.2
    //                                it.scaleY = 1.2
    //                                it
    //                            }
    //                            .reduceRight { s1, s2 -> Shape.intersect(s1, s2) }
    //                            .layoutBounds
    //
    //                    // scale back
    //                    cycle.nodes.map { it.zone.shape }.forEach {
    //                        it.scaleX = 1.0
    //                        it.scaleY = 1.0
    //                    }
    //
    //                    val center = Point2D(bounds.minX + bounds.width / 2, bounds.minY + bounds.height / 2)
    //
    //                    //val minRadius = BASE_CURVE_RADIUS / 5
    //
    //                    val minRadius = Math.min(bounds.width, bounds.height) / 2
    //
    //                    //println(center)
    //                    //debugPoints.add(center)
    //
    //                    contour = CircleCurve(center.x, center.y, minRadius, data.addedCurve)
    //                }
}