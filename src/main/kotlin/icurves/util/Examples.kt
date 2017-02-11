package icurves.util

import icurves.abstractdescription.AbstractDescription
import java.util.*

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
object Examples {

    val list = ArrayList<Pair<String, AbstractDescription> >()

    init {
        add("Venn-3", "a b c abc ab ac bc")
        add("Venn-4", "a b c d ab ac ad bc bd cd abc abd acd bcd abcd")
        add("Venn-5", "a b c d e ab ac ad ae bc bd be cd ce de abc abd abe acd ace ade bcd bce bde cde abcd abce abde acde bcde abcde")
    }

    private fun add(name: String, description: String) {
        list.add(name.to(AbstractDescription.from(description)))
    }
}