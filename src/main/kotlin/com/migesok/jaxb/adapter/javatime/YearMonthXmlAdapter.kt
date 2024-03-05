package com.migesok.jaxb.adapter.javatime

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.YearMonth

/**
 * `XmlAdapter` mapping JSR-310 `YearMonth` to a string such as 2007-12
 *
 *
 * String format details:
 *
 *  * [java.time.YearMonth.parse]
 *  * [java.time.YearMonth.toString]
 *
 *
 * @see javax.xml.bind.annotation.adapters.XmlAdapter
 *
 * @see java.time.YearMonth
 */
class YearMonthXmlAdapter : XmlAdapter<String?, YearMonth?>() {

    override fun unmarshal(stringValue : String?) : YearMonth? {
        return if (stringValue != null) YearMonth.parse(stringValue) else null
    }

    override fun marshal(value : YearMonth?) : String? {
        return if (value != null) value.toString() else null
    }
}