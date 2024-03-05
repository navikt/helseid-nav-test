package com.migesok.jaxb.adapter.javatime

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery

/**
 * `XmlAdapter` mapping JSR-310 `Instant` to ISO-8601 string
 *
 *
 * String format details: [java.time.format.DateTimeFormatter.ISO_INSTANT]
 *
 * @see javax.xml.bind.annotation.adapters.XmlAdapter
 *
 * @see java.time.Instant
 */
class InstantXmlAdapter : TemporalAccessorXmlAdapter<Instant?>(DateTimeFormatter.ISO_INSTANT,
    TemporalQuery { temporal : TemporalAccessor? -> Instant.from(temporal) })