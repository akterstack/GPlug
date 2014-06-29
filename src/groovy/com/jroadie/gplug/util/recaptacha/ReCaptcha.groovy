package com.jroadie.gplug.util.recaptacha

import com.bitmascot.constants.DomainConstants
import com.bitmascot.util.AppUtil

public class ReCaptcha {
    public static final String HTTP_SERVER = "http://www.google.com/recaptcha/api"
    public static final String HTTPS_SERVER = "https://www.google.com/recaptcha/api"
    public static final String VERIFY_URL = "/verify"
    public static final String AJAX_JS = "/js/recaptcha_ajax.js"

    String publicKey
    String privateKey
    Boolean useSecureAPI
    Boolean forceLanguageInURL = false

    public String createRecaptchaHtml(String errorMessage, Map options) {
        publicKey = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_public_key");
        def recaptchaServer = useSecureAPI ? HTTPS_SERVER : HTTP_SERVER
        def qs = new QueryString([k: publicKey, error: errorMessage])
        if (forceLanguageInURL && options?.lang) {
            qs.add("hl", URLEncoder.encode(options.remove("lang")))
        }

        def message = new StringBuffer()
        if (options) {
            message << "<script type=\"text/javascript\">\r\nvar RecaptchaOptions = {" +
                    options.collect { "$it.key:'${it.value}'" }.join(', ') +
                    "};\r\n</script>\r\n"
        }
        message << "<script type=\"text/javascript\" src=\"$recaptchaServer/challenge?${qs.toString()}\"></script>\r\n"
        return message.toString()
    }

    public Map checkAnswer(String remoteAddr, String challenge, String response) {
        privateKey = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_private_key")
        def recaptchaServer = useSecureAPI ? HTTPS_SERVER : HTTP_SERVER
        def post = new Post(url: recaptchaServer + VERIFY_URL)
        post.queryString.add("privatekey", privateKey)
        post.queryString.add("remoteip", remoteAddr)
        post.queryString.add("challenge", challenge)
        post.queryString.add("response", response)

        def responseMessage = post.text

        if (!responseMessage) {
            return [valid: false, errorMessage: "Null read from server."]
        }

        def a = responseMessage.split("\r?\n") as List
        if (a.isEmpty()) {
            return [valid: false, errorMessage: "No answer returned from recaptcha: $responseMessage"]
        }
        def isValid = "true".equals(a[0])
        def errorMessage = null;
        if (!isValid) {
            errorMessage = a[1] ?: "Unknown error"
        }
        [valid: isValid, errorMessage: errorMessage]
    }
}