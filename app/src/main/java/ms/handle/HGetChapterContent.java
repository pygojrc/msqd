package ms.handle;

import com.ny.gson.JsonElement;
import com.ny.gson.JsonObject;
import com.qidian.QDReader.repository.entity.ChapterContentItem;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class HGetChapterContent implements GoHandle {
    @Override
    public void handle(JsonElement requestData, JsonObject responseData) throws InterruptedException {
        JsonObject asJsonObject = requestData.getAsJsonObject();
        long bookId = asJsonObject.get("bookId").getAsLong();
        long chapterId = asJsonObject.get("chapterId").getAsLong();
        // 来源，1 起点？
        int source = 1;
        if (asJsonObject.has("source")) {
            source = asJsonObject.get("source").getAsInt();
        }
        ChapterContentHandler chapterContentHandler = new ChapterContentHandler(bookId, chapterId);
        // 包含 下载vip章节内容接口失败 mQDBookId:
        // 440 com.qidian.QDReader.component.bll.v
        com.qidian.QDReader.component.bll.v sObj = new com.qidian.QDReader.component.bll.v(source, bookId, chapterId, false, false, "", chapterContentHandler);
        // 包含 downloadContent threadPool rejected: isTerminating
        // 440 F
        sObj.F();
        // 带 ReaderThreadPool 和 submit,无参，无字符串的方法
        // 440 O
        // sObj.O();
        StringBuilder errorMsg = new StringBuilder();
        for (int i = 0; i < 9999; i++) {
            // 从队列中获取结果
            Map<String, Object> poll = chapterContentHandler.qidianChapterContentSendMsgDataQueue.poll(5, TimeUnit.SECONDS);
            if (poll == null) {
                responseData.addProperty("status", 404);
                responseData.addProperty("data", "获取超时，未截取到内容: " + errorMsg);
                return;
            }
            String method = String.valueOf(poll.get("method"));
            if (method.equals("onPaging")) {
                // 成功
                responseData.addProperty("data", poll.get("data").toString());
                return;
            } else if (method.equals("onError")) {
                // 失败
                responseData.addProperty("status", 501);
                responseData.addProperty("msg", "获取异常");
                responseData.addProperty("data", gson.toJson(poll));
                return;
            } else if (method.equals("onBuy")) {
                // 失败, 需要购买
                responseData.addProperty("status", 301);
                responseData.addProperty("msg", "需要购买");
                responseData.addProperty("data", gson.toJson(poll));
                return;
            } else {
                // 逐个错误选项累加
                errorMsg.append(gson.toJson(poll));
            }
        }

    }

    // 440 调用它的作为参数的接口类型
    // com.qidian.QDReader.component.bll.callback.b
    static class ChapterContentHandler implements com.qidian.QDReader.component.bll.callback.b {
        private long bookId;
        private long chapterId;
        public LinkedBlockingQueue<Map<String, Object>> qidianChapterContentSendMsgDataQueue = new LinkedBlockingQueue<>();

        public ChapterContentHandler(long bookId, long chapterId) {
            this.bookId = bookId;
            this.chapterId = chapterId;
        }

        @Override
        public void onBuy(String str, long j10) {
            HashMap<String, Object> e = new HashMap<>();
            e.put("bookId", bookId);
            e.put("chapterId", chapterId);
            e.put("method", "onBuy");
            // 需要购买章节的信息
            e.put("data", str);
            e.put("arg2", j10);
            try {
                qidianChapterContentSendMsgDataQueue.put(e);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void onError(String str, int i10, long j10) {
            HashMap<String, Object> e = new HashMap<>();
            e.put("bookId", bookId);
            e.put("chapterId", chapterId);
            e.put("errorCode", i10);
            e.put("errorMsg", str);
            e.put("method", "onError");
            try {
                qidianChapterContentSendMsgDataQueue.put(e);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void onLoading(long j10) {
            HashMap<String, Object> e = new HashMap<>();
            e.put("bookId", bookId);
            e.put("chapterId", chapterId);
            e.put("method", "onLoading");
            e.put("arg1", j10);
            try {
                qidianChapterContentSendMsgDataQueue.put(e);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void onPaging(ChapterContentItem chapterContentItem, long j10) {
            HashMap<String, Object> e = new HashMap<>();
            e.put("bookId", bookId);
            e.put("chapterId", chapterId);
            e.put("method", "onPaging");
            e.put("data", chapterContentItem.getOriginalChapterContent());
            try {
                qidianChapterContentSendMsgDataQueue.put(e);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void onSuccess(boolean z10, long j10) {
            HashMap<String, Object> e = new HashMap<>();
            e.put("bookId", bookId);
            e.put("chapterId", chapterId);
            e.put("method", "onSuccess");
            e.put("arg1", z10);
            e.put("arg2", j10);
            try {
                qidianChapterContentSendMsgDataQueue.put(e);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
