#!/usr/bin/zsh
export JAVA_HOME=/home/ms/.local/share/JetBrains/Toolbox/apps/android-studio/jbr

echo "停止APP" && \
adb shell am force-stop com.qidian.QDReader && \

echo "开始编译安装apk" && \
cd /data/projects/AndroidStudioProjects/msqd && \
./gradlew installDebug && \

echo "安装插件" && \
#adb shell su -c "cp $(adb shell pm path ms.qd | grep base.apk | cut -d: -f2) /data/app/BaiduIME/com.qidian.QDReader/" && \
adb shell "cp $(adb shell pm path ms.qd | grep base.apk | cut -d: -f2) /sdcard/Android/data/com.qidian.QDReader/files/_app.apk" && \

echo "安装插件完成，正在启动apk..." && \
adb shell monkey -p com.qidian.QDReader -c android.intent.category.LAUNCHER 1 && \

echo "完成!!!" && \
adb logcat -s "gumjs-so" "MYAPP"