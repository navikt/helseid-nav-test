package com.migesok.jaxb.adapter.javatime

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.OffsetTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery

/**
 * `XmlAdapter` mapping JSR-310 `OffsetTime` to ISO-8601 string
 *
 *
 * String format details: [java.time.format.DateTimeFormatter.ISO_OFFSET_TIME]
 *
 * @see javax.xml.bind.annotation.adapters.XmlAdapter
 *
 * @see java.time.OffsetTime
 */
class OffsetTimeXmlAdapter : TemporalAccessorXmlAdapter<OffsetTime?>(DateTimeFormatter.ISO_OFFSET_TIME,
    TemporalQuery { temporal : TemporalAccessor? -> OffsetTime.from(temporal) })