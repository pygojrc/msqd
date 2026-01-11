package ms.qd;

import android.content.Context;
import android.content.SharedPreferences;

public final class Md5Store {
    private static final String SP_NAME = "apk_extract_cache";
    private static final String KEY_MD5_PREFIX = "md5:"; // 后面拼 apkPath 或包名等

    public static String load(Context ctx, String keySuffix) {
        SharedPreferences sp = ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_MD5_PREFIX + keySuffix, null);
    }

    public static void save(Context ctx, String keySuffix, String md5) {
        SharedPreferences sp = ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_MD5_PREFIX + keySuffix, md5).apply();
    }
}
