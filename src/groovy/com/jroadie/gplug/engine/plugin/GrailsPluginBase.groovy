package com.jroadie.gplug.engine.plugin

import groovy.io.FileType
import groovy.io.FileVisitResult
import org.apache.commons.io.FileUtils

class GrailsPluginBase {
    def title
    def author
    def authorEmail
    def description
    def grailsVersion
    def documentation
    def watchedResources = ["file:./web-app/*.js", "file:./web-app/js/*.properties", "file:./web-app/*.css", "file:./web-app/images/**"]
    def applicationResourceBase;
    String pluginResourceBase;
    PluginMeta _plugin

    def doWithWebDescriptor = { xml ->
        PluginManager.loadedPlugins.add(_plugin);
    }

    def doWithSpring = {}

    def doWithDynamicMethods = { ctx ->
    }

    def hooks = []

    def pluginExcludes = []

    def doWithApplicationContext = { ctx ->
        applicationResourceBase = ctx.servletContext.getRealPath("/")
        pluginResourceBase = new File(applicationResourceBase).parentFile.absolutePath + "/plugins/repo/$_plugin.identifier/web-app".replace("/", File.separator)
        new File(applicationResourceBase + "plugins/$_plugin.identifier").deleteDir()
        new File(pluginResourceBase).traverse([type: FileType.FILES, preDir: {
            if(it.name == "WEB-INF" || it.name == "META-INF") {
                return FileVisitResult.SKIP_SUBTREE
            }
        }]) { _file ->
            updateResource(_file);
        }
        File resourceFolder = new File(pluginResourceBase + "/WEB-INF/system-resources");
        File destinationFolder = new File(applicationResourceBase + "WEB-INF/system-resources")
        if(resourceFolder.exists()) {
            if(!destinationFolder.exists()) {
                destinationFolder.mkdir()
            }
            FileUtils.copyDirectory(resourceFolder, destinationFolder)
        }
        _plugin.updatePluginResourceMeta()
        hooks.each { hook ->
            _plugin.hookPoints.put(hook.key, new PluginMeta.HookPoint(hook.value))
        }
        _plugin.registerHooks()
    }

    def onChange = { event ->
        File changeFile = event.source.file
        if(changeFile.absolutePath.startsWith(pluginResourceBase)) {
            updateResource(changeFile);
            println event.source.file.absolutePath + " updated in base application"
        }
    }

    def onConfigChange = { event ->
    }

    def onShutdown = { event ->
    }

    def updateResource(File file) {
        new File(applicationResourceBase + "plugins/$_plugin.identifier").mkdirs();
        String suffix = file.absolutePath.substring(pluginResourceBase.length())
        File out = new File(applicationResourceBase + "plugins/$_plugin.identifier" + suffix);
        out.parentFile.mkdirs();
        file.withInputStream { _in ->
            out.withOutputStream { _out ->
                _out << _in
            }
        }
    }
}
