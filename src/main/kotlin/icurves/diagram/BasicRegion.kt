package icurves.diagram

import icurves.description.AbstractBasicRegion

/**
 * A basic region, br (element of BR), in an Euler diagram.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class BasicRegion(

        val abRegion: AbstractBasicRegion,

        val containingCurves: List<Curve>,

        val excludingCurves: List<Curve>) {
}