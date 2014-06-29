package com.jroadie.gplug.plugin.boilerplate

import com.jroadie.gplug.engine.plugin.PluginMeta

import javax.servlet.ServletContext

class PluginBootStrap {

    static initialize(PluginMeta plugin, ServletContext servletContext) {
        println("Awesome Initialized!")
    }
}
