package com.jroadie.gplug.engine.plugin

import com.jroadie.gplug.util.AppUtil
import com.jroadie.gplug.util.StringUtil
import grails.util.Environment
import grails.util.Holders
import groovy.io.FileType
import groovy.util.logging.Log
import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.commons.spring.TypeSpecifyableTransactionProxyFactoryBean
import org.codehaus.groovy.grails.context.support.PluginAwareResourceBundleMessageSource
import org.codehaus.groovy.grails.orm.hibernate.ConfigurableLocalSessionFactoryBean
import org.codehaus.groovy.grails.orm.support.GroovyAwareNamedTransactionAttributeSource
import org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib
import org.codehaus.groovy.grails.validation.GrailsDomainClassValidator
import org.codehaus.groovy.grails.web.pages.TagLibraryLookup
import org.hibernate.SessionFactory
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.ConstructorArgumentValues
import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.springframework.beans.factory.config.RuntimeBeanReference
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.context.ApplicationContext
import org.springframework.transaction.support.TransactionSynchronizationManager

import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@Log
class PluginManager {

    private static boolean isInitialized = false;

    static List<PluginMeta> loadedPlugins = [];

    private static PluginAwareResourceBundleMessageSource _messageSource;

    private static PluginAwareResourceBundleMessageSource getMessageSource() {
        if(_messageSource) {
            return _messageSource;
        } else {
            return _messageSource = Holders.grailsApplication.mainContext.getBean("messageSource")
        }
    }

    private static _gspTagLibraryLookup;

    private static TagLibraryLookup getGspTagLibraryLookup() {
        if(_gspTagLibraryLookup) {
            return _gspTagLibraryLookup;
        } else {
            return _gspTagLibraryLookup = Holders.grailsApplication.mainContext.getBean("gspTagLibraryLookup")
        }
    }

    private static _g;

    private static ApplicationTagLib getG() {
        if(_g) {
            return _g;
        } else {
            return _g = Holders.grailsApplication.mainContext.getBean("org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib")
        }
    }

    private static registerPlugin(File pluginDir) {
        PluginMeta plugin = parseDescriptor(pluginDir);
        plugin.updatePluginResourceMeta()
        plugin.registerHooks()
        loadedPlugins.add(plugin);
    }

    public static boolean registrarPlugins() {
        File file = new File(AppUtil.servletContext.getRealPath("WEB-INF/wc-plugins"));
        if(file.exists()) {
            file.listFiles().each { dir ->
                registerPlugin(dir)
            }
        }
        isInitialized = true;
    }

    public static updateMessageSource() {
        String basePath = AppUtil.servletContext.getRealPath("/")
        def messageBaseNames = messageSource.pluginBaseNames;
        loadedPlugins.each { _plugin ->
            String messageBase = "WEB-INF/wc-plugins/" + _plugin.identifier + "/i18n/messages";
            File file = new File(basePath + messageBase + ".properties")
            if(file.exists()) {
                if(Environment.isWarDeployed()) {
                    messageBaseNames.add(messageBase);
                } else {
                    messageBaseNames.add(basePath + messageBase);
                }
            }
        }
    }

    private static PluginMeta parseDescriptor(File file) {
        //TODO: Check Dependencies
        ClassLoader loader = new GroovyClassLoader(Holders.grailsApplication.classLoader);
        file.traverse([type: FileType.FILES]) { _file ->
            if(_file.absolutePath.endsWith(".jar")) {
                loader.addURL(_file.toURI().toURL())
            }
        }
        PluginMeta plugin = new PluginMeta(hookPoints: [:], loader: loader);

        XmlParser xml = new XmlParser();
        Node node = xml.parse(new File(file, "descriptor.xml"));
        plugin.identifier = node.id.text()
        plugin.description = node.desc.text()
        plugin.name = node.name.text()
        def addLoadableClass = { loadableNode ->
            String beanName = loadableNode instanceof String ? loadableNode : loadableNode.text();
            Class clazz = Class.forName(beanName, false, loader);
            Holders.grailsApplication.addToLoaded(clazz);
            return clazz
        }

        node.controllers.each { controllers ->
            controllers.controller.each addLoadableClass
        }
        node.domains.each { domains ->
            domains.domain.each addLoadableClass
        }
        node.services.each { services ->
            services.service.each addLoadableClass
        }
        node.taglibs.each { taglibs ->
            taglibs.taglib.each addLoadableClass
        }
        node.utils.each { utils ->
            utils.util.each addLoadableClass
        }
        node.filters.each { filters ->
            filters.filter.each addLoadableClass
        }
        node.views.each { views ->
            views.view.each { view -> plugin.viewNames.add(view.text()); }
        }
        node.hooks.each { hooks ->
            hooks.children().each { hook ->
                plugin.hookPoints.put(hook.name(), new PluginMeta.HookPoint(hook.attributes()))
            }
        }
        node.urls.each {
            addLoadableClass(plugin.identifier.split("-").collect { it.capitalize() }.join('') + "UrlMappings")
        }

        BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        FileTime creationTime = attributes.creationTime();
        plugin.installed = new Date(creationTime.toMillis() - TimeZone.default.rawOffset);  //Getting GMT Time

        //Below lines will be needed for hot load
        /*
        DefaultGrailsApplication grailsApplication = Holders.grailsApplication;
        } catch (Throwable throwable) {
                log.log Level.SEVERE, "Could not load domain : Plugin# " + beanName, throwable
            }
        node.bootstrap.each { bootstrap ->
            String className = bootstrap.clazz.text();
            try {
                Class bootstrapClass = Class.forName(className, false, rootLoader);
                plugin.bootstarpClass = bootstrapClass;
                if(isInitialized) {
                    plugin.bootstarpClass.init(plugin);
                }
            } catch (Exception t) {
                log.log Level.SEVERE, "Could not load bootstrap : Plugin# " + className, t
            }
        }
        node.hookpoint.each { hookpoint ->
            PluginMeta.HookPoint hp = new PluginMeta.HookPoint();
            hp.point = hookpoint.point.text();
            hp.clazz = hookpoint.clazz.text();
            hp.hitpoint = hookpoint.hitpoint.text();
            hp.plugin = plugin;
            plugin.hookPoints.add(hp);
            HOOKPOINTS[hp.point].add(hp);
        }
        node.table.each addLoadableClass
        if(isInitialized) {
            initializeDomainClasses(classes);
            classes.clear()
        }
        node.service.each addLoadableClass
        if(isInitialized) {
            initializeServiceClasses(classes);
            classes.clear()
        }
        node.taglib.each addLoadableClass
        if(isInitialized) {
            initializeTaglibClasses(classes);
            classes.clear()
        }
        node.controller.each addLoadableClass
        if(isInitialized) {
            initializeControllerClasses(classes);
            classes.clear()
        }*/
        return plugin;
    }

    private static initializeDomainClasses(ArrayList classes) {
        ApplicationContext context = Holders.grailsApplication.mainContext;
        SessionFactory sessionFactory = context.sessionFactory;
        def factoryImpl = sessionFactory.getWrappedObject();
        DefaultGrailsApplication grailsApplication = Holders.grailsApplication;
        List domainClasses = classes.collect {
            GrailsDomainClass _class = grailsApplication.addArtefact(DomainClassArtefactHandler.TYPE, it);
            context.registerBeanDefinition _class.fullName, new GenericBeanDefinition(beanClass: _class.clazz, beanClassName: _class.fullName,
                    scope: BeanDefinition.SCOPE_PROTOTYPE, autowireMode: AbstractBeanDefinition.AUTOWIRE_BY_NAME);
            GenericBeanDefinition beanDef = new GenericBeanDefinition(beanClass: GrailsDomainClassValidator, lazyInit: true, propertyValues: [messageSource: new RuntimeBeanReference('messageSource'),
                    domainClass: new RuntimeBeanReference("${_class.fullName}DomainClass"), grailsApplication: grailsApplication]);
            context.registerBeanDefinition "${_class.fullName}Validator", beanDef
            beanDef = new GenericBeanDefinition(beanClass: MethodInvokingFactoryBean, lazyInit: true, propertyValues: [targetObject: grailsApplication, targetMethod: 'getArtefact', arguments:
                    [DomainClassArtefactHandler.TYPE, _class.fullName]])
            context.registerBeanDefinition "${_class.fullName}DomainClass", beanDef
            beanDef = new GenericBeanDefinition(beanClass: MethodInvokingFactoryBean, lazyInit: true, propertyValues: [targetObject: new RuntimeBeanReference("${_class.fullName}DomainClass"),
                    targetMethod: 'getClazz'])
            context.registerBeanDefinition "${_class.fullName}PersistentClass", beanDef
            return _class;
        };
        DefaultGrailsApplication dummyApplication = new DefaultGrailsApplication(classes as Class[], Holders.grailsApplication.classLoader) {
            public List getDomainClasses() {
                return domainClasses;
            }
        }
        DomainClassGrailsPlugin.enhanceDomainClasses(dummyApplication, context);
        domainClasses.each {
            //TODO: it is of package org.codehaus.groovy.grails.plugins.orm.hibernate.HibernatePluginSupport
            //HibernatePluginSupport.enhanceSessionFactory(sessionFactory, grailsApplication, context, '', [name: it.name]);
        }
        def newSessionFactoryFactory = context.getBean(ConfigurableLocalSessionFactoryBean.class);
        newSessionFactoryFactory.afterPropertiesSet()
        def holder = TransactionSynchronizationManager.getResource(factoryImpl);
        TransactionSynchronizationManager.unbindResource(factoryImpl);
        TransactionSynchronizationManager.bindResource(context.sessionFactory, holder);
    }

    private static initializeControllerClasses(ArrayList classes) {
        classes.each {
            GrailsControllerClass _class = Holders.grailsApplication.addArtefact(ControllerArtefactHandler.TYPE, it);
            Holders.grailsApplication.mainContext.registerBeanDefinition _class.fullName, new GenericBeanDefinition(beanClass: _class.clazz, beanClassName: _class.fullName,
                    scope: BeanDefinition.SCOPE_PROTOTYPE, autowireMode: AbstractBeanDefinition.AUTOWIRE_BY_NAME);
            _class.initialize();
        }
    }

    private static initializeServiceClasses(ArrayList classes) {
        classes.each {
            GrailsClass _class = Holders.grailsApplication.addArtefact(ServiceArtefactHandler.TYPE, it);
            def context = Holders.grailsApplication.mainContext;
            context.registerBeanDefinition _class.fullName + "ServiceClass", new GenericBeanDefinition(beanClass: MethodInvokingFactoryBean, lazyInit: true, propertyValues:
                    [targetObject: Holders.grailsApplication, targetMethod: 'getArtefact', arguments: [ServiceArtefactHandler.TYPE, _class.fullName]]);
            ConstructorArgumentValues values = new ConstructorArgumentValues();
            values.addGenericArgumentValue(_class.clazz);
            context.registerBeanDefinition AppUtil.getBeanName(it.simpleName), new GenericBeanDefinition(beanClass: TypeSpecifyableTransactionProxyFactoryBean,
                    constructorArgumentValues: values, propertyValues: [
                    target: new GenericBeanDefinition(factoryBeanName: _class.fullName + "ServiceClass", factoryMethodName: "newInstance"), proxyTargetClass: true,
                    transactionAttributeSource: new GroovyAwareNamedTransactionAttributeSource(), transactionManager: new RuntimeBeanReference("transactionManager")
            ])
        }
    }

    private static initializeTaglibClasses(ArrayList classes) {
        classes.each {
            GrailsClass _class = Holders.grailsApplication.addArtefact(TagLibArtefactHandler.TYPE, it);
            Holders.grailsApplication.mainContext.registerBeanDefinition _class.fullName, new GenericBeanDefinition(beanClass: _class.clazz, beanClassName: _class.fullName,
                    scope: BeanDefinition.SCOPE_SINGLETON, autowireMode: AbstractBeanDefinition.AUTOWIRE_BY_NAME)
        }
    }

    /**
     * @param place
     * @param attrs
     * @param body closure must return a string
     * @param out
     * @return
     */
    public static Object hookTag(String place, Map attrs, Closure body, Writer out) {
        boolean no_hook = true;
        loadedPlugins.each { _plugin ->
            PluginMeta.HookPoint hook = _plugin.hookPoints[place]
            if(hook) {
                no_hook = false;
                if(hook.taglib) {
                    def tagLib = gspTagLibraryLookup.lookupTagLibrary(hook.taglib, hook.callable);
                    if(tagLib) {
                        Closure _body = body
                        body = {
                            return tagLib."$hook.callable"(attrs, _body)
                        }
                    }
                } else if(hook.view) {
                    out << g.include(view: hook.view, params: attrs)
                }
            }
        }
        if(body) {
            out << body();
        }
    }

    public static installPlugin() {
        InputStream inputStream = new FileInputStream(AppUtil.servletContext.getRealPath("WEB-INF/AnacondaPlugin.zip"));
        String identifier = StringUtil.uuid
        String dir = AppUtil.servletContext.getRealPath("WEB-INF/plugins/" + identifier);
        ZipInputStream stream = new ZipInputStream(inputStream);
        while (true) {
            ZipEntry ze = stream.nextEntry;
            if (!ze) {
                break;
            }
            File outFile = new File(dir, ze.getName());
            if (ze.isDirectory()) {
                outFile.mkdirs();
            } else {
                if (!outFile.parentFile.exists()) {
                    outFile.parentFile.mkdirs();
                }
                FileOutputStream fout = new FileOutputStream(outFile);
                fout << stream
                stream.closeEntry();
                fout.close();
            }
        }
        stream.close();
        registerPlugin(new File(dir));
    }
}
