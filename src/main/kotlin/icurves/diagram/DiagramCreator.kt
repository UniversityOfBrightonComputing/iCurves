package icurves.diagram

import icurves.diagram.curve.PathCurve
import icurves.diagram.curve.PolygonCurve
import icurves.decomposition.DecomposerFactory
import icurves.description.AbstractBasicRegion
import icurves.description.AbstractCurve
import icurves.description.Description
import icurves.diagram.curve.CircleCurve
import icurves.graph.MED
import icurves.guifx.SettingsController
import icurves.recomposition.RecomposerFactory
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
 * Diagram creator that uses Hamiltonian cycles to ensure
 * that any diagram description is drawable.
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
     * Abstract zones we **currently** occupy.
     */
    private val abstractZones = ArrayList<AbstractBasicRegion>()
    val concreteShadedZones = ArrayList<BasicRegion>()

    lateinit var modifiedDual: MED

    val debugPoints = arrayListOf<Point2D>()
    val debugShapes = arrayListOf<Shape>()

    fun createDiagram(description: Description) {

        // all we need is decomposition; recomposition is almost no-op
        val dSteps = DecomposerFactory.newDecomposer(settings.decompType).decompose(description)
        val rSteps = RecomposerFactory.newRecomposer().recompose(dSteps)

        for (i in rSteps.indices) {
            val data = rSteps[i].addedCurveData

            if (i == 0) {

                // base case 1 curve
                val contour = CircleCurve(data.addedCurve, BASE_CURVE_RADIUS, BASE_CURVE_RADIUS, BASE_CURVE_RADIUS)
                curveToContour[data.addedCurve] = contour

                abstractZones.addAll(data.newZones)

            } else if (i == 1) {

                // base case 2 curves
                val contour = CircleCurve(data.addedCurve, (BASE_CURVE_RADIUS + 0) * 2, BASE_CURVE_RADIUS, BASE_CURVE_RADIUS)
                curveToContour[data.addedCurve] = contour

                abstractZones.addAll(data.newZones)

            } else if (i == 2) {

                // base case 3 curves
                val contour = CircleCurve(data.addedCurve, (BASE_CURVE_RADIUS + 0) * 1.5, BASE_CURVE_RADIUS * 2, BASE_CURVE_RADIUS)
                curveToContour[data.addedCurve] = contour

                abstractZones.addAll(data.newZones)

            } else {    // evaluating 4th+ curve

                createMED()

                log.trace("Searching cycle with zones: ${data.splitZones}")

                val cycle = modifiedDual.computeCycle(
                        data.splitZones
                )
                        // if the rest of the app worked properly, this will never happen because there is >= 1 Hamiltonian cycles
                .orElseThrow { RuntimeException("Failed to find cycle") }

                var curve: Curve = PathCurve(data.addedCurve, cycle.path)

                //var curve: Curve = PolygonCurve(data.addedCurve, cycle.nodes.map { it.point })

                // smooth curves if required
                if (settings.useSmooth()) {

                    Profiler.start("Smoothing")

                    val pathSegments = BezierApproximation.pathThruPoints(cycle.nodes.map { it.point }.toMutableList(), settings.smoothFactor)

                    val newPath = Path()

                    // add moveTo
                    newPath.elements.add(cycle.path.elements[0])

                    for (j in cycle.nodes.indices) {
                        val node1 = cycle.nodes[j]
                        val node2 = if (j == cycle.nodes.size - 1) cycle.nodes[0] else cycle.nodes[j + 1]

                        // this is to enable joining MED ring with internal edges at C2 continuity
//                        // remove first moveTo
//                        pathSegments[j].elements.removeAt(0)
//
//                        // add to new path
//                        newPath.elements.addAll(pathSegments[j].elements)
//
//                        if (true)
//                            continue


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

                            val lineTo = cycle.path.elements[j + 1] as LineTo

                            newPath.elements.addAll(lineTo)
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

                    curve = PathCurve(data.addedCurve, newPath)

                    Profiler.end("Smoothing")
                }

                curveToContour[data.addedCurve] = curve

                // we might've used more zones to get a cycle, so we make sure we capture all of the used ones
                // we also call distinct() to ensure we don't reuse the outside zone more than once
                abstractZones.addAll(cycle.nodes.map { it.zone.abRegion.moveInside(data.addedCurve) }.distinct())
            }
        }

        // create MED for final diagram if required
        if (settings.showMED())
            createMED()

        log.trace("Generating shaded zones")

        val shaded = abstractZones.minus(description.zones)

        concreteShadedZones.addAll(shaded.map { BasicRegion(it, curveToContour) })
    }

    /**
     * Needs to be generated every time because contours change zones.
     *
     * TODO: we could potentially only compute zones that have been changed by the curve
     */
    private fun createMED() {
        log.trace("Creating MED")

        val concreteZones = abstractZones.map { BasicRegion(it, curveToContour) }

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