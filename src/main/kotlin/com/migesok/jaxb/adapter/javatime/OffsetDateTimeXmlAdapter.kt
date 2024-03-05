package com.migesok.jaxb.adapter.javatime

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery

/**
 * `XmlAdapter` mapping JSR-310 `OffsetDateTime` to ISO-8601 string
 *
 *
 * String format details: [java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME]
 *
 * @see javax.xml.bind.annotation.adapters.XmlAdapter
 *
 * @see java.time.OffsetDateTime
 */
class OffsetDateTimeXmlAdapter : TemporalAccessorXmlAdapter<OffsetDateTime?>(DateTimeFormatter.ISO_OFFSET_DATE_TIME,
    TemporalQuery { temporal : TemporalAccessor? -> OffsetDateTime.from(temporal) })