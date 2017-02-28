package icurves.util

import icurves.description.Description
import java.util.*

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
object Examples {

    val list = ArrayList<Pair<String, Description> >()

    init {
        add("Venn-3", "a b c abc ab ac bc")
        add("Venn-4", "a b c d ab ac ad bc bd cd abc abd acd bcd abcd")
        add("Venn-5", "a b c d e ab ac ad ae bc bd be cd ce de abc abd abe acd ace ade bcd bce bde cde abcd abce abde acde bcde abcde")

        add("Double Piercing", "a b c ab ac af ag bc be cd abc abe abf abg acd acf afg bcd bce abcd abce abcf abfg")
        add("Double Piercing 1", "a b c d ab ac ad ae bc bd cd abc abd acd ace bcd abcd acde")
        add("Double Piercing 2", "p q r pq pr qr qs rs pqs prs qrs qrt pqrs")
        add("Double Piercing 3", "a b c d ac ad bc bd cd ce df abd acd ace bcd bce bdf cdf abcd abce bcdf")

        add("Edge Route", "a b c ab ac bc bd bf abc abd abf bcd bcf bdf abcd abdf bcdf")
    }

    private fun add(name: String, description: String) {
        list.add(name.to(Description.from(description)))
    }
}