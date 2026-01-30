package ms;

import com.ny.gson.JsonElement;
import com.ny.gson.JsonObject;

public class HEcho implements GoGoHandle {
    @Override
    public void handle(JsonElement requestData, JsonObject responseData) {
        responseData.add("data", requestData);
        responseData.addProperty("msg", "原样返回");
    }
}
