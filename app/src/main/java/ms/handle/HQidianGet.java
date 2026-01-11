package ms.handle;

import static ms.qd.Tools.qidian_http_resp_handle;

import com.ny.gson.JsonElement;
import com.ny.gson.JsonObject;
import com.qidian.QDReader.framework.network.qd.QDHttpResp;

import ms.qd.Tools;

public class HQidianGet implements GoHandle {
    @Override
    public void handle(JsonElement requestData, JsonObject responseData) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        JsonObject reqData = requestData.getAsJsonObject();
        // 请求 url, url 中包含了参数(?及后面的内容)
        String url = reqData.get("url").getAsString();
        // 440 修改为调用工具类
        QDHttpResp qdHttpResp = Tools.qdGet(url);
        // 处理响应
        qidian_http_resp_handle(responseData, qdHttpResp);
    }
}
