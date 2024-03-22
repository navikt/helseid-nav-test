package no.nav.helseidnavtest.adapters

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateAdapter  {
    companion object {
        @JvmStatic
        @Throws(RuntimeException::class)
        fun unmarshal(v: String?) = v?.let { DateTimeFormatter.ISO_LOCAL_DATE.parse(it, LocalDate::from) }

        @JvmStatic
        @Throws(RuntimeException::class)
        fun marshal(v: LocalDate?) = v?.toString()
    }
 }