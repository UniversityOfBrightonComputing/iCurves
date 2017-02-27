package icurves.graph

import icurves.CurvesApp
import icurves.algorithm.EdgeRouter
import icurves.description.AbstractBasicRegion
import icurves.description.AbstractCurve
import icurves.diagram.BasicRegion
import icurves.diagram.Curve
import icurves.graph.cycles.CycleFinder
import icurves.guifx.SettingsController
import icurves.util.Converter
import icurves.util.Profiler
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import javafx.scene.shape.*
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream

@Suppress("UNCHECKED_CAST")
/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class MED(val allZones: List<BasicRegion>, private val allContours: Map<AbstractCurve, Curve>) {

    private val log = LogManager.getLogger(javaClass)

    private val CONTROL_POINT_STEP = 5

    val nodes: MutableList<EulerDualNode>
    val edges: MutableList<EulerDualEdge>
    lateinit var cycles: List<GraphCycle<EulerDualNode, EulerDualEdge>>

    private val settings: SettingsController

    init {
        settings = CurvesApp.getInstance().settings

        Profiler.start("Creating EGD nodes")

        nodes = if (settings.isParallel) computeNodesParallel() else computeNodesSequential()

        Profiler.end("Creating EGD nodes")

        // go through each pair of nodes
        val pairs = ArrayList< Pair<EulerDualNode, EulerDualNode> >()

        for (i in nodes.indices) {
            var j = i + 1
            while (j < nodes.size) {
                val node1 = nodes[i]
                val node2 = nodes[j]

                pairs.add(node1.to(node2))

                j++
            }
        }

        Profiler.start("Creating EGD edges")

        edges = computeEdges(pairs)

        Profiler.end("Creating EGD edges")

        /*
         * MED generation
         * 1. get bounds of the diagram
         * 2. find center and radius
         * 3. get regions adjacent to outside and create edges
         * 4. create extra "arc" edges around the diagram
         */

        val bounds = allZones.map { it.getShape().layoutBounds }

        val minX = bounds.map { it.minX }.min()
        val minY = bounds.map { it.minY }.min()
        val maxX = bounds.map { it.maxX }.max()
        val maxY = bounds.map { it.maxY }.max()

        val center = Point2D((minX!! + maxX!!) / 2, (minY!! + maxY!!) / 2)

        val w = (maxX - minX) / 2
        val h = (maxY - minY) / 2

        // half diagonal of the bounds rectangle + distance between diagram and MED
        val radius = Math.sqrt(w*w + h*h) + settings.medSize

        Profiler.start("Creating MED nodes")

        val polygonMED = Converter.toPolygon2D(Converter.makePolygon(radius.toInt(), 16))
        val outside = BasicRegion(AbstractBasicRegion.OUTSIDE, allContours)

        val nodesMED = polygonMED.vertices()
                .map { EulerDualNode(outside, Point2D(it.x(), it.y()).subtract(w/2, h/2)) }


        // add the adjacent edges between outside in inside

        nodes.filter { it.zone.isTopologicallyAdjacent(outside) }
                .forEach { node ->
                    val closestMEDNode = nodesMED.sortedBy { it.point.distance(node.point) }.first()

                    edges.add(EulerDualEdge(node, closestMEDNode,
                            Line(node.point.x, node.point.y, closestMEDNode.point.x, closestMEDNode.point.y)))
                }

//        val nodesMED = computeMEDNodes(center, radius)

        // then add nodesMED to nodes
        nodes.addAll(nodesMED)

        Profiler.end("Creating MED nodes")

        Profiler.start("Creating MED edges")

        // sort nodes along the MED ring
        // sorting is CCW from 0 (right) to 360
        Collections.sort(nodesMED, { node1, node2 ->
            val v1 = node1.point.subtract(center)
            val angle1 = vectorToAngle(v1)

            val v2 = node2.point.subtract(center)
            val angle2 = vectorToAngle(v2)

            (angle1 - angle2).toInt()
        })

        for (i in nodesMED.indices) {
            val node1 = nodesMED[i]
            val node2 = if (i == nodesMED.size - 1) nodesMED[0] else nodesMED[i+1]

            val p1 = node1.point
            val p2 = node2.point

            edges.add(EulerDualEdge(node1, node2, Line(p1.x, p1.y, p2.x, p2.y)))
        }

        //computeMEDRingEdges(nodesMED, center, radius)


        Profiler.end("Creating MED edges")

        initCycles()
    }

    private fun computeNodesSequential(): MutableList<EulerDualNode> {
        return allZones.map { createNode(it) }.toMutableList()
    }

    private fun computeNodesParallel(): MutableList<EulerDualNode> {
        return Stream.of(*allZones.toTypedArray())
                .parallel()
                .map { createNode(it) }
                .collect(Collectors.toList()) as MutableList<EulerDualNode>

        // hack that allows us to do parallel stream in kotlin

        //        return IntStream.range(0, allZones.size)
        //                .parallel()
        //                .mapToObj { EulerDualNode(allZones[it], allZones[it].center) }
        //                .collect(Collectors.toList()) as MutableList<EulerDualNode>
    }

    /**
     * Computes EGD edges based on given pairs of nodes.
     * An edge is constructed if zones of nodes are topologically adjacent.
     * Runs in parallel mode based on settings.
     */
    private fun computeEdges(pairs: List< Pair<EulerDualNode, EulerDualNode> >): MutableList<EulerDualEdge> {

        log.trace("Computing edges")

        var stream = Stream.of(*pairs.toTypedArray())

        if (settings.isParallel) {
            stream = stream.parallel()
        }

        return stream.filter { it.first.zone.isTopologicallyAdjacent(it.second.zone) }
                .map { createEdge(it.first, it.second) }
                .collect(Collectors.toList()) as MutableList<EulerDualEdge>
    }

    private fun createNode(zone: BasicRegion): EulerDualNode {
        return EulerDualNode(zone, zone.center)
    }

    /**
     * Creates an Euler dual edge between [node1] and [node2] represented by
     * a Bezier curve.
     */
    private fun createEdge(node1: EulerDualNode, node2: EulerDualNode): EulerDualEdge {
        log.trace("Creating edge: ${node1.zone} - ${node2.zone}")

        //Profiler.start("Creating edge")

        val p1 = node1.zone.center
        val p2 = node2.zone.center

        val line = Line(p1.x, p1.y, p2.x, p2.y)

        // the new curve segment must pass through the straddled curve
        // and only through that curve
        val curve = node1.zone.abRegion.getStraddledContour(node2.zone.abRegion).get()

        log.trace("Searching ${node1.zone} - ${node2.zone} : $curve")

        if (!isOK(line, curve, allContours.values.toList())) {
            val poly = EdgeRouter.route(node1.zone, node2.zone)

            val points = arrayListOf<Double>()

            //settings.globalMap["astar"] = poly

            // shorten vertices
            var i = 0
            while (i < poly.points.size - 2) {
                points.add(poly.points[i])
                points.add(poly.points[i+1])

                i += 32
            }

            points.addAll(poly.points.takeLast(2))

            return EulerDualEdge(node1, node2, Polyline(*points.toDoubleArray()))
        }

        return EulerDualEdge(node1, node2, line)
    }

    /**
     * Does curve segment [q] only pass through [actual] curve.
     */
    private fun isOK(q: Shape, actual: AbstractCurve, curves: List<Curve>): Boolean {
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

    private fun computeMEDNodes(center: Point2D, radius: Double): List<EulerDualNode> {

        log.trace("Computing MED nodes")

        val outside = BasicRegion(AbstractBasicRegion.OUTSIDE, allContours)

        var stream = Stream.of(*nodes.toTypedArray())

        if (settings.isParallel) {
            stream = stream.parallel()
        }

        return stream.filter { it.zone.isTopologicallyAdjacent(outside) }
                .map {
                    val vectorToMED = it.zone.center.subtract(center)
                    val length = radius - vectorToMED.magnitude()

                    // from zone center to closest point on MED
                    val vector = vectorToMED.normalize().multiply(length)

                    val p1 = it.zone.center
                    val p2 = it.zone.center.add(vector)

                    // TODO: this can be replaced with a line

                    val q = QuadCurve()
                    q.fill = null
                    q.stroke = Color.BLACK
                    q.startX = p1.x
                    q.startY = p1.y
                    q.endX = p2.x
                    q.endY = p2.y

                    q.controlX = p1.midpoint(p2).x
                    q.controlY = p1.midpoint(p2).y

                    // make "distinct" nodes so that jgrapht doesn't think it's a loop
                    val node = EulerDualNode(BasicRegion(AbstractBasicRegion.OUTSIDE, allContours), p2)

                    edges.add(EulerDualEdge(it, node, q))

                    return@map node
                }
                .collect(Collectors.toList()) as List<EulerDualNode>
    }

    /**
     * @param nodesMED nodes of MED placed in the outside zone
     * @param center center of the MED bounding circle
     * @param radius radius of the MED bounding circle
     */
    private fun computeMEDRingEdges(nodesMED: List<EulerDualNode>, center: Point2D, radius: Double) {

        // sort nodes along the MED ring
        // sorting is CCW from 0 (right) to 360
        Collections.sort(nodesMED, { node1, node2 ->
            val v1 = node1.point.subtract(center)
            val angle1 = vectorToAngle(v1)

            val v2 = node2.point.subtract(center)
            val angle2 = vectorToAngle(v2)

            (angle1 - angle2).toInt()
        })

        for (i in nodesMED.indices) {
            val node1 = nodesMED[i]
            val node2 = if (i == nodesMED.size - 1) nodesMED[0] else nodesMED[i+1]

            val p1 = node1.point
            val p2 = node2.point

            val v1 = node1.point.subtract(center)
            val angle1 = vectorToAngle(v1)

            val v2 = node2.point.subtract(center)
            val angle2 = vectorToAngle(v2)

            //println("$angle1 -> $angle2")
            // a b c d e ab ac ad bc bd de abc abcd

            // extent of arc in degrees
            var extent = angle2 - angle1

            if (extent < 0) {
                extent += 360
            }

            val arc = Arc(center.x, center.y, radius, radius, angle1, extent)

            with(arc) {
                fill = null
                stroke = Color.BLACK

                userData = p1.to(p2)
                properties["sweep"] = angle1 < angle2
            }

            edges.add(EulerDualEdge(node1, node2, arc))
        }
    }

    /**
     * Compute all valid cycles.
     * A cycle is valid if it can be used to embed a curve.
     *
     * inside:
     * p q r pq pr qr pqrs pqs prs qrs ps qs rs
     *
     * p q r pq pr qr qs rs pqs prs qrs qrt pqrs (fails)
     */
    private fun computeValidCycles(): List<GraphCycle<EulerDualNode, EulerDualEdge>> {
        val graph = CycleFinder<EulerDualNode, EulerDualEdge>(EulerDualEdge::class.java)
        nodes.forEach { graph.addVertex(it) }
        edges.forEach { graph.addEdge(it.v1, it.v2, it) }

        Profiler.start("Enumerating cycles")

        val allCycles = graph.computeCycles()

        Profiler.end("Enumerating cycles")

        return IntStream.range(0, allCycles.size)
                .parallel()
                .mapToObj { allCycles[it] }
                .filter { cycle ->

            log.trace("Checking cycle: $cycle")

            // this ensures that we do not allow same vertices in the cycle
            // unless it's the outside vertex
            cycle.nodes.groupBy { it.zone.abRegion.toString() }.forEach {
                if (it.key != "{}" && it.value.size > 1) {
                    log.trace("Discarding cycle because ${it.key} is present ${it.value.size} times")
                    return@filter false
                }
            }

            cycle.smoothingData = arrayListOf()

            val path = Path()
            val moveTo = MoveTo(cycle.nodes.get(0).point.x, cycle.nodes.get(0).point.y)
            path.elements.addAll(moveTo)

            var tmpPoint = cycle.nodes.get(0).point

            // add the first point (move to)
            cycle.smoothingData.add(tmpPoint)

            cycle.edges.map { it.curve }.forEach { q ->

                when(q) {
                    is QuadCurve -> {
                        val quadCurveTo = QuadCurveTo()

                        // we do this coz source and end vertex might be swapped
                        if (tmpPoint == Point2D(q.startX, q.startY)) {
                            quadCurveTo.x = q.endX
                            quadCurveTo.y = q.endY
                        } else {
                            quadCurveTo.x = q.startX
                            quadCurveTo.y = q.startY
                        }

                        tmpPoint = Point2D(quadCurveTo.x, quadCurveTo.y)

                        quadCurveTo.controlX = q.controlX
                        quadCurveTo.controlY = q.controlY

                        path.elements.addAll(quadCurveTo)

                        throw RuntimeException("CANNOT BE")
                    }

                    is Arc -> {

                        val p1 = (q.userData as Pair<Point2D, Point2D>).first
                        val p2 = (q.userData as Pair<Point2D, Point2D>).second

                        // a b c d ab ac bc bd cd abc bcd
                        // a b c d ab ac bc bd cd ce abc ace bcd bce abce

                        val arcTo = ArcTo()
                        arcTo.radiusX = q.radiusX
                        arcTo.radiusY = q.radiusY
                        arcTo.xAxisRotation = q.startAngle

                        val arcCenter = Point2D(q.centerX, q.centerY)

                        // p1 is start then
                        if (tmpPoint == p1) {
                            arcTo.x = p2.x
                            arcTo.y = p2.y
                        } else {
                            arcTo.x = p1.x
                            arcTo.y = p1.y
                        }

//                        val angle1 = vectorToAngle(tmpPoint)
//                        val angle2 = vectorToAngle(Point2D(arcTo.x, arcTo.y))

                        // set start point for this arcTo
                        settings.globalMap[arcTo] = tmpPoint

//                        var angle2 = q.startAngle + q.length
//                        if (angle2 >= 360)
//                            angle2 -= 360

                        // TODO: we could alternatively check if arc is fine?
                        arcTo.isSweepFlag = q.properties["sweep"] as Boolean
                        //arcTo.isSweepFlag = angle1 < angle2

                        //println("$angle1 -> $angle2 sweep: ${arcTo.isSweepFlag}")

                        tmpPoint = Point2D(arcTo.x, arcTo.y)

                        path.elements.add(arcTo)

                        throw RuntimeException("CANNOT BE")
                    }

                    is Line -> {
                        val lineTo = LineTo()

                        // we do this coz source and end vertex might be swapped
                        if (tmpPoint == Point2D(q.startX, q.startY)) {
                            lineTo.x = q.endX
                            lineTo.y = q.endY
                        } else {
                            lineTo.x = q.startX
                            lineTo.y = q.startY
                        }

                        tmpPoint = Point2D(lineTo.x, lineTo.y)

                        path.elements.add(lineTo)
                        cycle.smoothingData.add(tmpPoint)
                    }

                    is Polyline -> {

                        val start = Point2D(q.points[0], q.points[1])
                        val end = Point2D(q.points[q.points.size-2], q.points[q.points.size-1])

                        val normalOrder: Boolean

                        // we do this coz source and end vertex might be swapped
                        if (tmpPoint == start) {
                            normalOrder = true
                        } else {
                            normalOrder = false
                        }

                        tmpPoint = end

                        // e.g. a b c ab ac bc bd bf abc abd abf bcd bcf bdf abcd abdf bcdf

                        if (normalOrder) {
                            var i = 2
                            while (i < q.points.size) {

                                val point = Point2D(q.points[i], q.points[++i])
                                val lineTo = LineTo(point.x, point.y)

                                path.elements.add(lineTo)
                                cycle.smoothingData.add(point)

                                i++
                            }
                        } else {
                            var i = q.points.size-3
                            while (i > 0) {

                                val point = Point2D(q.points[i-1], q.points[i])
                                val lineTo = LineTo(point.x, point.y)

                                path.elements.add(lineTo)
                                cycle.smoothingData.add(point)

                                i -= 2
                            }
                        }
                    }

                    else -> {
                        throw IllegalArgumentException("Unknown edge shape: $q")
                    }
                }
            }

            // drop last duplicate of first moveTO
            cycle.smoothingData.removeAt(cycle.smoothingData.size - 1)

            path.elements.add(ClosePath())
            path.fill = Color.TRANSPARENT

            cycle.path = path

            // we filter those vertices that are not part of the cycle
            // then we check if filtered vertices are inside the cycle
            nodes.filter {

                // we do not need to check for ouside zone right?
                !cycle.contains(it)
                // fails for some reason
                //!cycle.nodes.contains(it)

            }.forEach {

                log.trace("Checking vertex $it")

                if (path.contains(it.point)) {
                    log.trace("Discarding cycle because of inside vertex: ${it.point}")
                    return@filter false
                }
            }

            log.trace("Cycle is valid")
            return@filter true
        }
        .collect(Collectors.toList()) as List<GraphCycle<EulerDualNode, EulerDualEdge>>



        // SEQUENTIAL

//        return allCycles.filter { cycle ->
//
//            log.trace("Checking cycle: $cycle")
//
//            // this ensures that we do not allow same vertices in the cycle
//            // unless it's the outside vertex
//            cycle.nodes.groupBy { it.zone.abstractZone.toString() }.forEach {
//                if (it.key != "{}" && it.value.size > 1) {
//                    log.trace("Discarding cycle because ${it.key} is present ${it.value.size} times")
//                    return@filter false
//                }
//            }
//
//            val path = Path()
//            val moveTo = MoveTo(cycle.nodes.get(0).point.x, cycle.nodes.get(0).point.y)
//            path.elements.addAll(moveTo)
//
//            tmpPoint = cycle.nodes.get(0).point
//
//            cycle.edges.map { it.curve }.forEach { q ->
//
//                when(q) {
//                    is QuadCurve -> {
//                        val quadCurveTo = QuadCurveTo()
//
//                        // we do this coz source and end vertex might be swapped
//                        if (tmpPoint == Point2D(q.startX, q.startY)) {
//                            quadCurveTo.x = q.endX
//                            quadCurveTo.y = q.endY
//                        } else {
//                            quadCurveTo.x = q.startX
//                            quadCurveTo.y = q.startY
//                        }
//
//                        tmpPoint = Point2D(quadCurveTo.x, quadCurveTo.y)
//
//                        quadCurveTo.controlX = q.controlX
//                        quadCurveTo.controlY = q.controlY
//
//                        path.elements.addAll(quadCurveTo)
//                    }
//
//                    is Arc -> {
//
//                        val p1 = (q.userData as Pair<Point2D, Point2D>).first
//                        val p2 = (q.userData as Pair<Point2D, Point2D>).second
//
//                        // a b c d ab ac bc bd cd abc bcd
//                        // a b c d ab ac bc bd cd ce abc ace bcd bce abce
//
//                        val arcTo = ArcTo()
//                        arcTo.radiusX = q.radiusX
//                        arcTo.radiusY = q.radiusY
//                        arcTo.xAxisRotation = q.startAngle
//
//                        val arcCenter = Point2D(q.centerX, q.centerY)
//
//                        // p1 is start then
//                        if (tmpPoint == p1) {
//                            arcTo.x = p2.x
//                            arcTo.y = p2.y
//                        } else {
//                            arcTo.x = p1.x
//                            arcTo.y = p1.y
//                        }
//
//                        arcTo.isSweepFlag = q.properties["sweep"] as Boolean
//
//                        tmpPoint = Point2D(arcTo.x, arcTo.y)
//
//                        path.elements.add(arcTo)
//                    }
//
//                    else -> {
//                        throw IllegalArgumentException("Unknown edge shape: $q")
//                    }
//                }
//            }
//
//            path.elements.add(ClosePath())
//            path.fill = Color.TRANSPARENT
//
//            cycle.path = path
//
//            // we filter those vertices that are not part of the cycle
//            // then we check if filtered vertices are inside the cycle
//            nodes.filter {
//
//                // we do not need to check for ouside zone right?
//                !cycle.contains(it)
//                // fails for some reason
//                //!cycle.nodes.contains(it)
//
//            }.forEach {
//
//                log.trace("Checking vertex $it")
//
//                if (path.contains(it.point)) {
//                    log.trace("Discarding cycle because of inside vertex: ${it.point}")
//                    return@filter false
//                }
//            }
//
//            log.trace("Cycle is valid")
//            return@filter true
//        }
    }

    fun initCycles() {
        Profiler.start("Computing all cycles")

        cycles = computeValidCycles()

        Profiler.end("Computing all cycles")

        log.debug("Valid cycles: $cycles")
    }

    fun computeCycle(zonesToSplit: List<AbstractBasicRegion>): Optional<GraphCycle<EulerDualNode, EulerDualEdge>> {
        return Optional.ofNullable(cycles.filter { it.nodes.map { it.zone.abRegion }.containsAll(zonesToSplit) }.firstOrNull())
    }

    /**
     * @return angle in [0..360]
     */
    private fun vectorToAngle(v: Point2D): Double {
        var angle = -Math.toDegrees(Math.atan2(v.y, v.x))

        if (angle < 0) {
            val delta = 180 - (-angle)
            angle = delta + 180
        }

        return angle
    }
}