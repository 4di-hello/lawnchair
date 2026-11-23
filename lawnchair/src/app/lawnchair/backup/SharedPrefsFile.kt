package app.lawnchair.backup

import android.util.Xml
import java.io.File
import org.xmlpull.v1.XmlPullParser

/**
 * Fork: reads and writes a SharedPreferences XML file directly, without going through the
 * cached SharedPreferences instance. A restore replaces the file on disk while the launcher
 * still holds the old values in memory; writing through that instance would put the old values
 * back into the file.
 */
internal object SharedPrefsFile {

    fun read(file: File): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        if (!file.exists()) return map
        file.inputStream().use { input ->
            val parser = Xml.newPullParser()
            parser.setInput(input, null)
            var setName: String? = null
            var set: MutableSet<String>? = null
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    val name = parser.getAttributeValue(null, "name")
                    val value = parser.getAttributeValue(null, "value")
                    when (parser.name) {
                        "string" -> {
                            val text = parser.nextText()
                            if (set != null) {
                                set.add(text)
                            } else if (name != null) {
                                map[name] = text
                            }
                        }

                        "int" -> if (name != null && value != null) map[name] = value.toInt()

                        "long" -> if (name != null && value != null) map[name] = value.toLong()

                        "float" -> if (name != null && value != null) map[name] = value.toFloat()

                        "boolean" -> if (name != null && value != null) map[name] = value.toBoolean()

                        "set" -> if (name != null) {
                            setName = name
                            set = mutableSetOf()
                        }
                    }
                } else if (event == XmlPullParser.END_TAG && parser.name == "set") {
                    val finished = set
                    if (setName != null && finished != null) map[setName] = finished
                    setName = null
                    set = null
                }
                event = parser.next()
            }
        }
        return map
    }

    fun write(file: File, map: Map<String, Any>) {
        file.parentFile?.mkdirs()
        val tmp = File(file.parentFile, "${file.name}.tmp")
        tmp.outputStream().use { out ->
            val serializer = Xml.newSerializer()
            serializer.setOutput(out, "utf-8")
            serializer.startDocument("utf-8", true)
            serializer.startTag(null, "map")
            map.forEach { (key, value) ->
                when (value) {
                    is String -> serializer.startTag(null, "string").attribute(null, "name", key)
                        .text(value).endTag(null, "string")

                    is Set<*> -> {
                        serializer.startTag(null, "set").attribute(null, "name", key)
                        value.filterIsInstance<String>().forEach {
                            serializer.startTag(null, "string").text(it).endTag(null, "string")
                        }
                        serializer.endTag(null, "set")
                    }

                    else -> {
                        val tag = when (value) {
                            is Int -> "int"
                            is Long -> "long"
                            is Float -> "float"
                            is Boolean -> "boolean"
                            else -> return@forEach
                        }
                        serializer.startTag(null, tag).attribute(null, "name", key)
                            .attribute(null, "value", value.toString()).endTag(null, tag)
                    }
                }
            }
            serializer.endTag(null, "map")
            serializer.endDocument()
            out.fd.sync()
        }
        if (!tmp.renameTo(file)) {
            tmp.delete()
            error("Could not replace ${file.name}")
        }
    }
}
