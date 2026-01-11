package ms.handle;

import com.ny.gson.JsonElement;
import com.ny.gson.JsonObject;

public class HEcho implements GoHandle {
    @Override
    public void handle(JsonElement requestData, JsonObject responseData) {
        responseData.add("data", requestData);
        responseData.addProperty("msg", "原样返回");
    }
}
