package com.jroadie.gplug.util.security

import com.jroadie.gplug.util.Base64Coder
import org.apache.commons.lang.StringUtils

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest
import java.text.SimpleDateFormat

public class InformationEncrypter {
    private final String text = "DJKIR46JHSD023REKSDFSDKLF219546JMASDADF70233JASDH7";
    private final String applicationName = "GRAILS_WEB_COMMANDER";
    private final String applicationVersion = "2.0";

    private List hiddenInfos = new ArrayList();
    private long validTimeDuration = 60000;

    public InformationEncrypter() {}

    public InformationEncrypter(String key) {
        verifyKey(key, true);
    }

    public InformationEncrypter(String key, boolean verifyTime) {
        verifyKey(key, verifyTime);
    }

    public InformationEncrypter(String key, long validityDuration) {
        validTimeDuration = validityDuration;
        verifyKey(key, true);
    }

    public void hideInfo(String info) {
        hiddenInfos.add(info);
    }

    private void verifyKey(String key, boolean verifyTime) {
        try {
            String matchKey = String.format("%1\$tY-%1\$tb-%1\$td-%1\$tH-%1\$tM-%1\$tS", [Calendar.getInstance(TimeZone.getTimeZone("GMT"))] as Object[]);
            String[] parts = key.replace(" ", "").split("---");
            if(parts.length != 2) {
                throw new Exception();
            }
            if(verifyTime) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MMM-dd-HH-mm-ss");
                Date dateInKey = formatter.parse(parts[1]);
                Date matchDate = formatter.parse(matchKey);
                if(matchDate.getTime() - dateInKey.getTime() > validTimeDuration) {
                    throw new Exception();
                }
            }
            byte[] ciphertext = Base64Coder.decode(parts[0]);

            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] originKey = digest.digest(parts[1].getBytes("UTF-8"));
            byte[] newKey = new byte[24];
            for(int h=0; h<8; h++) {
                newKey[h+16] = (byte)(originKey[h] + originKey[h + 8]);
            }
            SecretKey sKey = new SecretKeySpec(newKey, "DESede");
            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            cipher.init(Cipher.DECRYPT_MODE, sKey, iv);
            String joinedText = new String(cipher.doFinal(ciphertext));
            String[] jparts = joinedText.split(",");
            if(jparts.length < 4) {
                throw new Exception();
            }
            for(int h=4; h<jparts.length; h++) {
                hiddenInfos.add(jparts[h]);
            }
            if(!jparts[3].equals(parts[1])) {
                throw new Exception();
            }
        } catch(Throwable k) {
            throw new RuntimeException("Could not verify key");
        }
    }

    public List getHiddenInfos() {
        return hiddenInfos;
    }

    @Override
    public String toString() {
        return generateKey();
    }

    private String generateKey() {
        try {
            String key = String.format("%1\$tY-%1\$tb-%1\$td-%1\$tH-%1\$tM-%1\$tS", [Calendar.getInstance(TimeZone.getTimeZone("GMT"))] as Object[]);
            String joinedText = text + "," + applicationName + "," + applicationVersion + "," + key;
            if(hiddenInfos.size() > 0) {
                joinedText += "," + StringUtils.join(hiddenInfos, ',');
            }
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] originKey = digest.digest(key.getBytes("UTF-8"));
            byte[] newKey = new byte[24];
            for(int h=0; h<8; h++) {
                newKey[h+16] = (byte)(originKey[h] + originKey[h + 8]);
            }
            SecretKey sKey = new SecretKeySpec(newKey, "DESede");
            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            cipher.init(Cipher.ENCRYPT_MODE, sKey, iv);
            byte[] cipherText = cipher.doFinal(joinedText.getBytes("UTF-8"));
            return Base64Coder.encode(cipherText) + "---" + key;
        } catch(Throwable t) {
            return null;
        }
    }
}
