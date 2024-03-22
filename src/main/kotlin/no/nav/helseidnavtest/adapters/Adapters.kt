package no.nav.helseidnavtest.adapters

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME


class LocalDateAdapter  {
    companion object {
        @JvmStatic
        @Throws(RuntimeException::class)
        fun unmarshal(v: String?) = v?.let { ISO_LOCAL_DATE.parse(it, LocalDate::from) }

        @JvmStatic
        @Throws(RuntimeException::class)
        fun marshal(v: LocalDate?) = v?.toString()
    }
 }

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