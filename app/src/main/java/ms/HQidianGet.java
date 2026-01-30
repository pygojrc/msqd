package ms;

import static ms.Tools.getQidianSearch;
import static ms.Tools.qidian_http_resp_handle;

import com.ny.gson.JsonElement;
import com.ny.gson.JsonObject;
import com.qidian.QDReader.framework.network.qd.QDHttpResp;

import r8.judian;
import v8.a;

public class HQidianGet implements GoGoHandle{
    @Override
    public void handle(JsonElement requestData, JsonObject responseData) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        JsonObject reqData = requestData.getAsJsonObject();
        // 请求 url, url 中包含了参数(?及后面的内容)
        String url = reqData.get("url").getAsString();
        // 请求工具
        a httpGetClient = new a(getQidianSearch(), new judian());
        // 发出请求
        QDHttpResp cihai = httpGetClient.cihai(null, url, 0);
        // 处理响应
        qidian_http_resp_handle(responseData, cihai);
    }
}
