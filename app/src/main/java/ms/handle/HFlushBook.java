package ms.handle;

import com.ny.gson.JsonElement;
import com.ny.gson.JsonObject;

public class HFlushBook implements GoHandle {
    @Override
    public void handle(JsonElement requestData, JsonObject responseData) {
        JsonObject asJsonObject = requestData.getAsJsonObject();
        long bookId = asJsonObject.get("bookId").getAsLong();

        boolean notInBookshelf = false;
        if (asJsonObject.has("notInBookshelf")) {
            notInBookshelf = asJsonObject.get("notInBookshelf").getAsBoolean();
        }
        // 440 com.qidian.QDReader.component.bll.manager.v1  s  包含 mergeMemoryData 字符串的类
        com.qidian.QDReader.component.bll.manager.v1 t1Obj = com.qidian.QDReader.component.bll.manager.v1.s(bookId, notInBookshelf);
        // 440 J0
        t1Obj.J0(notInBookshelf);
        String json = gson.toJson(t1Obj);
        responseData.add("data", gson.toJsonTree(json));
    }
}
