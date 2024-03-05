package com.migesok.jaxb.adapter.javatime

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery

/**
 * `XmlAdapter` mapping JSR-310 `ZonedDateTime` to ISO-8601 string
 *
 *
 * String format details: [java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME]
 *
 * @see javax.xml.bind.annotation.adapters.XmlAdapter
 *
 * @see java.time.ZonedDateTime
 */
class ZonedDateTimeXmlAdapter : TemporalAccessorXmlAdapter<ZonedDateTime?>(DateTimeFormatter.ISO_ZONED_DATE_TIME,
    TemporalQuery { temporal : TemporalAccessor? -> ZonedDateTime.from(temporal) })