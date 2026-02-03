package ms.qd;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.ny.gson.Gson;
import com.ny.gson.JsonObject;
import com.qidian.QDReader.framework.network.qd.QDHttpClient;
import com.qidian.QDReader.framework.network.qd.QDHttpResp;

import java.io.File;
import java.io.FileWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Tools {
    public static Gson gson = new Gson();
    private static File logFile = null;
    private static File appDataDir = null;

    private static Application app = null;

    //  440/443 修改处理之
    //  类：包含 okhttp_request_QDHttpClient 的 com.qidian.QDReader.framework.network.qd.QDHttpClient
    //  var QDHttpClientJudian = Java.use('com.qidian.QDReader.framework.network.qd.QDHttpClient$judian')
    //  Get
    //  let o = QDHttpClientJudian.$new().judian().j("https://druidv6.if.qidian.com/argus/api/v3/chapterlist/chapterlist?bookId=1035420986&timeStamp=1769776758000&requestSource=0&md5Signature=7ee222c32c975253eb421e749e1be2a4&extendchapterIds=768559860,774413797,852160051,878298066")
    //  POST QDHttpClientJudian.$new().judian().o(...)
    public static QDHttpClient QDHttpClient = new QDHttpClient.judian().judian();

    public static QDHttpResp qdGet(String url) {
        // 比对签名  443 QDHttpResp i(String var1)
        return QDHttpClient.j(url);
    }

    public static QDHttpResp qdPost(String url, ContentValues contentValues) {
        // 比对签名  443 QDHttpResp n(String str, ContentValues contentValues)
        return QDHttpClient.o(url, contentValues);
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
            log_info("获取Application出错:" + Log.getStackTraceString(e));
        }
        return null;
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

    private static Handler MAIN = null;

    public static void toast(String msg) {
        try {
            if (MAIN == null) {
                MAIN = new Handler(Looper.getMainLooper());
            }
            Context context = Objects.requireNonNull(getApplication()).getApplicationContext();
            MAIN.post(() -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            log_info("toast出错:" + Log.getStackTraceString(e));
        }
    }

    static boolean isPortAvailable(int port) {
        try (ServerSocket ss = new ServerSocket()) {
            ss.setReuseAddress(false);
            ss.bind(new InetSocketAddress("0.0.0.0", port));
            return true;   // 可用
        } catch (Exception e) {
            return false;  // 被占用 / 无权限
        }
    }

    static boolean canConnect(String host, int port, int timeoutMs) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;   // 有服务
        } catch (Exception e) {
            return false;  // 不可达 / 无服务
        }
    }

}
