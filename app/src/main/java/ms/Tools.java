package ms;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.ny.gson.Gson;
import com.ny.gson.JsonObject;
import com.qidian.QDReader.framework.network.qd.QDHttpResp;

import java.io.File;
import java.io.FileWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Tools {
    public static Gson gson = new Gson();
    private static File logFile = null;
    private static File appDataDir = null;

    private static Application app = null;

    public static com.qidian.QDReader.framework.network.search getQidianSearch() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        // f8.search >> l8.search
        // p1114p8.search
        return p8.search.cihai();
//        Class<?> clazz = Class.forName("l8.search");
//        Field field = clazz.getDeclaredField("d");
//        field.setAccessible(true);
//        Object value = field.get(null);
//        return (search) value;
    }

    public static void qidian_http_resp_handle(JsonObject fromJson, QDHttpResp cihai) {
        if (cihai.isSuccess()) {
            fromJson.addProperty("status", 200);
            fromJson.addProperty("http_response_code", cihai.search());
            fromJson.addProperty("data", cihai.getData());
        } else {
            fromJson.addProperty("status", 400);
            fromJson.addProperty("http_response_code", cihai.search());
            fromJson.addProperty("errorMessage", cihai.getErrorMessage());
            fromJson.addProperty("data", cihai.getData());
        }
    }

    public static String getStringFromStackTrace(Throwable e) {
        StringBuilder stringBuilder = new StringBuilder(e.toString());
        stringBuilder.append("\n");
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            stringBuilder.append("    at ");
            stringBuilder.append(stackTraceElement);
            stringBuilder.append("\n");
        }
        Throwable cause = e.getCause();
        if (cause != null) {
            stringBuilder.append("Caused by ");
            stringBuilder.append(cause);
            for (StackTraceElement stackTraceElement : cause.getStackTrace()) {
                stringBuilder.append("    at ");
                stringBuilder.append(stackTraceElement);
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }

    public static void log_info(String content) {
        writeToLogFile("INFO " + content);
        Log.i("MYAPP", content);
    }

    public static void writeToLogFile(String content) {
        try {
            if (logFile == null) {
                File appDataDir = getAppDataDir();
                String format = Instant.now().atOffset(ZoneOffset.ofHours(8)).toLocalDate().format(DateTimeFormatter.ISO_DATE);
                logFile = new File(appDataDir, "myapp_" + format + ".log");
            }
            if (!logFile.exists()) {
                boolean newFile = logFile.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(logFile.getAbsolutePath(), true);
            LocalDateTime now = Instant.now().atOffset(ZoneOffset.ofHours(8)).toLocalDateTime();
            fileWriter.write("[" + now.format(DateTimeFormatter.ISO_DATE_TIME) + "] ");
            fileWriter.write(content);
            fileWriter.write("\n");
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception ignored) {

        }

    }

    public static File getAppDataDir() {
        if (Tools.appDataDir != null) {
            return Tools.appDataDir;
        }
        try {
            Application currentApplication = getApplication();
            assert currentApplication != null;
            Context context = currentApplication.getApplicationContext();
            // 拿到目录
            File appDataDir = context.getExternalFilesDir(null);
            Tools.appDataDir = appDataDir;
            return appDataDir;
        } catch (Throwable e) {
        }
        return null;
    }

    public static Application getApplication() {
        try {
            if (app == null) {
                app = (Application) Class.forName("android.app.ActivityThread").getDeclaredMethod("currentApplication").invoke(null);
            }
            return app;
        } catch (Exception e) {
            return null;
        }

    }

    @SuppressLint("DiscouragedPrivateApi")
    public static Long getAppVersion() {
        try {
            Application currentApplication = getApplication();
            String pkg = currentApplication.getPackageName();
            PackageManager pm = currentApplication.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(pkg, 0);
            String versionName = pi.versionName;   // 如 "1.2.3"
            // 如 123
            return pi.getLongVersionCode();
        } catch (Exception e) {
            log_info("获取app版本号异常：" + e + "\n" + Log.getStackTraceString(e));
            return null;
        }
    }
}
