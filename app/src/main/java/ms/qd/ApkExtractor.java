package ms.qd;

import android.content.Context;
import android.os.Build;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ApkExtractor {

    // 只示例解压 prefix 下的文件：如 "assets/" 或 "lib/arm64-v8a/"
    public static void extractIfChanged(
            Context ctx,
            File apkFile,
            File outDir,
            String prefix
    ) throws IOException {

        String apkMd5 = HashUtils.md5File(apkFile);

        // 用 apk 路径当 key（也可以换成包名/版本号）
        String keySuffix = apkFile.getAbsolutePath();
        String lastMd5 = Md5Store.load(ctx, keySuffix);

        File done = new File(outDir, ".done");
        String doneMd5 = readSmallText(done);

        boolean alreadyOk = apkMd5.equals(lastMd5) && apkMd5.equals(doneMd5) && outDir.exists();

        if (alreadyOk) {
            // MD5 一样 + done 标记一致：不重复解压
            return;
        }

        // 不一致：先清理旧目录（避免脏文件）
        deleteRecursively(outDir);
        outDir.mkdirs();

        unzipPrefix(apkFile, outDir, prefix);

        // 写入 done：先写临时文件，再原子替换
        File tmp = new File(outDir, ".done.tmp");
        writeSmallText(tmp, apkMd5);
        if (done.exists()) done.delete();
        // renameTo 在同一目录通常是原子的（Linux/Android 上）
        if (!tmp.renameTo(done)) {
            // 兜底：复制
            writeSmallText(done, apkMd5);
            tmp.delete();
        }

        Md5Store.save(ctx, keySuffix, apkMd5);
    }

    private static void unzipPrefix(File apkFile, File outDir, String prefix) throws IOException {
        byte[] buffer = new byte[8192];

        try (ZipFile zip = new ZipFile(apkFile)) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                String name = e.getName();
                if (!name.startsWith(prefix)) continue;

                String rel = name.substring(prefix.length());
                if (rel.isEmpty()) continue;

                File out = new File(outDir, rel);

                if (e.isDirectory()) {
                    out.mkdirs();
                    continue;
                }

                File parent = out.getParentFile();
                if (parent != null) parent.mkdirs();

                try (InputStream is = zip.getInputStream(e);
                     OutputStream os = new BufferedOutputStream(new FileOutputStream(out))) {
                    int n;
                    while ((n = is.read(buffer)) > 0) {
                        os.write(buffer, 0, n);
                    }
                }
            }
        }
    }

    private static String readSmallText(File f) {
        if (f == null || !f.exists()) return null;
        try (InputStream is = new FileInputStream(f)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[256];
            int n;
            while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return bos.toString(StandardCharsets.UTF_8).trim();
            } else {
                return bos.toString().trim();
            }
        } catch (IOException ignore) {
            return null;
        }
    }

    private static void writeSmallText(File f, String s) throws IOException {
        try (OutputStream os = new FileOutputStream(f, false)) {
            os.write(s.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
    }

    private static void deleteRecursively(File f) {
        if (f == null || !f.exists()) return;
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (File c : children) deleteRecursively(c);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        f.delete();
    }
}
