import groovy.xml.MarkupBuilder
import org.codehaus.groovy.runtime.DefaultGroovyMethods

includeTargets << grailsScript("_GrailsPackage")

boolean shouldClean = args.indexOf("-clean") > -1;
if(shouldClean) {
    includeTargets << grailsScript("_GrailsClean")
}

target(build: "Generates a packed webcommander plugin") {
    depends(descriptor, buildJar)
    //unknown reason in zip target update: false not working
    ant.delete(file: "./target/$grailsAppName-${grailsAppVersion}.zip")
    ant.mkdir(dir: "./target/public")
    ant.copy(todir: "./target/public") {
        fileset(dir: "./web-app", excludes: "WEB-INF/ META-INF/")
    }
    ant.zip(destfile: "./target/$grailsAppName-${grailsAppVersion}.zip",
        basedir: "./target",
        includes: "plugin.jar, public/**",
        filesonly: "true",
        update: "true")
    if(new File("./web-app/WEB-INF/system-resources/").exists()) {
        ant.zip(destfile: "./target/$grailsAppName-${grailsAppVersion}.zip",
            basedir: "./web-app/WEB-INF",
            includes: "system-resources/**",
            filesonly: "true",
            update: "true")
    }
    if(new File("./lib/").exists()) {
        ant.zip(destfile: "./target/$grailsAppName-${grailsAppVersion}.zip",
            basedir: "./lib",
            includes: "*.jar",
            update: "true")
    }
    ant.zip(destfile: "./target/$grailsAppName-${grailsAppVersion}.zip",
        basedir: "./target",
        includes: "descriptor.xml",
        update: "true")
    ant.zip(destfile: "./target/$grailsAppName-${grailsAppVersion}.zip",
        basedir: "./grails-app",
        includes: "i18n/**",
        filesonly: "true",
        update: "true")
    ant.delete(dir: "./target/public")
    println("$grailsAppName Plugin ($grailsAppName-${grailsAppVersion}.zip) - has been successfully created and stored in target");
}

target(buildJar: "Generates a packed webcommander plugin") {
    if(shouldClean) {
        depends(cleanWork)
    }
    depends(createConfig, compile)
    if(new File("grails-app/views").exists()) {
        depends(compilegsp)
    }
    //unknown reason in zip target (update: false not working)
    ant.delete(file: "./target/plugin.jar")
    println "Cleared previous pack"
    ant.zip(destfile: "./target/plugin.jar",
        basedir: "./target/classes",
        excludes: "gsp/ application.properties BuildConfig* Config* *GrailsPlugin*",
        filesonly: "true")
    println "Created plugin.jar"
}

target(descriptor: "Generates descriptor.xml") {
    ant.delete(file: "target/descriptor.xml")

    def camalCase = { string -> string.split("-").collect { it.capitalize() }.join('') }
    def xCamalCase = { string ->
        def parts = string.split("-");
        if(parts.size() == 1) {
            return string;
        }
        parts.eachWithIndex { v, i ->
            if( i == 0) {
                string = v;
                return;
            }
            string += v.capitalize()
        }
        return string;
    }

    def gcl = new GroovyClassLoader(classLoader)
    def pluginClass = gcl.parseClass(new File(camalCase(grailsAppName) + "GrailsPlugin.groovy"))
    def plugin = pluginClass.newInstance()
    def pluginProps = DefaultGroovyMethods.getProperties(plugin);

    File file = new File("target/descriptor.xml");
    file.parentFile.mkdirs();
    file.createNewFile();
    file.withWriter { writer ->
        def xml = new MarkupBuilder(writer)
        xml.descriptor() {
            id(grailsAppName)
            name(pluginProps.title)
            version(grailsAppVersion)
            desc(pluginProps.description)
            controllers() {
                File controllers = new File("grails-app/controllers");
                if(controllers.exists()) {
                    int cutLength = controllers.absolutePath.length() + 1;
                    controllers.traverse { _file ->
                        if(!_file.directory) {
                            String path = _file.absolutePath;
                            controller(path.substring(cutLength, path.length() - 7).replace(File.separatorChar, '.' as char))
                        }
                    }
                }
            }
            domains() {
                File domains = new File("grails-app/domain");
                if(domains.exists()) {
                    int cutLength = domains.absolutePath.length() + 1;
                    domains.traverse { _file ->
                        if(!_file.directory) {
                            String path = _file.absolutePath;
                            domain(path.substring(cutLength, path.length() - 7).replace(File.separatorChar, '.' as char))
                        }
                    }
                }
            }
            services() {
                File services = new File("grails-app/services");
                if(services.exists()) {
                    int cutLength = services.absolutePath.length() + 1;
                    services.traverse { _file ->
                        if(!_file.directory) {
                            String path = _file.absolutePath;
                            service(path.substring(cutLength, path.length() - 7).replace(File.separatorChar, '.' as char))
                        }
                    }
                }
            }
            filters() {
                File filters = new File("grails-app/conf/com/bitmascot/plugin/" + grailsAppName.replace('-', '_') + "/filters");
                if(filters.exists()) {
                    int cutLength = new File("grails-app/conf").absolutePath.length() + 1;
                    filters.traverse { _file ->
                        if(!_file.directory && _file.name.endsWith("Filters.groovy")) {
                            String path = _file.absolutePath;
                            filter(path.substring(cutLength, path.length() - 7).replace(File.separatorChar, '.' as char))
                        }
                    }
                }
            }
            taglibs() {
                File taglibs = new File("grails-app/taglib");
                if(taglibs.exists()) {
                    int cutLength = taglibs.absolutePath.length() + 1;
                    taglibs.traverse { _file ->
                        if(!_file.directory) {
                            String path = _file.absolutePath;
                            taglib(path.substring(cutLength, path.length() - 7).replace(File.separatorChar, '.' as char))
                        }
                    }
                }
            }
            utils() {
                File utils = new File("grails-app/utils");
                if(utils.exists()) {
                    int cutLength = utils.absolutePath.length() + 1;
                    utils.traverse { _file ->
                        if(!_file.directory) {
                            String path = _file.absolutePath;
                            util(path.substring(cutLength, path.length() - 7).replace(File.separatorChar, '.' as char))
                        }
                    }
                }
            }
            views() {
                File views = new File("grails-app/views");
                if(views.exists()) {
                    int cutLength = views.absolutePath.length();
                    views.traverse { _file ->
                        if(!_file.directory) {
                            view("gsp_" + xCamalCase(grailsAppName) + _file.parentFile.absolutePath.substring(cutLength).replace(File.separatorChar, '_' as char) +
                                    _file.name.replace('.' as char, '_' as char))
                        }
                    }
                }
            }
            hooks() {
                pluginProps.hooks.each { hook -> "$hook.key"(hook.value) }
            }
            if(new File("grails-app/conf/" + camalCase(grailsAppName) + "UrlMappings.groovy").exists()) {
                urls()
            }
        }
    }
    println "Created descriptor.xml"
}

setDefaultTarget(build)
