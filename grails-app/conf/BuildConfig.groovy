grails.servlet.version = "3.0"
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/" + appName + "-" + appVersion + "/work"
grails.work.dir = "target/" + appName + "-" + appVersion + "/grails.cache"
grails.project.target.level = 1.7
grails.project.source.level = 1.7
grails.project.dependency.resolver="maven"
grails.project.dependency.resolution = {
    cacheDir "target/" + appName + "-" + appVersion + "/maven.cache"

    inherits("global") {
    }
    log "info"
    checksums false
    legacyResolve false
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        runtime 'mysql:mysql-connector-java:5.1.29'
        compile "org.springframework:spring-orm:$springVersion"
    }

    plugins {
        build ":tomcat:7.0.53"
        compile ":scaffolding:2.1.0"
        compile ":quartz:1.0.2"
        runtime ":hibernate4:4.3.5.3"
        runtime ":mail:1.0.6"
    }
}
/*

File plugins = new File("./target/plugins.properties")
if(plugins.exists()) {
    Properties ppt = new Properties()
    ppt.load(plugins.newInputStream())
    def allLocations = grails.plugin.location
    allLocations.putAll(ppt)
}*/
