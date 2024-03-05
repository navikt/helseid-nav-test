package com.migesok.jaxb.adapter.javatime

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.MonthDay

/**
 * `XmlAdapter` mapping JSR-310 `MonthDay` to a string such as --12-03
 *
 *
 * String format details:
 *
 *  * [java.time.MonthDay.parse]
 *  * [java.time.MonthDay.toString]
 *
 *
 * @see javax.xml.bind.annotation.adapters.XmlAdapter
 *
 * @see java.time.MonthDay
 */
class MonthDayXmlAdapter : XmlAdapter<String?, MonthDay?>() {

    override fun unmarshal(stringValue : String?) : MonthDay? {
        return if (stringValue != null) MonthDay.parse(stringValue) else null
    }

    override fun marshal(value : MonthDay?) : String? {
        return if (value != null) value.toString() else null
    }
}