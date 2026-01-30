package ms;

import android.content.ContentValues;

import com.ny.gson.JsonElement;
import com.ny.gson.JsonObject;
import com.qidian.QDReader.framework.network.qd.QDHttpResp;

import v8.b;

public class HQidianPost implements GoGoHandle {
    @Override
    public void handle(JsonElement requestData, JsonObject responseData) throws Exception {
        JsonObject reqData = requestData.getAsJsonObject();
        //  请求 url
        String url = reqData.get("url").getAsString();
        // 参数
        ContentValues contentValues = new ContentValues();
        JsonObject params = reqData.getAsJsonObject("params");
        params.keySet().forEach(key -> {
            JsonElement jsonElement = params.get(key);
            if (jsonElement == null || jsonElement.isJsonNull()) {
                contentValues.put(key, "");
            } else {
                contentValues.put(key, jsonElement.getAsString());
            }
        });
        // 请求工具
        b httpPostClient = new b(Tools.getQidianSearch(), null);
        // 发出请求
        QDHttpResp cihai = httpPostClient.cihai(url, contentValues);
        // 处理响应
        Tools.qidian_http_resp_handle(responseData, cihai);
    }
}
