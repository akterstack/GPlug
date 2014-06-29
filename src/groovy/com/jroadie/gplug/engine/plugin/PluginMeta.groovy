package com.jroadie.gplug.engine.plugin

import com.bitmascot.manager.HookManager
import com.bitmascot.util.AppUtil
import grails.util.Holders
import groovy.io.FileType

import java.lang.reflect.Method

class PluginMeta {
    public static class HookPoint {
        String bean
        String callable
        String taglib
        String clazz
        String view
    }

    Map<String, HookPoint> hookPoints = [:];
    String name;
    String description;
    String identifier;
    ClassLoader loader;
    List<String> viewNames = [];
    Date installed;

    Map autoPageJS = [:]
    Map autoPageCSS = [:]
    List siteJS = []

    public updatePluginResourceMeta() {
        updatePluginSiteJS()
        updatePluginAutoPageJS()
        updatePluginAutoPageCSS()
    }

    private updatePluginSiteJS() {
        String jsPath = AppUtil.servletContext.getRealPath("plugins/$identifier/js/site");
        File file = new File(jsPath);
        int cutLength = file.toURI().path.length();
        if(file.exists()) {
            file.traverse([type: FileType.FILES]) { _file ->
                siteJS.add("plugins/$identifier/js/site/" + _file.toURI().path.substring(cutLength))
            }
        }
    }

    private updatePluginAutoPageJS() {
        String jsPath = AppUtil.servletContext.getRealPath("plugins/$identifier/js/auto_page");
        File file = new File(jsPath);
        int cutLength = file.toURI().path.length();
        if(file.exists()) {
            file.traverse([type: FileType.FILES]) { _file ->
                String name = _file.name;
                name = name.substring(0, name.length() - 3)
                autoPageJS.put(name, "plugins/$identifier/js/auto_page/" + _file.toURI().path.substring(cutLength))
            }
        }
    }

    private updatePluginAutoPageCSS() {
        String cssPath = AppUtil.servletContext.getRealPath("plugins/$identifier/css/auto_page");
        File file = new File(cssPath);
        int cutLength = file.toURI().path.length();
        if(file.exists()) {
            file.traverse([type: FileType.FILES]) { _file ->
                String name = _file.name;
                name = name.substring(0, name.length() - 4)
                autoPageCSS.put(name, "plugins/$identifier/css/auto_page/" + _file.toURI().path.substring(cutLength))
            }
        }
    }

    public registerHooks() {
        hookPoints.each { name, hookpoint ->
            if(hookpoint.bean) {
                HookManager.register name, { param ->
                    def bean = Holders.applicationContext.getBean(hookpoint.bean)
                    Method method
                    if(bean) {
                        method = bean.class.getDeclaredMethods().find { lookupmethod ->
                            lookupmethod.name == hookpoint.callable
                        }
                        if(method) {
                            return method.invoke(bean, param.toArray(Object[]))
                        }
                    }
                    return null;
                }
            }
        }
    }
}
