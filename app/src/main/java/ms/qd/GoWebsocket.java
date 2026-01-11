package ms.qd;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.ny.gson.Gson;
import com.ny.gson.JsonElement;
import com.ny.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.net.InetSocketAddress;
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

import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import ms.handle.GoHandle;
import ms.handle.HEcho;
import ms.handle.HFlushBook;
import ms.handle.HGetChapterContent;
import ms.handle.HInfo;
import ms.handle.HQidianGet;
import ms.handle.HQidianPost;

public class GoWebsocket {
    public static final HashMap<String, GoHandle> GOGO_TYPE_HANDLE_MAP = new HashMap<>();
    private static final GoWebsocket goGo = new GoWebsocket();
    private static final Gson gson = new Gson();
    private static WebSocketServer serverSocket = null;
    public static final Map<String, Object> goGoMessage = new HashMap<>();
    private static final LinkedBlockingQueue<Map<String, Object>> SEND_MSG_DATA_QUEUE = new LinkedBlockingQueue<>();
    private static final HashSet<WebSocket> CLIENT_HANDLER_HASH_SET = new HashSet<>();

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

        main();
    }

    public static void main() {
        try {
            File appDataDir = Tools.getAppDataDir();
            if (appDataDir != null) {
                try (FileWriter fileWriter = new FileWriter(new File(appDataDir, "myapp.txt"))) {
                    LocalDateTime offsetDateTime = Instant.now().atOffset(ZoneOffset.ofHours(8)).toLocalDateTime();
                    fileWriter.write("你好世界啊，" + offsetDateTime + "\n");
                }
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
                    if (!clientHandler.isOpen()) {
                        CLIENT_HANDLER_HASH_SET.remove(clientHandler);
                        return;
                    }
                    try {
                        clientHandler.send(gson.toJson(poll));
                    } catch (Exception e) {
                        Tools.log_info("clientHandler.send error:" + Log.getStackTraceString(e));
                    }
                });
            }
        }).start();
        if (GoWebsocket.serverSocket == null) {
            int port = 8118;
            for (int i = 0; i < 100; i++) {
                // 循环 100 次，允许最多100个实例
                try {
                    if (!Tools.isPortAvailable(port)) {
                        port += 1;
                        continue;
                    }
                    goGoMessage.put("port", port);
                    GoWebsocket.serverSocket = new WsServer(new InetSocketAddress(port));
//                    server.setMaxPayloadSize(8 * 1024 * 1024); // 8MB
                    GoWebsocket.serverSocket.start();
                    Tools.log_info("WebSocket服务器已启动[" + port + "]，等待连接...");
                    return;
                } catch (Exception e) {
                    Tools.log_info("绑定端口error:" + Log.getStackTraceString(e));
                }
            }
            Tools.log_info("没有端口可用！！！！！！！！");
        }
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


    static class WsServer extends WebSocketServer {
        public WsServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            Tools.log_info("客户端已连接: " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            CLIENT_HANDLER_HASH_SET.remove(conn);
            Tools.log_info("客户端已断开: " + code + " " + reason);
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            try {
                String dispatcher = goGo.dispatcher(message, conn);
                conn.send(dispatcher);
            } catch (Exception e) {
                Tools.log_info("处理消息异常：" + Log.getStackTraceString(e));
                conn.close(CloseFrame.UNEXPECTED_CONDITION, "server error");
            }
        }

        @Override
        public void onMessage(WebSocket conn, ByteBuffer message) {
            try {
                this.onMessage(conn, new String(message.array(), StandardCharsets.UTF_8));
            } catch (Exception e) {
                Tools.log_info("处理消息异常：" + Log.getStackTraceString(e));
                conn.close(CloseFrame.UNEXPECTED_CONDITION, "server error");
            }
            super.onMessage(conn, message);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            Tools.log_info("WebSocket异常：" + Log.getStackTraceString(ex));
        }

        @Override
        public void onStart() {
            Tools.log_info("WebSocket服务启动完成");
        }
    }


    private String dispatcher(String script, WebSocket clientHandler) {
        try {
            JsonObject fromJson = gson.fromJson(script, JsonObject.class);
            String type = fromJson.get("type").getAsString();

            // 保存 data
            JsonElement requestData = fromJson.get("data");
            fromJson.remove("data");

            // 处理之
            if (GOGO_TYPE_HANDLE_MAP.containsKey(type)) {
                Objects.requireNonNull(GOGO_TYPE_HANDLE_MAP.get(type)).handle(requestData, fromJson);
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
