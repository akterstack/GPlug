package com.jroadie.gplug.util

/**
 * Created by zobair on 19/11/13.
 */
class SortUtil {
    public static Collection sortInCustomOrder(Collection objs, String matchProperty, List order) {
        return objs.sort {
            order.indexOf(it[matchProperty])
        }
    }
}
