package com.jroadie.gplug.engine.plugin

import org.codehaus.groovy.grails.cli.support.GrailsRootLoader

class PluginAwareRootLoader extends GrailsRootLoader {
    public PluginAwareRootLoader(ClassLoader parent) {
        super(new URL[0], parent)
    }

    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve)
        } catch(ClassNotFoundException no) {
            if(name.startsWith("com.jroadie.gplug.plugin")) {
                String pluginId = name.substring(21);
                int dotIndex = pluginId.indexOf(".");
                if(dotIndex == -1) {
                    throw no;
                }
                pluginId = pluginId.substring(0, dotIndex).replace('_' as char, '-' as char);
                ClassLoader loader = PluginManager.loadedPlugins.findResult { _plugin ->
                    if(_plugin.identifier == pluginId) {
                        return _plugin.loader;
                    }
                }
                if(!loader) {
                    throw no;
                }
                Class c = loader.findLoadedClass(name);
                if(!c) {
                    throw no;
                }
                if(resolve) {
                    loader.resolveClass(c)
                }
                return c;
            }
            throw no;
        }
    }
}
