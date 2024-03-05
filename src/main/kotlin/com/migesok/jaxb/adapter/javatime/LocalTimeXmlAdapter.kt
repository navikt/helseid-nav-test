package com.migesok.jaxb.adapter.javatime

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery

/**
 * `XmlAdapter` mapping JSR-310 `LocalTime` to ISO-8601 string
 *
 *
 * String format details: [java.time.format.DateTimeFormatter.ISO_LOCAL_TIME]
 *
 * @see javax.xml.bind.annotation.adapters.XmlAdapter
 *
 * @see java.time.LocalTime
 */
class LocalTimeXmlAdapter : TemporalAccessorXmlAdapter<LocalTime?>(DateTimeFormatter.ISO_LOCAL_TIME,
    TemporalQuery { temporal : TemporalAccessor? -> LocalTime.from(temporal) })