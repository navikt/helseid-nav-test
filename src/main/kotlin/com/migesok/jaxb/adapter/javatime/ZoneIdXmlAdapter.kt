package com.migesok.jaxb.adapter.javatime

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.ZoneId

/**
 * `XmlAdapter` mapping JSR-310 `ZoneId` and `ZoneOffset` to the time-zone ID string
 *
 *
 * Time-zone ID format details:
 *
 *  * [java.time.ZoneId.of]
 *  * [java.time.ZoneId.getId]
 *
 *
 * @see javax.xml.bind.annotation.adapters.XmlAdapter
 *
 * @see java.time.ZoneId
 *
 * @see java.time.ZoneOffset
 */
class ZoneIdXmlAdapter : XmlAdapter<String?, ZoneId?>() {

    override fun unmarshal(stringValue : String?) : ZoneId? {
        return if (stringValue != null) ZoneId.of(stringValue) else null
    }

    override fun marshal(value : ZoneId?) : String? {
        return if (value != null) value.getId() else null
    }
}