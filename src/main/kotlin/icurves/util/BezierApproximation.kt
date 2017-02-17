package icurves.util

import icurves.CurvesApp
import icurves.algorithm.ClosedBezierSpline
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import javafx.scene.shape.CubicCurveTo
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import java.util.*

/**
 * The algorithm is adapted from AS3
 * http://www.cartogrammar.com/blog/actionscript-curves-update/
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
object BezierApproximation {

    private val z = 0.5
    private val angleFactor = 0.75

    fun smoothPath2(points: MutableList<Point2D>, smoothFactor: Int): List<Path> {

        val pair = ClosedBezierSpline.GetCurveControlPoints(points.toTypedArray())

        points.add(points[0])

        val result = arrayListOf<Path>()

        val firstPt = 0
        val lastPt = points.size

        for (i in firstPt..lastPt - 2){

            val path = Path()

            path.elements.add(MoveTo(points[i].x, points[i].y))

            val j = if (i == lastPt - 2) 0 else i + 1

            path.elements.add(CubicCurveTo(
                    pair.key[i].x, pair.key[i].y,
                    pair.value[j].x, pair.value[j].y,
                    //controlPts[i].second.x, controlPts[i].second.y,
                    //controlPts[i+1].first.x, controlPts[i+1].first.y,
                    points[i+1].x, points[i+1].y)
            )


            path.fill = Color.TRANSPARENT
            result.add(path)
        }

        return result
    }

    fun smoothPath(points: MutableList<Point2D>, smoothFactor: Int): List<Path> {



        // to make a closed cycle
        points.add(points[0])

        val result = arrayListOf<Path>()

        val firstPt = 0
        val lastPt = points.size

        // store the two control points (of a cubic Bezier curve) for each point
        val controlPts = arrayListOf<Pair<Point2D, Point2D>>()
        for (i in firstPt until lastPt) {
            controlPts.add(Pair(Point2D.ZERO, Point2D.ZERO))
        }

        // loop through all the points to get curve control points for each
        for (i in firstPt until lastPt) {

            val prevPoint = if (i-1 < 0) points[points.size-2] else points[i-1]
            val currPoint = points[i]
            val nextPoint = if (i+1 == points.size) points[1] else points[i+1]

            var a = prevPoint.distance(currPoint)
            if (a < 0.001) a = .001;		// Correct for near-zero distances, a cheap way to prevent division by zero

            var b = currPoint.distance(nextPoint)
            if (b < 0.001) b = .001;

            var c = prevPoint.distance(nextPoint)
            if (c < 0.001) c = .001

            var cos = (b*b + a*a - c*c) / (2*b*a)

            // ensure cos is between -1 and 1 so that Math.acos will work
            if (cos < -1)
                cos = -1.0
            else if (cos > 1)
                cos = 1.0

            // angle formed by the two sides of the triangle (described by the three points above) adjacent to the current point
            val C = Math.acos(cos)




            // Duplicate set of points. Start by giving previous and next points values RELATIVE to the current point.
            var aPt = Point2D(prevPoint.x-currPoint.x,prevPoint.y-currPoint.y);
            var bPt = Point2D(currPoint.x,currPoint.y);
            var cPt = Point2D(nextPoint.x-currPoint.x,nextPoint.y-currPoint.y);

            /*
            We'll be adding adding the vectors from the previous and next points to the current point,
            but we don't want differing magnitudes (i.e. line segment lengths) to affect the direction
            of the new vector. Therefore we make sure the segments we use, based on the duplicate points
            created above, are of equal length. The angle of the new vector will thus bisect angle C
            (defined above) and the perpendicular to this is nice for the line tangent to the curve.
            The curve control points will be along that tangent line.
            */

            if (a > b){
                //aPt.normalize(b);	// Scale the segment to aPt (bPt to aPt) to the size of b (bPt to cPt) if b is shorter.
                aPt = aPt.normalize().multiply(b)
            } else if (b > a){
                //cPt.normalize(a);	// Scale the segment to cPt (bPt to cPt) to the size of a (aPt to bPt) if a is shorter.
                cPt = cPt.normalize().multiply(a)
            }

            // Offset aPt and cPt by the current point to get them back to their absolute position.
            aPt = aPt.add(currPoint.x,currPoint.y)
            cPt = cPt.add(currPoint.x,currPoint.y)









            // Get the sum of the two vectors, which is perpendicular to the line along which our curve control points will lie.
            var ax = bPt.x-aPt.x	// x component of the segment from previous to current point
            var ay = bPt.y-aPt.y
            var bx = bPt.x-cPt.x	// x component of the segment from next to current point
            var by = bPt.y-cPt.y

            var rx = ax + bx;	// sum of x components
            var ry = ay + by;

            // Correct for three points in a line by finding the angle between just two of them
            if (rx == 0.0 && ry == 0.0){
                rx = -bx;	// Really not sure why this seems to have to be negative
                ry = by;
            }
            // Switch rx and ry when y or x difference is 0. This seems to prevent the angle from being perpendicular to what it should be.
            if (ay == 0.0 && by == 0.0){
                rx = 0.0;
                ry = 1.0;
            } else if (ax == 0.0 && bx == 0.0){
                rx = 1.0;
                ry = 0.0;
            }

            val theta = Math.atan2(ry,rx)	// angle of the new vector

            var controlDist = Math.min(a, b) * z;	// Distance of curve control points from current point: a fraction the length of the shorter adjacent triangle side
            val controlScaleFactor = C/Math.PI;	// Scale the distance based on the acuteness of the angle. Prevents big loops around long, sharp-angled triangles.

            controlDist *= ((1.0-angleFactor) + angleFactor*controlScaleFactor);	// Mess with this for some fine-tuning

            val controlAngle = theta+Math.PI/2;	// The angle from the current point to control points: the new vector angle plus 90 degrees (tangent to the curve).


            var controlPoint2 = polarToCartesian(controlDist, controlAngle);	// Control point 2, curving to the next point.
            var controlPoint1 = polarToCartesian(controlDist, controlAngle+Math.PI);	// Control point 1, curving from the previous point (180 degrees away from control point 2).

            // Offset control points to put them in the correct absolute position
            controlPoint1 = controlPoint1.add(currPoint);
            controlPoint2 = controlPoint2.add(currPoint);

            /*
            Haven't quite worked out how this happens, but some control points will be reversed.
            In this case controlPoint2 will be farther from the next point than controlPoint1 is.
            Check for that and switch them if it's true.
            */
            if (controlPoint2.distance(nextPoint) > controlPoint1.distance(nextPoint)){
                controlPts[i] = Pair(controlPoint2, controlPoint1)	// Add the two control points to the array in reverse order
            } else {
                controlPts[i] = Pair(controlPoint1, controlPoint2)	// Otherwise add the two control points to the array in normal order
            }
        }

        // a b c ab ac ad bc abc abd acd abcd




        CurvesApp.getInstance().settings.globalMap["controlPoints"] = controlPts












        val straightLines:Boolean = true;	// Change to true if you want to use lineTo for straight lines of 3 or more points rather than curves. You'll get straight lines but possible sharp corners!

        // Loop through points to draw cubic Bézier curves through the penultimate point, or through the last point if the line is closed.
        for (i in firstPt..lastPt - 2){

            val path = Path()

            path.elements.add(MoveTo(points[i].x, points[i].y))

            // Determine if multiple points in a row are in a straight line
            val isStraight:Boolean = ( ( i > 0 && Math.atan2(points[i].y-points[i-1].y, points[i].x-points[i-1].x)
                    == Math.atan2(points[i+1].y-points[i].y, points[i+1].x - points[i].x) )
                    || ( i < points.size - 2 && Math.atan2(points[i+2].y-points[i+1].y,points[i+2].x-points[i+1].x)
                    == Math.atan2(points[i+1].y-points[i].y,points[i+1].x-points[i].x) ) );

            if (straightLines && isStraight){
                path.elements.add(LineTo(points[i+1].x,points[i+1].y))
            } else {

                // BezierSegment instance using the current point, its second control point, the next point's first control point, and the next point

//                var t = 0.01
//                while (t < 1.01) {
//
//                    val point = getBezierValue(points[i], controlPts[i].second, controlPts[i+1].first, points[i+1], t)
//                    path.elements.add(LineTo(point.x, point.y))
//
//                    t += 1.0 / smoothFactor
//                }

                path.elements.add(CubicCurveTo(
                        controlPts[i].second.x, controlPts[i].second.y,
                        controlPts[i+1].first.x, controlPts[i+1].first.y,
                        points[i+1].x, points[i+1].y)
                )
            }

            path.fill = Color.TRANSPARENT
            result.add(path)
        }

        return result
    }


//
//    fun pathThruPoints(points: MutableList<Point2D>,
//                       smoothFactor: Int,
//                       closedCycle: Boolean = true,
//                       z: Double = 0.5,
//                       angleFactor: Double = 0.75): List<Path> {
//
//        if (closedCycle)
//            points.add(points[0])
//
//        val result = ArrayList<Path>()
//
//        // Ordinarily, curve calculations will start with the second point and go through the second-to-last point
//        var firstPt = 1;
//        var lastPt = points.size - 1;
//
//        // Check if this is a closed line (the first and last points are the same)
//        if (points[0].x == points[points.size-1].x && points[0].y == points[points.size-1].y){
//            // Include first and last points in curve calculations
//            firstPt = 0
//            lastPt = points.size
//        }
//
//        val controlPts = ArrayList<Pair<Point2D, Point2D>>()   // An array to store the two control points (of a cubic Bézier curve) for each point
//        for (i in 0..points.size - 1) {
//            controlPts.add(Pair(Point2D.ZERO, Point2D.ZERO))
//        }
//
//        // Loop through all the points (except the first and last if not a closed line) to get curve control points for each.
//        for (i in firstPt..lastPt - 1) {
//
//            // The previous, current, and next points
//            var p0 = if (i-1 < 0) points[points.size-2] else points[i-1];	// If the first point (of a closed line), use the second-to-last point as the previous point
//            var p1 = points[i];
//            var p2 = if (i+1 == points.size) points[1] else points[i+1];		// If the last point (of a closed line), use the second point as the next point
//
//            var a = p0.distance(p1)	// Distance from previous point to current point
//            if (a < 0.001) a = .001;		// Correct for near-zero distances, a cheap way to prevent division by zero
//            var b = p1.distance(p2);	// Distance from current point to next point
//            if (b < 0.001) b = .001;
//            var c = p0.distance(p2);	// Distance from previous point to next point
//            if (c < 0.001) c = .001;
//            var cos = (b*b+a*a-c*c)/(2*b*a);
//
//
//
//
//
//
//
//
//            // Make sure above value is between -1 and 1 so that Math.acos will work
//            if (cos < -1)
//                cos = -1.0;
//            else if (cos > 1)
//                cos = 1.0;
//
//            var C = Math.acos(cos);	// Angle formed by the two sides of the triangle (described by the three points above) adjacent to the current point
//            // Duplicate set of points. Start by giving previous and next points values RELATIVE to the current point.
//            var aPt = Point2D(p0.x-p1.x,p0.y-p1.y);
//            var bPt = Point2D(p1.x,p1.y);
//            var cPt = Point2D(p2.x-p1.x,p2.y-p1.y);
//
//            /*
//            We'll be adding adding the vectors from the previous and next points to the current point,
//            but we don't want differing magnitudes (i.e. line segment lengths) to affect the direction
//            of the new vector. Therefore we make sure the segments we use, based on the duplicate points
//            created above, are of equal length. The angle of the new vector will thus bisect angle C
//            (defined above) and the perpendicular to this is nice for the line tangent to the curve.
//            The curve control points will be along that tangent line.
//            */
//
//            if (a > b){
//                //aPt.normalize(b);	// Scale the segment to aPt (bPt to aPt) to the size of b (bPt to cPt) if b is shorter.
//                aPt = aPt.normalize().multiply(b)
//            } else if (b > a){
//                //cPt.normalize(a);	// Scale the segment to cPt (bPt to cPt) to the size of a (aPt to bPt) if a is shorter.
//                cPt = cPt.normalize().multiply(a)
//            }
//
//            // Offset aPt and cPt by the current point to get them back to their absolute position.
//            aPt = aPt.add(p1.x,p1.y);
//            cPt = cPt.add(p1.x,p1.y);
//
//            // Get the sum of the two vectors, which is perpendicular to the line along which our curve control points will lie.
//            var ax = bPt.x-aPt.x;	// x component of the segment from previous to current point
//            var ay = bPt.y-aPt.y;
//            var bx = bPt.x-cPt.x;	// x component of the segment from next to current point
//            var by = bPt.y-cPt.y;
//            var rx = ax + bx;	// sum of x components
//            var ry = ay + by;
//
//            // Correct for three points in a line by finding the angle between just two of them
//            if (rx == 0.0 && ry == 0.0){
//                rx = -bx;	// Really not sure why this seems to have to be negative
//                ry = by;
//            }
//            // Switch rx and ry when y or x difference is 0. This seems to prevent the angle from being perpendicular to what it should be.
//            if (ay == 0.0 && by == 0.0){
//                rx = 0.0;
//                ry = 1.0;
//            } else if (ax == 0.0 && bx == 0.0){
//                rx = 1.0;
//                ry = 0.0;
//            }
//
//            //var r = Math.sqrt(rx*rx+ry*ry);	// length of the summed vector - not being used, but there it is anyway
//            var theta = Math.atan2(ry,rx);	// angle of the new vector
//
//            var controlDist = Math.min(a,b)*z;	// Distance of curve control points from current point: a fraction the length of the shorter adjacent triangle side
//            var controlScaleFactor = C/Math.PI;	// Scale the distance based on the acuteness of the angle. Prevents big loops around long, sharp-angled triangles.
//
//            controlDist *= ((1.0-angleFactor) + angleFactor*controlScaleFactor);	// Mess with this for some fine-tuning
//
//            var controlAngle = theta+Math.PI/2;	// The angle from the current point to control points: the new vector angle plus 90 degrees (tangent to the curve).
//
//
//            var controlPoint2 = polarToCartesian(controlDist, controlAngle);	// Control point 2, curving to the next point.
//            var controlPoint1 = polarToCartesian(controlDist, controlAngle+Math.PI);	// Control point 1, curving from the previous point (180 degrees away from control point 2).
//
//            // Offset control points to put them in the correct absolute position
//            controlPoint1 = controlPoint1.add(p1.x,p1.y);
//            controlPoint2 = controlPoint2.add(p1.x,p1.y);
//
//            /*
//            Haven't quite worked out how this happens, but some control points will be reversed.
//            In this case controlPoint2 will be farther from the next point than controlPoint1 is.
//            Check for that and switch them if it's true.
//            */
//            if (controlPoint2.distance(p2) > controlPoint1.distance(p2)){
//                controlPts[i] = Pair(controlPoint2,controlPoint1);	// Add the two control points to the array in reverse order
//            } else {
//                controlPts[i] = Pair(controlPoint1,controlPoint2);	// Otherwise add the two control points to the array in normal order
//            }
//
//            // Uncomment to draw lines showing where the control points are.
//            /*
//            g.moveTo(p1.x,p1.y);
//            g.lineTo(controlPoint2.x,controlPoint2.y);
//            g.moveTo(p1.x,p1.y);
//            g.lineTo(controlPoint1.x,controlPoint1.y);
//            */
//        }
//
//
//        //
//        // CURVE CONSTRUCTION VIA ELEMENTS
//        //
//
//        //path.elements.add(MoveTo(points[0].x, points[0].y))
//
//
//
//        var straightLines:Boolean = true;	// Change to true if you want to use lineTo for straight lines of 3 or more points rather than curves. You'll get straight lines but possible sharp corners!
//
//        // Loop through points to draw cubic Bézier curves through the penultimate point, or through the last point if the line is closed.
//        for (i in firstPt..lastPt - 2){
//
//            val path = Path()
//
//            path.elements.add(MoveTo(points[i].x, points[i].y))
//
//            // Determine if multiple points in a row are in a straight line
//            var isStraight:Boolean = ( ( i > 0 && Math.atan2(points[i].y-points[i-1].y, points[i].x-points[i-1].x)
//                    == Math.atan2(points[i+1].y-points[i].y, points[i+1].x - points[i].x) )
//                    || ( i < points.size - 2 && Math.atan2(points[i+2].y-points[i+1].y,points[i+2].x-points[i+1].x)
//                    == Math.atan2(points[i+1].y-points[i].y,points[i+1].x-points[i].x) ) );
//
//            if (straightLines && isStraight){
//                path.elements.add(LineTo(points[i+1].x,points[i+1].y))
//            } else {
//
//                // BezierSegment instance using the current point, its second control point, the next point's first control point, and the next point
//                //var bezier:BezierSegment = new BezierSegment(points[i], controlPts[i].second, controlPts[i+1].first, points[i+1]);
//
//                // Construct the curve out of 100 segments (adjust number for less/more detail)
//
//                var t = 0.01
//                while (t < 1.01) {
//
//                    val point = getBezierValue(points[i], controlPts[i].second, controlPts[i+1].first, points[i+1], t)
//                    path.elements.add(LineTo(point.x, point.y))
//
//                    t += 1.0 / smoothFactor
//                }
//            }
//
//            path.fill = Color.TRANSPARENT
//            result.add(path)
//        }
//
//        // moveTo
//        // lineTo * 10 * numPoints
//        // closePath
//
//        //println("Path elements: ${path.elements}")
//
////        if (closedCycle)
////            path.elements.add(ClosePath())
//
//        //path.fill = Color.TRANSPARENT
//
//        return result
//    }

    /**
     * Polar to Cartesian conversion.
     */
    private fun polarToCartesian(len: Double, angle: Double): Point2D {
        return Point2D(len * Math.cos(angle), len * Math.sin(angle))
    }

    /**
     * Cubic bezier equation from
     * http://stackoverflow.com/questions/5634460/quadratic-bezier-curve-calculate-point
     */
    private fun getBezierValue(p1: Point2D, p2: Point2D, p3: Point2D, p4: Point2D, t: Double): Point2D {
        val x = Math.pow(1 - t, 3.0) * p1.x + 3 * t * Math.pow(1 - t, 2.0) * p2.x + 3 * t*t * (1 - t) * p3.x + t*t*t*p4.x
        val y = Math.pow(1 - t, 3.0) * p1.y + 3 * t * Math.pow(1 - t, 2.0) * p2.y + 3 * t*t * (1 - t) * p3.y + t*t*t*p4.y
        return Point2D(x, y)
    }
}