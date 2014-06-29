package com.jroadie.gplug.util

import org.apache.commons.io.FilenameUtils

/**
 * Created by zobair on 07/01/14.*/
class URLUtil {
    public static String fileExtension(String url) {
        String ext = FilenameUtils.getExtension(url)
        if(ext && ext.indexOf((int)('/' as char)) > -1) {
            return null
        }
        return ext;
    }
}
