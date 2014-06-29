includeTargets << grailsScript("_GrailsInit")
includeTargets << new File("../scripts/_UndeployPlugin.groovy")
includeTargets << new File("../scripts/_DetachFromDevelopment.groovy")

target(attachInDevelopment: "Attaches $grailsAppName plugin to development environment") {
    depends(undeployPlugin, detachFromDevelopment)

    File propFile = new File("../../../target/plugins.properties");
    if(!propFile.exists()) {
        propFile.parentFile.mkdirs()
        propFile.createNewFile()
    }

    Properties attacheds = new Properties();
    propFile.withInputStream { stream ->
        attacheds.load(stream)
    }
    attacheds.put(grailsAppName, "/ref/plugins/$grailsAppName".toString())
    propFile.withOutputStream { stream ->
        attacheds.store(stream, null)
    }

    println "Project attached with base. Restart base to make it effective"
}

setDefaultTarget(attachInDevelopment)