package com.jeskeshouse.robolectric

class XmlUtil {

    static String writeXmlToString(xml) {
        def writer = new StringWriter()
        new XmlNodePrinter(new PrintWriter(writer)).print(xml)

        writer.toString()
    }

}
