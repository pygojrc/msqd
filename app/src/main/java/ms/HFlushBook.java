package ms;

import com.ny.gson.JsonElement;
import com.ny.gson.JsonObject;

public class HFlushBook implements GoGoHandle{
    @Override
    public void handle(JsonElement requestData, JsonObject responseData) {
        JsonObject asJsonObject = requestData.getAsJsonObject();
        long bookId = asJsonObject.get("bookId").getAsLong();

        boolean notInBookshelf = false;
        if (asJsonObject.has("notInBookshelf")) {
            notInBookshelf = asJsonObject.get("notInBookshelf").getAsBoolean();
        }
        // com.qidian.QDReader.component.bll.manager.t1.J to s1.I
        // 439 com.qidian.QDReader.component.bll.manager.C4705v1 m18004s
        com.qidian.QDReader.component.bll.manager.v1 t1Obj = com.qidian.QDReader.component.bll.manager.v1.s(bookId, notInBookshelf);
        // D0 to B0
        // * renamed from: J0 */
        t1Obj.J0(notInBookshelf);
        String json = gson.toJson(t1Obj);
        responseData.add("data", gson.toJsonTree(json));
    }
}
