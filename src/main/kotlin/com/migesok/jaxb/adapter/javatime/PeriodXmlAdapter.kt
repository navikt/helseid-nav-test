package com.migesok.jaxb.adapter.javatime

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.Period

class PeriodXmlAdapter : XmlAdapter<String?, Period?>() {

    override fun unmarshal(stringValue : String?) : Period? {
        return if (stringValue != null) Period.parse(stringValue) else null
    }

    override fun marshal(value : Period?) : String? {
        return value?.toString()
    }
}