package com.migesok.jaxb.adapter.javatime

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery

/**
 * `XmlAdapter` mapping JSR-310 `LocalDateTime` to ISO-8601 string
 *
 *
 * String format details: [java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME]
 *
 * @see javax.xml.bind.annotation.adapters.XmlAdapter
 *
 * @see java.time.LocalDateTime
 */
class LocalDateTimeXmlAdapter : TemporalAccessorXmlAdapter<LocalDateTime?>(DateTimeFormatter.ISO_LOCAL_DATE_TIME,
    TemporalQuery { temporal : TemporalAccessor? -> LocalDateTime.from(temporal) })