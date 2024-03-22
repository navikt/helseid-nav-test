package no.nav.helseidnavtest.adapters

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME


class LocalDateTimeAdapter {

    companion object {
        @JvmStatic
        @Throws(RuntimeException::class)
        fun unmarshal(v: String?) =  v?.let {ISO_LOCAL_DATE_TIME.parse(it, LocalDateTime::from) }

        @JvmStatic
        @Throws(RuntimeException::class)
        fun marshal(v: LocalDateTime?) = v?.toString()
    }
}