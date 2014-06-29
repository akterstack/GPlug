package com.jroadie.gplug.util

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClassProperty
import org.codehaus.groovy.grails.commons.GrailsClass

/**
 * Created by zobair on 31/03/2014.*/
class DomainUtil {
    public static Object clone(domainInstanceToClone, List exclude = [], List reference = []) {
        GrailsClass domainClass = domainInstanceToClone.domainClass
        def newDomainInstance = domainClass.newInstance()
        List notCloneable = domainClass.getPropertyValue("clone_exclude")
        if(notCloneable) {
            exclude.addAll(notCloneable)
        }
        exclude.addAll(["class", "metaClass", "properties", "id"])
        List copyReferences = domainClass.getPropertyValue("copy_reference")
        if(copyReferences) {
            reference.addAll(copyReferences)
        }
        for(DefaultGrailsDomainClassProperty prop in domainClass.getPersistentProperties()) {
            if (prop.name in exclude)
                continue

            def oldObj = domainInstanceToClone."${prop.name}"
            if (prop.association) {
                boolean referenceCopy = reference.contains(prop.name);
                if (prop.owningSide) {
                    if (prop.oneToOne) {
                        newDomainInstance."${prop.name}" = referenceCopy ? oldObj : clone(oldObj)
                    } else {
                        oldObj.each { associationInstance ->
                            def newAssociationInstance = referenceCopy ? associationInstance : clone(associationInstance)
                            if (newAssociationInstance) {
                                newDomainInstance."addTo${prop.name.capitalize()}"(newAssociationInstance)
                            }
                        }
                    }
                } else {
                    if (prop.manyToOne || prop.oneToOne) {
                        newDomainInstance."${prop.name}" = referenceCopy ? oldObj : clone(oldObj)
                    } else if (prop.oneToMany) {
                        oldObj.each { associationInstance ->
                            def newAssociationInstance = referenceCopy ? associationInstance : clone(associationInstance)
                            newDomainInstance."addTo${prop.name.capitalize()}"(newAssociationInstance)
                        }
                    } else {
                        String fieldName = domainClass.getPropertyValue("non_owning_reference")[prop.name]
                        oldObj.each {
                            it[fieldName].add(newDomainInstance)
                        }
                    }
                }
            } else {
                if(oldObj instanceof Collection) {
                    def list = newDomainInstance."${prop.name}" = oldObj instanceof List ? [] : [] as Set
                    oldObj.each {
                        list.add(it)
                    }
                } else {
                    newDomainInstance."${prop.name}" = oldObj
                }
            }
        }

        return newDomainInstance
    }
}