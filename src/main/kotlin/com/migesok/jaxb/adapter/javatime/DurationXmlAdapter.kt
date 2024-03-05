package com.migesok.jaxb.adapter.javatime

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.Duration

/**
 * `XmlAdapter` mapping JSR-310 `Duration` to ISO-8601 string
 *
 *
 * String format details:
 *
 *  * [java.time.Duration.parse]
 *  * [java.time.Duration.toString]
 *
 *
 * @see jakarta.xml.bind.annotation.adapters.XmlAdapter
 *
 * @see java.time.Duration
 */
class DurationXmlAdapter : XmlAdapter<String?, Duration?>() {

    override fun unmarshal(stringValue : String?) : Duration? {
        return if (stringValue != null) Duration.parse(stringValue) else null
    }

    override fun marshal(value : Duration?) : String? {
        return value?.toString()
    }
}