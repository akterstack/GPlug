package com.jroadie.gplug.util

import com.bitmascot.spring.CommanderControllerHelper
import grails.util.Holders
import grails.web.Action
import org.codehaus.groovy.grails.commons.GrailsControllerClass

import java.lang.reflect.Method
import java.lang.reflect.Modifier

class ControllerMixin {
    static void mixinActions(Class baseControllerClass, Class mixinClass) {
        GrailsControllerClass _class = Holders.grailsApplication.controllerClasses.find {
            it.clazz == baseControllerClass
        }
        if(!_class) {
            throw new ClassNotFoundException()
        }

        Class superClass = mixinClass;
        def controller = Holders.grailsApplication.mainContext.getBean(_class.clazz);
        controller.metaClass.mixin mixinClass
        while(superClass != null && superClass != Object.class && superClass != GroovyObject.class) {
            for(Method method : superClass.getMethods()) {
                if(Modifier.isPublic(method.getModifiers()) && method.getAnnotation(Action.class) != null) {
                    String methodName = method.getName();
                    if(!methodName.endsWith("Flow")) {
                        _class.registerMapping(methodName)
                        CommanderControllerHelper.updateMetaPropertyCache(_class.clazz, methodName, {
                            controller."${methodName}"()
                        })
                    }
                }
            }
            superClass = superClass.getSuperclass();
        }
    }
}
