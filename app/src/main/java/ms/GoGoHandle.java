package ms;

import com.ny.gson.Gson;
import com.ny.gson.JsonElement;
import com.ny.gson.JsonObject;

public interface GoGoHandle {

    Gson gson = new Gson();

    void handle(JsonElement requestData, JsonObject responseData) throws Exception;
}
