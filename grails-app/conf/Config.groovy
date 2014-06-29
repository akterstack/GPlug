import com.jroadie.gplug.util.PropertyReader
import org.apache.log4j.ConsoleAppender
import org.apache.log4j.PatternLayout

grails.controllers.defaultScope = "singleton"
grails.services.defaultScope = "singleton"
grails.gorm.failOnError = true
grails.project.groupId = "com.jroadie.gplug" // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = true
grails.mime.types = [
    all:           '*/*',
    atom:          'application/atom+xml',
    css:           'text/css',
    csv:           'text/csv',
    form:          'application/x-www-form-urlencoded',
    html:          ['text/html','application/xhtml+xml'],
    js:            'text/javascript',
    json:          ['application/json', 'text/json'],
    multipartForm: 'multipart/form-data',
    rss:           'application/rss+xml',
    text:          'text/plain',
    xml:           ['text/xml', 'application/xml']
]
grails.gorm.default.mapping = {
    autoTimestamp false
    version false
    cache true
}

grails.http.invalid.method.allow.header = false
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
grails.scaffolding.templates.domainSuffix = 'Instance'
grails.json.legacy.builder = false
grails.enable.native2ascii = true
grails.spring.bean.packages = []
grails.web.disable.multipart=false
grails.exceptionresolver.params.exclude = ['password']
grails.hibernate.cache.queries = true

environments {
    development {
        grails.logging.jul.usebridge = true
    }
    production {
        grails.logging.jul.usebridge = false
    }
}

log4j.main = {
    def logLayoutPattern = new PatternLayout("%d [%t] %-5p %c %x - %m%n")
    root {
        debug: appLogConsole: [
            'org.codehaus.groovy.grails',
            'grails.spring',
            'org.springframework',
            'org.hibernate',
            'filters',
            'errors.GrailsExceptionResolver'
        ]
        all: nullAppender: [
            "org.apache.catalina"
        ]
        environments {
            production {
                error: appLogConsole: [
                    'org.codehaus.groovy.grails',
                    'grails.spring',
                    'org.springframework',
                    'org.hibernate',
                    'filters',
                    'errors.GrailsExceptionResolver',
                    'com.bitmascot'
                ]
            }
            development {
                all: appLogConsole: 'com.jroadie.gplug'
            }
        }
    }
    appenders {
        appender new ConsoleAppender(name: "appLogConsole", layout: logLayoutPattern)
        'null' name: "nullAppender"
    }
}

adminpackages = [
    "com.jroadie.gplug.admin",
    "com.jroadie.gplug.plugin.*.admin"
]

grails.server.port.http = PropertyReader.getProperty("grails.server.port.http", "80")
grails.server.port.https = PropertyReader.getProperty("grails.server.port.https", "443")
grails.server.hostname = PropertyReader.getProperty("grails.server.hostname", "localhost")