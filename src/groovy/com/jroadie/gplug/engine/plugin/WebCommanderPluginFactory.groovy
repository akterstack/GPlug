package com.jroadie.gplug.engine.plugin

import com.jroadie.gplug.util.AppUtil
import org.codehaus.groovy.grails.commons.GrailsApplicationFactoryBean
import org.springframework.core.io.Resource
import org.springframework.web.context.support.ServletContextResource

class WebCommanderPluginFactory extends GrailsApplicationFactoryBean {

    @Override
    void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        PluginManager.registrarPlugins();
        PluginManager.loadedPlugins.each { plugin ->
            try {
                Class.forName("com.jroadie.gplug.plugin." + plugin.identifier.replace('-' as char, '_' as char) + ".PluginBootStrap", false, plugin.loader).onLoad();
            } catch(ClassNotFoundException e) {
            } catch(MissingMethodException e) {
            }
        }
    }

    @Override
    void setGrailsDescriptor(Resource r) {
        ServletContextResource resource = r;
        AppUtil.servletContext = resource.getServletContext();
        super.setGrailsDescriptor(r);
    }
}
