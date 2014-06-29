package com.jroadie.gplug.util

/**
 * Created by zobair on 19/11/13.*/
class StringUtil {
    public static String getCapitalizedAndPluralName(String name) {
        String pname = name.capitalize() + "s";
        if(pname.endsWith("ys")) {
            pname = pname.replaceAll(/ys$/, "ies");
        } else if(pname.endsWith("xs")) {
            pname = pname.replaceAll(/xs$/, "xes");
        }
        return pname
    }

    public static String getUuid() {
        return UUID.randomUUID().toString().toUpperCase()
    }
}
