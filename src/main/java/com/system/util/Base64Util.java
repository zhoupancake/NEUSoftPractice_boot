package com.system.util;

import java.util.Base64;

public class Base64Util {
    public static byte[] decodeBase64(String base64EncodedString) {
        if (base64EncodedString == null || base64EncodedString.isEmpty()) {
            return null;
        }
        return Base64.getDecoder().decode(base64EncodedString);
    }

    public static String decodeBase64ToString(String base64EncodedString) {
        if (base64EncodedString == null || base64EncodedString.isEmpty()) {
            return null;
        }
        return new String(Base64.getDecoder().decode(base64EncodedString));
    }
}