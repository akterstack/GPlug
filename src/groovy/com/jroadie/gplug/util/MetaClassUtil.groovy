package com.jroadie.gplug.util

import com.bitmascot.constants.DomainConstants
import grails.gorm.DetachedCriteria
import grails.orm.HibernateCriteriaBuilder
import groovy.time.TimeCategory
import org.codehaus.groovy.grails.plugins.web.mimes.FormatInterceptor
import org.codehaus.groovy.runtime.NullObject
import org.grails.datastore.mapping.query.api.QueryableCriteria
import org.hibernate.criterion.Property
import org.hibernate.internal.SessionImpl

import javax.servlet.http.HttpServletRequest
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.text.SimpleDateFormat

static init() {

    if(String.metaClass.hasProperty("toDate")) {
        return;
    }

    String.metaClass.toDate = { String format = null ->
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format ?: "yyyy-MM-dd hh:mm:ss");
            return dateFormat.parse(delegate)
        } catch(Throwable x) {
            return null;
        }
    }

    String.metaClass.toInteger = { Integer defaultInt ->
        try {
            return delegate.toInteger();
        } catch(Throwable x) {
            return defaultInt;
        }
    }

    String.metaClass.removeHTMLTag = {
        return delegate.replaceAll("\\<[^>]*>", "");
    }

    String.metaClass.truncate = { Integer length ->
        if(length > 3 && delegate.size() > length) {
            return delegate.substring(0, length - 3) + "...";
        }
        return delegate;
    }

    String.metaClass.toLong = { Long defaultLong ->
        try {
            return delegate.toLong();
        } catch(Throwable x) {
            return defaultLong;
        }
    }

    String.metaClass.toDouble = { Double defaultDouble ->
        try {
            return delegate.toDouble();
        } catch(Throwable x) {
            return defaultDouble;
        }
    }

    String.metaClass.toBoolean = { Boolean defaultBoolean = false ->
        if(delegate == "") {
            return defaultBoolean;
        }
        String check = delegate.toLowerCase()
        return check == "true" || check == "yes" || check == "on" || check == "1";
    }

    /**
     * It assumes the format is only a date format
     * default format is yyyy-MM-dd
     */
    String.metaClass.getDayStartTime = { String format = null ->
        return (delegate + " 00:00:00").toDate(format ? format + " hh:mm:ss" : null);
    }

    /**
     * It assumes the format is only a date format
     * default format is yyyy-MM-dd
     */
    String.metaClass.getDayEndTime = { String format = null ->
        return (delegate + " 23:59:59").toDate(format ? format + " hh:mm:ss" : null);
    }

    String.metaClass.toInitCapitalized = {
        return "${delegate.charAt(0).toUpperCase()}${delegate.substring(1).toLowerCase()}"
    }

    String.metaClass.sanitize = {
        return delegate.trim().toLowerCase().replaceAll(/\s/, "-").replaceAll("[^a-z0-9-\\._]+", "-")
    }

    String.metaClass.removeLast = { char match ->
        int index = delegate.lastIndexOf((int)match);
        String returnable = delegate;
        if(index != -1) {
            returnable = delegate.substring(0, index) + delegate.substring(index + 1);
        }
        return returnable;
    }

    String.metaClass.replaceLast = { char match, char replace ->
        int index = delegate.lastIndexOf((int)match);
        String returnable = delegate;
        if(index != -1) {
            returnable = delegate.substring(0, index) + replace + delegate.substring(index + 1);
        }
        return returnable;
    }

    String.metaClass.dotCase = {
        def writer = new StringWriter();
        def reader = new StringReader(delegate);
        char data;
        while((data = reader.read()) != -1) {
            if(data > ('@' as char) && data < ('[' as char)) {
                writer.append(".")
                writer.append((char)(data.charValue() + 32))
            } else {
                writer.append(data)
            }
        }
        return writer.toString()
    }

    NullObject.metaClass.toInteger = { Integer defaultInt = 0 ->
        return defaultInt;
    }

    NullObject.metaClass.toLong = { Long defaultLong = 0 ->
        return defaultLong;
    }

    NullObject.metaClass.toBoolean = { Boolean defaultBoolean = false ->
        return defaultBoolean;
    }

    Date.metaClass.toGMT = { timeZone = null ->
        long time = delegate.getTime() - (timeZone ? timeZone.rawOffset : TimeZone.default.rawOffset);
        return new Date(time);
    }

    Date.metaClass.toAppFormat = { type, showTime, showZone, timeZone ->
        if(type == "email") {
            return delegate.toEmailFormat();
        } else if(type == "admin") {
            return delegate.toAdminFormat(showTime, showZone, timeZone);
        } else {
            return delegate.toSiteFormat(showTime, showZone, timeZone);
        }
    }

    /**
     * It assumes that calling date is of GMT+0
     */
    Date.metaClass.toEmailFormat = {
        int offset = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.EMAIL, "time_zone").toInteger() * 60 * 1000;
        long time = delegate.getTime() + offset;
        String dateFormat = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.EMAIL, "date_format")
        String timeFormat = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.EMAIL, "time_format")
        return new Date(time).toFormattedString(dateFormat, true, timeFormat, true, new SimpleTimeZone(offset, "temp"));
    }

    /**
     * It assumes that calling date is of GMT+0
     */
    Date.metaClass.toAdminFormat = { showTime, showZone, timeZone ->
        long time = delegate.getTime() + timeZone.rawOffset;
        String dateFormat = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOCALE, "admin_date_format")
        String timeFormat = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOCALE, "admin_time_format")
        return new Date(time).toFormattedString(dateFormat, showTime, timeFormat, showZone, timeZone);
    }

    /**
     * It assumes that calling date is of GMT+0
     */
    Date.metaClass.toDatePickerFormat = { showTime, timeZone ->
        long time = delegate.getTime() + timeZone.rawOffset;
        String dateFormat = "yyyy-MM-dd"
        return new Date(time).toFormattedString(dateFormat, showTime, "HH:mm:ss", false, timeZone);
    }

    /**
     * It assumes that calling date is of GMT+0
     */
    Date.metaClass.toSiteFormat = { showTime, showZone, timeZone ->
        if(!timeZone) {
            timeZone = TimeZone.default
        }
        long time = delegate.getTime() + timeZone.rawOffset;
        String dateFormat = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOCALE, "site_date_format")
        String timeFormat = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOCALE, "site_time_format")
        return new Date(time).toFormattedString(dateFormat, showTime, timeFormat, showZone, timeZone);
    }

    /**
     * It assumes that calling date is of GMT+0
     */
    Date.metaClass.toZone = { timeZone ->
        if(!timeZone) {
            return delegate
        }
        long time = delegate.getTime() + timeZone.rawOffset;
        return new Date(time);
    }

    /**
     * It assumes that calling date is of GMT+0
     */
    Date.metaClass.toFormattedString = { datePattern, showTime, timePattern, showZone, timeZone ->
        String zoneString = "";
        if(showZone) {
            String symbol;
            int offset = timeZone.rawOffset
            if(offset < 0) {
                symbol = "-";
                offset *= -1;
            } else {
                symbol = "+";
            }
            double hour = offset / 1000 / 60 / 60;
            int hourOffset = Math.floor(hour);
            int minOffset = Math.floor((hour - hourOffset) * 60);
            zoneString = " (GMT" + symbol + hourOffset.toNDigit(2) + ":" + minOffset.toNDigit(2) + ")";
        }
        SimpleDateFormat formatted = new SimpleDateFormat(datePattern + (showTime ? " " + timePattern : ""));
        return formatted.format(delegate) + zoneString;
    }

    Date.metaClass.getDayStartTime = {
        return delegate.clearTime();
    }

    Date.metaClass.getDayEndTime = {
        def startTime = delegate.clearTime();
        use(TimeCategory) {
            return startTime + 1.days - 1.seconds;
        }
    }

    Integer.metaClass.toNDigit = { n ->
        return String.format("%0" + n + "d", delegate);
    }

    Number.metaClass.toPrice = { padZero = true ->
        return delegate.toFixed(2, padZero);
    }

    Number.metaClass.toLength = { padZero = true ->
        return delegate.toFixed(2, padZero);
    }

    Number.metaClass.toWeight = { padZero = true ->
        return delegate.toFixed(3, padZero);
    }

    Number.metaClass.toFixed = { n, padZero = true ->
        def string = String.format("%.${n}f", delegate);
        if(!padZero) {
            string = string.replaceAll(/(\.0*|0+)$/, "")
        }
        return string
    }

    DetachedCriteria.metaClass.deleteAll = {
        if(delegate.targetClass.metaClass.getMetaMethod("beforeDelete", [] as Object[])) {
            def domains = delegate.class.getDeclaredMethod("list", [] as Class[]).invoke(delegate, [] as Object[]);
            domains*.delete();
            return domains.size();
        } else {
            return delegate.class.getDeclaredMethod("deleteAll", [] as Class[]).invoke(delegate, [] as Object[]);
        }
    }

    DetachedCriteria.metaClass.updateAll = { Map map ->
        if(delegate.targetClass.metaClass.getMetaProperty("updated")) {
            map.updated = new Date().toGMT()
        }
        return delegate.class.getDeclaredMethod("updateAll", [Map] as Class[]).invoke(delegate, [map] as Object[]);
    }

    DetachedCriteria.metaClass.createAlias = { String path, String alias ->
        if(!delegate.metaClass.getMetaProperty("aliases")) {
            delegate.metaClass.aliases = [];
        }
        aliases.add([path: path, alias: alias]);
        return delegate;
    }

    HttpServletRequest.metaClass.withMime = { closure ->
        String accept = delegate.getHeader("Accept");
        String mime = accept ? (accept.matches(/.*application\/json.*/) ? "json" : (accept.matches(/.*text\/xml.*/) ? "xml" : "html")) : "html";
        LinkedHashMap<String, Object> formats = null
        def original = closure.delegate
        try {
            final interceptor = new FormatInterceptor()
            closure.delegate = interceptor
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure.call()
            formats = interceptor.formatOptions
        } finally {
            closure.delegate = original
            closure.resolveStrategy = Closure.OWNER_FIRST
        }
        if(!formats.size()) {
            return null;
        }
        Closure mimeFunc = formats[mime];
        if(mimeFunc) {
            return mimeFunc.call();
        } else {
            return null;
        }
    }

    HttpServletRequest.metaClass.getIp = {
        return delegate.getHeader("X-Real-IP") ?: delegate.getRemoteAddr()
    }

    HibernateCriteriaBuilder.metaClass.inList = { String propertyName, QueryableCriteria propertyValue ->
        def hCriterion = getHibernateDetachedCriteria(propertyValue);
        propertyValue.aliases.each {
            hCriterion.createAlias(it.path, it.alias)
        }
        addToCriteria(Property.forName(propertyName).in(hCriterion));
        return delegate;
    }

    SessionImpl.metaClass.discard = {
        if(delegate.transactionInProgress) {
            List evictables = []
            delegate.persistenceContext.entitiesByKey.values().each {
                if(it.isDirty()) {
                    evictables + it
                }
            }
            evictables*.discard()
        }
    }

    File.metaClass.created = {
        BasicFileAttributes attributes = Files.readAttributes(delegate.toPath(), BasicFileAttributes.class);
        FileTime creationTime = attributes.creationTime();
        return creationTime.toMillis();
    }
}
