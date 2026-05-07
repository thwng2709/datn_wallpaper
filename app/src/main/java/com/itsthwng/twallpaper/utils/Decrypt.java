package com.itsthwng.twallpaper.utils;

import android.util.Base64;
import android.util.Log;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Decrypt {
    private static final String TAG = "AES";
    private static final byte[] keyValue = {49, 50, 51, 109, 105, 110, 104,
            110, 100, 75, 101, 121, 115, 52, 53, 54};

    public static String decrypt(String paramString) {
        try {
            paramString = getStringDecode(paramString);
            Key localKey = generateKey();
            Cipher localCipher = Cipher.getInstance(TAG);
            localCipher.init(2, localKey);
            String str = new String(localCipher.doFinal(MyBase64.decode(paramString)));
            return str;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Decode base 64 to string;
     *
     * @param string
     * @return
     */
    public static String decodeBase64String(String string) {
        try {
            return new String(Base64.decode(string, Base64.DEFAULT), "UTF-8");
        } catch (Exception ex) {
            // Logging.
            Log.e(TAG, "Error: " + ex.getMessage());
        }

        return "";
    }

    public static String encodeToBase64(String source) {
        try {
            return Base64.encodeToString(source.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (Exception ex) {
            // Logging.
            Log.e(TAG, "Error: " + ex.getMessage());
        }

        return "";
    }

    private static Key generateKey() throws Exception {
        return new SecretKeySpec(keyValue, TAG);
    }

    private static String getStringDecode(String str) {
        String temp = "", result = "";
        for (int i = str.length() - 1; i >= 0; i--) {
            temp = temp + str.charAt(i);
        }
        for (int i = 0; i < temp.length(); i++) {
            if (i != 4 && i != 7 && i != 10) {
                result = result + temp.charAt(i);
            }
        }

        return result;
    }

}
