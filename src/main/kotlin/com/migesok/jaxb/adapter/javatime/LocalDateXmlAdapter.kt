package com.migesok.jaxb.adapter.javatime

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery

/**
 * `XmlAdapter` mapping JSR-310 `LocalDate` to ISO-8601 string
 *
 *
 * It uses [java.time.format.DateTimeFormatter.ISO_DATE] for parsing and serializing,
 * time-zone information ignored.
 *
 * @see javax.xml.bind.annotation.adapters.XmlAdapter
 *
 * @see java.time.LocalDate
 */
class LocalDateXmlAdapter : TemporalAccessorXmlAdapter<LocalDate?>(DateTimeFormatter.ISO_DATE,
    TemporalQuery { temporal : TemporalAccessor? -> LocalDate.from(temporal) })