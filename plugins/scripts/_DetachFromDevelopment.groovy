includeTargets << grailsScript("_GrailsInit")

target(detachFromDevelopment: "Detach $grailsAppName plugin from base application") {
    new File("../../../web-app/plugins/$grailsAppName").deleteDir()
    File propFile = new File("../../../target/plugins.properties");
    if(propFile.exists()) {
        Properties attacheds = new Properties();
        propFile.withInputStream { stream ->
            attacheds.load(stream)
        }
        attacheds.remove(grailsAppName)
        propFile.withOutputStream { stream ->
            attacheds.store(stream, null)
        }
    }
    println "Plugin $grailsAppName detached from base"
}

setDefaultTarget(detachFromDevelopment)
