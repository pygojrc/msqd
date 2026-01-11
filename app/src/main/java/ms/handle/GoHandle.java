package ms.handle;

import com.ny.gson.Gson;
import com.ny.gson.JsonElement;
import com.ny.gson.JsonObject;

public interface GoHandle {

    Gson gson = new Gson();

    void handle(JsonElement requestData, JsonObject responseData) throws Exception;
}
