package com.jroadie.gplug.util.recaptacha

class Post {
    String url
    QueryString queryString = new QueryString()
    URLConnection connection
    String text

    String getText() {
        def thisUrl = new URL(url)
        connection = thisUrl.openConnection()
        if (connection.metaClass.respondsTo(connection, "setReadTimeout", int)) {
            connection.readTimeout = 10000
        }
        if (connection.metaClass.respondsTo(connection, "setConnectTimeout", int)) {
            connection.connectTimeout = 10000
        }
        connection.setRequestMethod("POST")
        connection.doOutput = true
        Writer writer = new OutputStreamWriter(connection.outputStream)
        writer.write(queryString.toString())
        writer.flush()
        writer.close()
        connection.connect()
        return connection.content.text
    }

    String toString() {
        return "POST:\n" +
                url + "\n" +
                queryString.toString()
    }
}