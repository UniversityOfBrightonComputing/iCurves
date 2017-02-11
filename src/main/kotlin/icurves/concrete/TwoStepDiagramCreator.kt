package icurves.concrete

import icurves.abstractdescription.AbstractBasicRegion
import icurves.abstractdescription.AbstractCurve
import icurves.abstractdescription.AbstractDescription
import icurves.decomposition.DecomposerFactory
import icurves.decomposition.DecompositionStrategyType
import icurves.graph.EulerDualGraph
import icurves.recomposition.RecomposerFactory
import icurves.recomposition.RecompositionStrategyType
import icurves.util.CannotDrawException
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.stream.Collectors

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class TwoStepDiagramCreator : DiagramCreator(
        DecomposerFactory.newDecomposer(DecompositionStrategyType.INNERMOST),
        RecomposerFactory.newRecomposer(RecompositionStrategyType.DOUBLY_PIERCED)) {

    private val log = LogManager.getLogger(javaClass)

    private fun removeCurveFromDiagram(curve: AbstractCurve, diagram: ConcreteDiagram, size: Int): ConcreteDiagram {
        // generate a concrete diagram with the removed curve
        val newCurves = TreeSet(diagram.actualDescription.curves)
        run {
            val it = newCurves.iterator()
            while (it.hasNext()) {
                if (it.next().matchesLabel(curve)) {
                    it.remove()
                }
            }
        }

        val newZones = TreeSet(diagram.actualDescription.zones)
        val it = newZones.iterator()
        while (it.hasNext()) {
            val zone = it.next()
            if (zone.hasCurveWithlabel(curve.label)) {
                it.remove()
            }
        }

        val actual = AbstractDescription(newCurves, newZones)

        diagram.circles.removeAll { it.curve.matchesLabel(curve) }

        val contours = ArrayList(diagram.contours)

        val concreteDiagram = ConcreteDiagram(diagram.originalDescription, actual,
                diagram.circles, diagram.curveToContour, size, *contours.toTypedArray())
        return concreteDiagram
    }

    override fun createDiagram(description: AbstractDescription, size: Int): ConcreteDiagram {
        initial = description

        try {
            log.debug("Using Original Algorithm")
            val diagram0 = super.createDiagram(description, size)

            val duplicates = diagram0.findDuplicateContours()
            if (duplicates.isEmpty())
                return diagram0

            log.debug("Running post-processing")
            var d = postProcess(diagram0, size)

            // REORDER

            //println("Generating for: ${d.actualDescription}")
            dSteps = null
            rSteps = null
            d = super.createDiagram(d.actualDescription, size)

            return postProcess(d, size)
        } catch(e: Exception) {
            return BetterDiagramCreator(DecomposerFactory.newDecomposer(DecompositionStrategyType.INNERMOST),
                    RecomposerFactory.newRecomposer(RecompositionStrategyType.DOUBLY_PIERCED)).createDiagram(description, size)
        }
    }

    // Fails: a b d ab ac ad bd acd

    private fun postProcess(diagram0: ConcreteDiagram, size: Int): ConcreteDiagram {
        val duplicates = diagram0.findDuplicateContours()

        var d = diagram0

        for (curve in duplicates.keys) {
            val iCirclesDiagramNew = removeCurveFromDiagram(curve, d, size)

            val ad = d.getActualDescription()

            log.debug("Actual Description: " + ad)
            log.debug("Removed curve description: ${iCirclesDiagramNew.actualDescription}")

            var zones = ad.zones.filter({ z -> z.hasCurveWithlabel(curve.label) })

            log.debug("Zones in " + curve + ":" + zones.toString())

            zones = zones.map({ z -> z.moveOutsideNew(curve) })

            log.debug("Zones that will be in " + curve + ":" + zones.toString())

            val graph = EulerDualGraph(iCirclesDiagramNew)

            //CannotDrawException("No cycle found")
            val cycleMaybe = graph.computeCycle(zones)
            //val cycleMaybe = graph.computeCycleIncomplete(zones)

//            if (!cycleMaybe.isPresent)
//                continue

            val cycle = cycleMaybe.orElseThrow { CannotDrawException("No cycle found for $zones") }

            println("Found approriate: $cycle")

            // create new contour
            val contour = PathContour(curve, cycle.path)

            val newCurves = TreeSet(iCirclesDiagramNew.actualDescription.curves)
            run {
                val iter = newCurves.iterator()
                while (iter.hasNext()) {
                    if (iter.next().matchesLabel(curve)) {
                        iter.remove()
                    }
                }
            }

            newCurves.add(curve)

            // GENERATE ACTUAL DESC
            val newZones = TreeSet(iCirclesDiagramNew.actualDescription.zones)
            val iter = newZones.iterator()
            while (iter.hasNext()) {
                val zone = iter.next()
                if (zone.hasCurveWithlabel(curve.label)) {
                    iter.remove()
                }
            }

            for (zone in cycle.nodes.map { it.zone.abstractZone }) {
                newZones.add(zone.moveInside(curve))
            }

            val actual = AbstractDescription(newCurves, newZones)

            println("New actual: $actual")

            // put mapping from abstract to conrete curve
            iCirclesDiagramNew.curveToContour.put(curve, contour)

            val contours = ArrayList(iCirclesDiagramNew.contours)
            contours.add(contour)

            d = ConcreteDiagram(iCirclesDiagramNew.originalDescription, actual,
                    iCirclesDiagramNew.circles, iCirclesDiagramNew.curveToContour, size, *contours.toTypedArray())

        }

        return d
    }

    private lateinit var initial: AbstractDescription

    override fun getInitialDiagram(): AbstractDescription {
        return initial
    }
}