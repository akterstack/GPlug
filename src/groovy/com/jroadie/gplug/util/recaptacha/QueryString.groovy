package com.jroadie.gplug.util.recaptacha

class QueryString {
    Map params = [:]
    QueryString(Map params) {
        if (params) {
            this.params.putAll(params)
        }
    }
    void add(String name, Object value) {
        if (value) {
            params.put(name, value)
        }
    }
    String toString() {
        def list = []
        params.each {name, value ->
            list << "$name=" + URLEncoder.encode(value.toString())
        }
        return list.join("&")
    }
}
