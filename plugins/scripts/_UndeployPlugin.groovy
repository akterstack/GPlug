includeTargets << grailsScript("_GrailsInit")

target(undeployPlugin: "Undeploy $grailsAppName plugin from base application") {
    ant.delete(dir: "../../../web-app/WEB-INF/wc-plugins/$grailsAppName", quiet: "true")
    ant.delete(dir: "../../../web-app/plugins/$grailsAppName", quiet: "true")
    println "Project $grailsAppName undeployed"
}

setDefaultTarget(undeployPlugin)
