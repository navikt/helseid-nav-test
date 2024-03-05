package com.migesok.jaxb.adapter.javatime

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery
import java.util.Objects

/**
 * `XmlAdapter` mapping any JSR-310 `TemporalAccessor` to string using provided `DateTimeFormatter`
 *
 *
 * Example:
 * <pre>
 * `public class DottedDateXmlAdapter extends TemporalAccessorXmlAdapter<LocalDate> {
 * public DottedDateXmlAdapter() {
 * super(DateTimeFormatter.ofPattern("dd.MM.yyyy"), LocalDate::from);
 * }
 * }
` *
</pre> *
 *
 * @param <T> mapped temporal type
 * @see javax.xml.bind.annotation.adapters.XmlAdapter
 *
 * @see java.time.temporal.TemporalAccessor
 *
 * @see java.time.format.DateTimeFormatter
</T> */
open class TemporalAccessorXmlAdapter<T : TemporalAccessor?>(formatter : DateTimeFormatter, temporalQuery : TemporalQuery<out T>?) : XmlAdapter<String?, T?>() {

    private val formatter : DateTimeFormatter = Objects.requireNonNull(formatter, "formatter must not be null")
    private val temporalQuery = Objects.requireNonNull(temporalQuery, "temporal query must not be null")!!

    override fun unmarshal(stringValue : String?) : T? {
        return if (stringValue != null) formatter.parse(stringValue, temporalQuery) else null
    }

    override fun marshal(value : T?) : String? {
        return if (value != null) formatter.format(value) else null
    }
}