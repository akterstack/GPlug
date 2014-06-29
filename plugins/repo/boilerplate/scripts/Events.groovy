eventSetClasspath = { ClassLoader rootLoader ->
    GroovyClassLoader loader = new GroovyClassLoader(rootLoader);
    File currentDir = new File("")
    currentDir = new File(currentDir.absolutePath)
    String scriptDir
    File baseDir
    if(currentDir.parentFile.name == "plugins") {
        scriptDir = "../scripts/ClasspathSetter.groovy"
        baseDir = currentDir.parentFile.parentFile.parentFile
    } else {
        scriptDir = "ref/plugins/scripts/ClasspathSetter.groovy"
        baseDir = currentDir
    }
    Class groovyClass = loader.parseClass(new File(scriptDir));
    groovyClass.setPath baseDir, rootLoader, baseName
}