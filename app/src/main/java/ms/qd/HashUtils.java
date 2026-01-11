package ms.qd;

import com.ny.gson.Gson;
import com.ny.gson.JsonElement;
import com.ny.gson.JsonObject;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtils {

    public static String md5File(File file) throws IOException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // Android/Java 标准都有 MD5
        }

        byte[] buf = new byte[1024 * 1024]; // 1MB buffer
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            int n;
            while ((n = is.read(buf)) > 0) {
                md.update(buf, 0, n);
            }
        }

        byte[] digest = md.digest();
        return toHexLower(digest);
    }

    private static String toHexLower(byte[] bytes) {
        char[] hex = "0123456789abcdef".toCharArray();
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            out[i * 2] = hex[v >>> 4];
            out[i * 2 + 1] = hex[v & 0x0F];
        }
        return new String(out);
    }

}
