package com.jroadie.gplug.util

import grails.gsp.PageRenderer
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsHttpSession
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.codehaus.groovy.grails.web.util.WebUtils
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AppUtil {
    static ServletContext servletContext;
    private static def site_config = null;

    public static String getBeanName(String name) {
        char firstChar = name.charAt(0);
        char secondChar = name.charAt(1);
        if(secondChar > 96 && firstChar < 97 && firstChar != '_') {
            firstChar = firstChar + 32;
        }
        return "" + firstChar + name.substring(1);
    }

    public static clearConfig() {
        site_config = null;
    }

    public static GrailsParameterMap getParams() {
        try {
            RequestContextHolder.currentRequestAttributes().params;
        } catch (Throwable t) {
            return null;
        }
    }

    public static GrailsHttpSession getSession() {
        try {
            WebUtils.retrieveGrailsWebRequest().session;
        } catch (Throwable t) {
            return null;
        }
    }

    public static HttpServletRequest getRequest() {
        try {
            WebUtils.retrieveGrailsWebRequest().request;
        } catch (Throwable t) {
            return null;
        }
    }

    public static HttpServletResponse getResponse() {
        try {
            WebUtils.retrieveGrailsWebRequest().response;
        } catch (Throwable t) {
            return null;
        }
    }

    public static String convertToByteNotation(Long size) {
        if (size < 1024) {
            return size.toString() + " B";
        }
        size = size / 1024;
        if (size < 1024) {
            return size.toString() + " KB";
        }
        size = size / 1024;
        if (size < 1024) {
            return size.toString() + " MB";
        }
        size = size / 1024;
        return size.toString() + " GB";
    }

    public static String pluginPackageCase(string) {
        def parts = string.split("-");
        if(parts.size() == 1) {
            return string;
        }
        parts.eachWithIndex { v, i ->
            if(i == 0) {
                string = v;
                return;
            }
            string += v.capitalize()
        }
        return string;
    }

    public static waitFor(Object obj, String property, Object compareValue, Long timeout = 30000) {
        if(obj."$property" != compareValue) {
            Thread.sleep(1000)
            waitFor(obj, property, compareValue, timeout - 1000)
        }
    }

    public static GrailsWebRequest initialDummyRequest() {
        HttpServletRequest _request = PageRenderer.PageRenderRequestCreator.createInstance("/page/dummy")
        _request.IS_DUMMY = true;
        GrailsWebRequest webRequest = new GrailsWebRequest(_request, PageRenderer.PageRenderResponseCreator.createInstance(new PrintWriter(new StringWriter())), servletContext)
        RequestContextHolder.setRequestAttributes(webRequest)
        return webRequest
    }

    public static def initializeDefaultImages(List<String> types) {
        types.each { type ->
            def imagePath = servletContext.getRealPath("resources/$type/default");
            File imageLok = new File(imagePath);
            if(!imageLok.list()?.length) {
                def repositoryPath = servletContext.getRealPath("WEB-INF/system-resources/default-images/$type");
                File repositoryLok = new File(repositoryPath);
                repositoryLok.eachFile { image ->
                    File copyImage = new File(imageLok.absolutePath + "/" + image.name)
                    copyImage.parentFile.mkdirs()
                    copyImage.createNewFile()
                    image.withInputStream { stream ->
                        copyImage << stream
                    }
                }
            }
        }
    }

    public static String getQueryStringFromMap(Map params) {
        if(params.size() == 0) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for (Map.Entry param: params.entrySet()) {
            buffer.append("&").append(param.key).append("=").append(param.value.encodeAsURL());
        }
        return buffer.substring(1);
    }

    public static Map<String, String> getURLQueryMap(String query) {
        String[] params = query.split("&")
        Map<String, String> map = new HashMap<String, String>()
        for (String param: params) {
            String name = param.split("=")[0]
            String value = param.split("=")[1]
            map.put(name, value.decodeURL())
        }
        return map
    }
}
