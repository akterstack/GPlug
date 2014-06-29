package com.jroadie.gplug.util

public class PropertyReader {
    private static Properties ppt;
    public final static String CONFIG_FILE_NAME = "appConfig.properties";

    static getProperty(def name, def defaultV) {
        if (ppt == null) {
            ppt = new Properties();
            try {
                URL url = PropertyReader.class.getResource("ConfigurationReader.class");
                String thisPath = url.getPath();
                thisPath = thisPath.substring(0, thisPath.lastIndexOf("classes")) + CONFIG_FILE_NAME;
                url = new URL("file://" + thisPath);
                InputStream stream = url.openStream();
                ppt.load(stream);
                stream.close();
            } catch (Throwable k) {}
        }
        def result = ppt.getProperty(name, defaultV.toString());
        return result != "" ? result : defaultV;
    }
}