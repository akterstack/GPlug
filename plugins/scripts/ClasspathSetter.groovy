import groovy.io.FileType

static setPath(baseDir, rootLoader, baseName) {
    File targetDirLocation = new File(baseDir, "target")
    def baseClasspath = new File(targetDirLocation, "classes").toURI().toURL()
    def baseLibpath = new File(baseDir, "lib")
    rootLoader.getParent().addURL(baseClasspath)
    baseLibpath.traverse([type: FileType.FILES]) { _file ->
        if(_file.absolutePath.endsWith(".jar")) {
            rootLoader.getParent().addURL(_file.toURI().toURL())
        }
    }
    File appProperties = new File(baseDir, "application.properties")
    Properties app = new Properties();
    appProperties.withInputStream { stream ->
        app.load stream
    }
    File mavenCacheDir = new File(new File(targetDirLocation, app["app.name"] + "-" + app["app.version"]), "maven.cache")
    if(mavenCacheDir.exists()) {
        mavenCacheDir.traverse([type: FileType.FILES]) { _file ->
            if(_file.absolutePath.endsWith(".jar")) {
                rootLoader.getParent().addURL(_file.toURI().toURL())
            }
        }
    }
}