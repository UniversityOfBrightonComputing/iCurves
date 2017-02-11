package icurves.description

/**
 * An abstract curve \kappa (an element of K).
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class AbstractCurve(val label: String) : Comparable<AbstractCurve> {

    override fun compareTo(other: AbstractCurve) = this.label.compareTo(other.label)

    override fun toString() = label
}