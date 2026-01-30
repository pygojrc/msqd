package ms;

import static ms.GoGo.goGoMessage;

import com.ny.gson.JsonElement;
import com.ny.gson.JsonObject;
import com.ny.gson.JsonParser;

public class HInfo implements GoGoHandle{
    @Override
    public void handle(JsonElement requestData, JsonObject responseData) throws Exception {
        responseData.add("data", JsonParser.parseString(gson.toJson(goGoMessage)));
    }
}
