includeTargets << grailsScript("_GrailsInit")
includeTargets << new File("../scripts/_DetachFromDevelopment.groovy")
includeTargets << new File("../scripts/_UndeployPlugin.groovy")
includeTargets << new File("../scripts/_BuildPlugin.groovy")

target(deployPlugin: "Deploy $grailsAppName plugin in base application") {
    depends(undeployPlugin, detachFromDevelopment, descriptor, buildJar)
    ant.mkdir(dir: "../../../web-app/WEB-INF/wc-plugins/$grailsAppName")
    ant.copy(file: "target/plugin.jar", todir: "../../../web-app/WEB-INF/wc-plugins/$grailsAppName", overwrite: true, force: true)
    ant.copy(file: "target/descriptor.xml", todir: "../../../web-app/WEB-INF/wc-plugins/$grailsAppName", overwrite: true, force: true)
    ant.mkdir(dir: "../../../web-app/plugins/$grailsAppName")
    ant.copy(todir: "../../../web-app/plugins/$grailsAppName", overwrite: true, force: true) {
        fileset(dir: "./web-app", excludes: "WEB-INF/ META-INF/")
    }
    ant.copy(todir: "../../../web-app/WEB-INF/wc-plugins/$grailsAppName/i18n", overwrite: true, force: true) {
        fileset(dir: "grails-app/i18n")
    }
    if(new File("./web-app/WEB-INF/system-resources/").exists()) {
        ant.copy(todir: "../../../web-app/WEB-INF/system-resources", overwrite: true, force: true) {
            fileset(dir: "./web-app/WEB-INF/system-resources")
        }
    }
    if(new File("./lib").exists()) {
        ant.copy(todir: "../../../web-app/plugins/$grailsAppName", overwrite: true, force: true) {
            fileset(dir: "./lib", includes: "*.jar")
        }
    }
    println "Plugin $grailsAppName deployed in base. Please restart base to make it effective"
}

setDefaultTarget(deployPlugin)
