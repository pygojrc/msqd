package ms.handle;

import static ms.qd.Go.goGoMessage;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.ny.gson.JsonElement;
import com.ny.gson.JsonObject;
import com.ny.gson.JsonParser;
import com.ola.star.uin.U;
import com.qidian.common.lib.QDConfig;

import java.lang.reflect.Field;
import java.util.Objects;

import ms.qd.Tools;

public class HInfo implements GoHandle {
    @Override
    public void handle(JsonElement requestData, JsonObject responseData) throws Exception {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                goGoMessage.put("ProcessName", Process.myProcessName());
            }

            Field search = QDConfig.class.getDeclaredField("search");
            search.setAccessible(true);
            Object o = search.get(QDConfig.getInstance());
            goGoMessage.put("QDConfig", o);

            // BU#MODEL
            // 包含 TM#G_SIM_SE_NUM is Really Call System API
            // 443 com.qidian.QDReader.qmethod.pandoraex.monitor.d
            JsonObject jsonObject = new JsonObject();
            goGoMessage.put("环境信息", jsonObject);
            Context context = Objects.requireNonNull(Tools.getApplication()).getApplicationContext();
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            jsonObject.addProperty("TM#G_MID", com.qidian.QDReader.qmethod.pandoraex.monitor.d.a(telephonyManager));
            jsonObject.addProperty("BU#MODEL", com.qidian.QDReader.qmethod.pandoraex.monitor.d.b());
            jsonObject.addProperty("TM#G_NWK_OP", com.qidian.QDReader.qmethod.pandoraex.monitor.d.c(telephonyManager));
            jsonObject.addProperty("BU#SERByField", com.qidian.QDReader.qmethod.pandoraex.monitor.d.d());
            jsonObject.addProperty("BU#SERByMethod", com.qidian.QDReader.qmethod.pandoraex.monitor.d.e());
            jsonObject.addProperty("TM#G_SIM_OP", com.qidian.QDReader.qmethod.pandoraex.monitor.d.f(telephonyManager));
            jsonObject.addProperty("TM#G_SIM_SE_NUM", com.qidian.QDReader.qmethod.pandoraex.monitor.d.g(telephonyManager));

            // ContentResolver
            ContentResolver contentResolver = context.getContentResolver();
            JsonObject hJson = new JsonObject();
            jsonObject.add("SE#G_AID", hJson);
            hJson.addProperty("enabled_accessibility_services", com.qidian.QDReader.qmethod.pandoraex.monitor.d.h(contentResolver, "enabled_accessibility_services"));
            hJson.addProperty("android_id", com.qidian.QDReader.qmethod.pandoraex.monitor.d.h(contentResolver, "android_id"));

            jsonObject.addProperty("TM#G_SID", com.qidian.QDReader.qmethod.pandoraex.monitor.d.k(telephonyManager));
            jsonObject.addProperty("TM#G_DID_imei_release", com.qidian.QDReader.qmethod.pandoraex.monitor.d.judian(telephonyManager));
            jsonObject.addProperty("TM#G_DID#I_1_imei2_release", com.qidian.QDReader.qmethod.pandoraex.monitor.d.cihai(telephonyManager, 1));

            // 带 so load fail,error info :%s
            // 443 com.ola.star.uin.U
            JsonObject uJson = new JsonObject();
            goGoMessage.put("设备信息", uJson);
            uJson.addProperty("a", U.a());
            uJson.addProperty("e", U.e());

            uJson.addProperty("oo", U.d());
            uJson.addProperty("oz", U.a(context));
            uJson.addProperty("kernel", U.c());
            uJson.addProperty("ro.product.board", U.a("ro.product.board"));
            uJson.addProperty("ro.product.device", U.a("ro.product.device"));
            uJson.addProperty("ro.product.first_api_level", U.a("ro.product.first_api_level"));
            uJson.addProperty("ro.product.manufacturer", U.a("ro.product.manufacturer"));
            uJson.addProperty("ro.product.name", U.a("ro.product.name"));
            uJson.addProperty("ro.build.host", U.a("ro.build.host"));
            uJson.addProperty("sys.tencent.model", U.a("sys.tencent.model"));

        } catch (Throwable e) {
            goGoMessage.put("Exception", Log.getStackTraceString(e));
        }
        responseData.add("data", JsonParser.parseString(gson.toJson(goGoMessage)));
    }
}
