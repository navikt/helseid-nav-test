package com.migesok.jaxb.adapter.javatime

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.Year

/**
 * `XmlAdapter` mapping JSR-310 `Year` to ISO proleptic year number
 *
 *
 * Year number interpretation details:
 *
 *  * [java.time.Year.of]
 *  * [java.time.Year.getValue]
 *
 *
 * @see javax.xml.bind.annotation.adapters.XmlAdapter
 *
 * @see java.time.Year
 */
class YearXmlAdapter : XmlAdapter<Int?, Year?>() {

    override fun unmarshal(isoYearInt : Int?) : Year? {
        return if (isoYearInt != null) Year.of(isoYearInt) else null
    }

    override fun marshal(year : Year?) : Int? {
        return if (year != null) year.getValue() else null
    }
}