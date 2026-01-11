package ms.qd;

import android.util.Log;

import com.qidian.common.lib.QDConfig;

public class MyQDConfig {
    public static void main() {
        try {
            QDConfig.getInstance().SetSetting("imei_release", "863846040618003");
            QDConfig.getInstance().SetSetting("imei2_release", "863846040618003");
            // 71f325ba k80pro
            QDConfig.getInstance().SetSetting("SIMSERIAL_release", "804b8f7d");
            Tools.log_info("设置IMEI和SN成功");
        } catch (Throwable e) {
            Tools.log_info("设置IMEI和SN失败: " + Log.getStackTraceString(e));
        }
    }
}
