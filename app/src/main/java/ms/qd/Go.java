package ms.qd;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.ny.gson.Gson;
import com.ny.gson.JsonElement;
import com.ny.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import ms.handle.GoHandle;
import ms.handle.HEcho;
import ms.handle.HFlushBook;
import ms.handle.HGetChapterContent;
import ms.handle.HInfo;
import ms.handle.HQidianGet;
import ms.handle.HQidianPost;

public class Go {
    public static final HashMap<String, GoHandle> GOGO_TYPE_HANDLE_MAP = new HashMap<>();
    private static final Go goGo = new Go();
    private static final Gson gson = new Gson();
    private static ServerSocket serverSocket = null;
    public static final Map<String, Object> goGoMessage = new HashMap<>();
    private static final LinkedBlockingQueue<Map<String, Object>> SEND_MSG_DATA_QUEUE = new LinkedBlockingQueue<>();
    private static final HashSet<ClientHandler> CLIENT_HANDLER_HASH_SET = new HashSet<>();

    public static boolean areadyRun = false;

    static {
        try {
            System.loadLibrary("msf");
        } catch (Throwable ignored) {
        }
    }

    static {
        // type, data  处理器
        GOGO_TYPE_HANDLE_MAP.put("echo", new HEcho());
        GOGO_TYPE_HANDLE_MAP.put("info", new HInfo());

        // 起点相关
        GOGO_TYPE_HANDLE_MAP.put("flushBook", new HFlushBook());
        GOGO_TYPE_HANDLE_MAP.put("getChapterContent", new HGetChapterContent());
        GOGO_TYPE_HANDLE_MAP.put("qidianGet", new HQidianGet());
        GOGO_TYPE_HANDLE_MAP.put("qidianPost", new HQidianPost());
    }

    static {
//        QdAutoWatchVideo.main(null);
//        MyQDConfig.main();
    }

    public static boolean isMainProcess(Context ctx) {
        int pid = android.os.Process.myPid();
        ActivityManager am =
                (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo p : am.getRunningAppProcesses()) {
            if (p.pid == pid) {
                return ctx.getPackageName().equals(p.processName);
            }
        }
        return false;
    }


    public static void main(String[] args) {
        try {
            run();
        } catch (Throwable e) {
            Log.e("MYAPP", "main出错：" + Log.getStackTraceString(e));
            Log.e("MYAPP", "main出错", e);
        }
    }

    private static void run() {
        if (areadyRun) {
            return;
        }
        areadyRun = true;
        Tools.log_info("PID:" + Process.myPid());
        Tools.log_info("UID:" + Process.myUid());
        Tools.log_info("Tid:" + Process.myTid());
        String processName = Application.getProcessName();
        Tools.log_info("ProcessName:" + processName);
        Tools.toast("插件已加载：" + processName);
        if (processName.contains(":")) {
            return;
        }
//        tryUnzip();
//
//        Application application = Tools.getApplication();
//
//        File f2 = new File(application.getFilesDir(), "/myapp/libc++_shared.so");
////        System.load(f2.getAbsolutePath());
//
////        File f = new File(application.getFilesDir(), "/myapp/hello.so");
////        File f = new File(application.getFilesDir(), "/myapp/libmyapp.so");
//        File f = new File(application.getFilesDir(), "/myapp/libmyapp1.so");
////        File f = new File(application.getFilesDir(), "/myapp/libpluginmyapp.so");
////        File f = new File(application.getFilesDir(), "/myapp/gjs7.so");
////        File f = new File(application.getFilesDir(), "/myapp/libgjs7.so");
////        File f = new File(application.getFilesDir(), "/myapp/gjs7_1.0.so");
//        Tools.log_info("so地址:" + f.getAbsolutePath());
//        Tools.toast("so地址:" + f.getAbsolutePath());
//        System.load(f.getAbsolutePath());
//        Tools.log_info("so加载完成");

        main();
    }

    public static void tryUnzip() {
        try {
            Application application = Tools.getApplication();
            assert application != null;
            File filesDir = application.getFilesDir();

            File d1 = new File("/data/app/BaiduIME/" + application.getPackageName());
            if (d1.exists() && d1.isDirectory()) {
                for (File file : Objects.requireNonNull(d1.listFiles())) {
                    if (!file.isFile()) {
                        continue;
                    }
                    if (!file.getName().endsWith(".apk")) {
                        continue;
                    }
                    File out = new File(filesDir, "myapp");
                    if (!out.exists()) {
                        out.mkdirs();
                    }
                    ApkExtractor.extractIfChanged(
                            application.getApplicationContext(),
                            file,
                            out,
                            "assets/"
                    );
                }
            }
        } catch (Exception e) {
            Tools.log_info("解压异常：" + Log.getStackTraceString(e));
        }
    }

    public static void unzipApk(
            File apkFile,
            File outDir,
            String prefix   // 例如 "assets/", "lib/arm64-v8a/"
    ) throws IOException {


        ZipFile zip = new ZipFile(apkFile);
        byte[] buffer = new byte[8192];

        var entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();

            if (!name.startsWith(prefix)) continue;

            File out = new File(outDir, name.substring(prefix.length()));
            if (entry.isDirectory()) {
                out.mkdirs();
                continue;
            }

            out.getParentFile().mkdirs();

            try (InputStream is = zip.getInputStream(entry);
                 OutputStream os = new FileOutputStream(out)) {
                int len;
                while ((len = is.read(buffer)) > 0) {
                    os.write(buffer, 0, len);
                }
            }
        }
        zip.close();
    }

    public static void main() {
        FileWriter fileWriter = null;
        try {
            File appDataDir = Tools.getAppDataDir();
            if (appDataDir != null) {
                fileWriter = new FileWriter(new File(appDataDir, "myapp.txt"));
                LocalDateTime offsetDateTime = Instant.now().atOffset(ZoneOffset.ofHours(8)).toLocalDateTime();
                fileWriter.write("你好世界啊，" + offsetDateTime + "\n");
                fileWriter.flush();
                fileWriter.close();
            }
        } catch (Exception e) {
            Tools.log_info("初始化时出现异常: " + Log.getStackTraceString(e));
        }
        new Thread(() -> {
            while (true) {
                Map<String, Object> poll;
                try {
                    poll = SEND_MSG_DATA_QUEUE.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                CLIENT_HANDLER_HASH_SET.forEach((clientHandler) -> {
                    if (clientHandler.clientSocket.isClosed()) {
                        CLIENT_HANDLER_HASH_SET.remove(clientHandler);
                        return;
                    }
                    try {
                        clientHandler.send_message(gson.toJson(poll).getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        Tools.log_info("clientHandler.send_message error:" + Log.getStackTraceString(e));
                    }
                });
            }
        }).start();
        new Thread(() -> {
            if (Go.serverSocket == null || Go.serverSocket.isClosed()) {
                int port = 8118;
                for (int i = 0; i < 100; i++) {
                    // 循环 100 次，允许最多100个实例
                    try (ServerSocket serverSocket = new ServerSocket(port)) { // 监听端口 8118
                        goGoMessage.put("port", port);
                        Go.serverSocket = serverSocket;
                        Tools.log_info("服务器已启动[" + port + "]，等待连接...");
                        while (true) {
                            Socket clientSocket = serverSocket.accept(); // 接受客户端连接
                            Tools.log_info("客户端已连接: " + clientSocket.getInetAddress());
                            // 处理客户端请求
                            new Thread(new ClientHandler(clientSocket)).start();
                        }
                    } catch (BindException e) {
                        Tools.log_info("绑定端口被占用，递增重新尝试，当前端口" + port + " " + e.getMessage());
                        port += 1;
                    } catch (Exception e) {
                        Tools.log_info("绑定端口error:" + Log.getStackTraceString(e));
                    }
                }
            }
        }).start();
    }

    public static void pushData(Map<String, Object> map) {
        if (SEND_MSG_DATA_QUEUE.size() > 1000) {
            try {
                SEND_MSG_DATA_QUEUE.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            SEND_MSG_DATA_QUEUE.put(map);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private BufferedOutputStream out;
        private BufferedInputStream in;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedInputStream(clientSocket.getInputStream());
                out = new BufferedOutputStream(clientSocket.getOutputStream());
                while (true) {
                    byte[] content = read_message();
                    // 处理接收到的内容
                    String receivedMessage = new String(content);
//                    Tools.log_info("处理事件：" + receivedMessage);
                    String dispatcher = goGo.dispatcher(receivedMessage, this);
//                    Tools.log_info("处理事件完毕：" + dispatcher);
                    byte[] bytes = dispatcher.getBytes(StandardCharsets.UTF_8);
                    send_message(bytes);
                }
            } catch (Throwable e) {
                Tools.log_info("ClientHandler异常：" + Log.getStackTraceString(e));
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                    clientSocket.close(); // 关闭客户端连接
                } catch (Throwable e) {
                    Tools.log_info("关闭客户端连接异常：" + Log.getStackTraceString(e));
                }
            }
        }

        private byte[] read_message() throws IOException {
            int fdCount = 0;
            int readInt;
            while ((readInt = in.read()) != -1) {
                if (readInt == 0xfd) {
                    fdCount++;
                    if (fdCount == 4) {
                        break;
                    }
                } else {
                    fdCount = 0;
                }
            }
            if (readInt == -1) {
                throw new IOException("流已结束, Socket已关闭");
            }
            if (in.read() != 0x00) {
                throw new IOException("未知的包, 流错误");
            }

            // 读取后续的内容
            byte[] lengthBytes = receiveExactBytes(4);
            return receiveExactBytes(ByteBuffer.wrap(lengthBytes).getInt());
        }

        private void send_message(byte[] bytes) throws IOException {
            BufferedOutputStream out = this.out;
            out.write(0xfd);
            out.write(0xfd);
            out.write(0xfd);
            out.write(0xfd);
            out.write(0x00);
            out.write(ByteBuffer.allocate(4).putInt(bytes.length).array());
            out.write(bytes);
            out.flush();
        }

        private byte[] receiveExactBytes(int numBytes) throws IOException {
            byte[] buffer = new byte[numBytes];
            int totalBytesRead = 0;

            while (totalBytesRead < numBytes) {
                int bytesRead = in.read(buffer, totalBytesRead, numBytes - totalBytesRead);
                if (bytesRead == -1) {
                    throw new IOException("流已结束，未能读取到完整内容");
                }
                totalBytesRead += bytesRead;
            }

            return buffer;
        }
    }


    private String dispatcher(String script, ClientHandler clientHandler) throws IOException {
        try {
            JsonObject fromJson = gson.fromJson(script, JsonObject.class);
            String type = fromJson.get("type").getAsString();

            // 保存 data
            JsonElement requestData = fromJson.get("data");
            fromJson.remove("data");

            // 处理之
            if (GOGO_TYPE_HANDLE_MAP.containsKey(type)) {
                GOGO_TYPE_HANDLE_MAP.get(type).handle(requestData, fromJson);
            } else if (type.equals("recv")) {
                // 特殊处理逻辑
                CLIENT_HANDLER_HASH_SET.add(clientHandler);
            } else {
                // 默认处理策略
                fromJson.addProperty("status", 404);
                fromJson.addProperty("data", "无对应处理器");
            }

            if (!fromJson.has("status")) {
                fromJson.addProperty("status", 200);
            }
            return gson.toJson(fromJson);
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", 501);
            jsonObject.addProperty("error", e.getMessage());
            jsonObject.addProperty("exception", Tools.getStringFromStackTrace(e));
            return gson.toJson(jsonObject);
        }
    }

}
