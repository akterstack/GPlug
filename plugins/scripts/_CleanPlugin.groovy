includeTargets << grailsScript("_GrailsClean")
includeTargets << new File("../scripts/_UndeployPlugin.groovy")
includeTargets << new File("../scripts/_DetachFromDevelopment.groovy")

target(cleanPlugin: "Detach and Undeploy and then clear project") {
    depends(cleanWork, detachFromDevelopment, undeployPlugin)
    new File("target/descriptor.xml").delete()
    new File("target/plugin.jar").delete()
    new File("target/$grailsAppName-${grailsAppVersion}.zip").delete()
    println "Project Cleaned"
}

setDefaultTarget(cleanPlugin)
