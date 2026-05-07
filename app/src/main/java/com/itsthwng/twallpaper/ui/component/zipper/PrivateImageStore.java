package com.itsthwng.twallpaper.ui.component.zipper;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.graphics.BitmapFactory;

import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntConsumer;

public final class PrivateImageStore {

    private static final OkHttpClient client = new OkHttpClient();

    private PrivateImageStore() {}

    // ======= StorageGuard (shared) =======
    private static final long SAFETY_BUFFER_BYTES = 50L * 1024 * 1024;   // luôn chừa ~50MB
    private static final long DEFAULT_EST_IMAGE_BYTES = 3L * 1024 * 1024;// fallback 3MB/ảnh
    private static final long SPACE_CHECK_INTERVAL_BYTES = 512L * 1024;  // check mỗi ~512KB

    private enum EvictMode { SETS, FILES }               // dọn theo Bộ (id) hay theo File lẻ
    private static final int EVICT_BATCH = 1;            // mỗi lần thiếu chỗ -> xóa 1 BỘ (hoặc 6 file)
    private static final int EVICT_FILES_COUNT = 6;      // nếu dùng FILES mode

    private static long headContentLength(String url) {
        try {
            Request req = new Request.Builder().url(url).head().build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful()) return -1L;
                String cl = resp.header("Content-Length");
                return cl == null ? -1L : Long.parseLong(cl);
            }
        } catch (Exception ignore) { return -1L; }
    }

    private static boolean isImageFile(File f) {
        String n = f.getName().toLowerCase(Locale.ROOT);
        return n.endsWith(".jpg")||n.endsWith(".jpeg")||n.endsWith(".png")
                ||n.endsWith(".webp")||n.endsWith(".gif")||n.endsWith(".heic")||n.endsWith(".heif");
    }

    private static Integer extractSetIdFromName(String name) {
        try {
            java.util.regex.Matcher m1 = java.util.regex.Pattern.compile("^chain_(\\d+)_.*$").matcher(name);
            if (m1.matches()) return Integer.parseInt(m1.group(1));
            java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("^[^_]+_(\\d+)_.*$").matcher(name);
            if (m2.matches()) return Integer.parseInt(m2.group(1));
        } catch (Exception ignore) {}
        return null;
    }

    private static int evictOldestFiles(Context ctx, int maxDelete) {
        File base = ctx.getFilesDir();
        File[] files = base.listFiles(f -> f.isFile() && isImageFile(f));
        if (files == null) return 0;
        java.util.Arrays.sort(files, java.util.Comparator.comparingLong(File::lastModified)); // cũ->mới
        int del = 0;
        for (File f : files) { if (del >= maxDelete) break; if (f.delete()) del++; }
        return del;
    }

    private static int evictOldestSets(Context ctx, int setsToRemove, Integer excludeSetId) {
        File base = ctx.getFilesDir();
        File[] files = base.listFiles(f -> f.isFile() && isImageFile(f));
        if (files == null) return 0;
        java.util.Map<Integer, java.util.List<File>> bySet = new java.util.HashMap<>();
        for (File f : files) {
            Integer id = extractSetIdFromName(f.getName());
            if (id != null) bySet.computeIfAbsent(id, k -> new java.util.ArrayList<>()).add(f);
        }
        java.util.List<java.util.Map.Entry<Integer, java.util.List<File>>> groups =
                new java.util.ArrayList<>(bySet.entrySet());
        // mới->cũ (so theo max lastModified trong nhóm)
        groups.sort((a, b) -> {
            long ta = a.getValue().stream().mapToLong(File::lastModified).max().orElse(0L);
            long tb = b.getValue().stream().mapToLong(File::lastModified).max().orElse(0L);
            return Long.compare(tb, ta);
        });
        int removed = 0;
        for (int i = groups.size()-1; i>=0 && removed<setsToRemove; i--) {
            Integer setId = groups.get(i).getKey();
            if (excludeSetId != null && excludeSetId.equals(setId)) continue;
            for (File f : groups.get(i).getValue()) f.delete();
            removed++;
        }
        return removed;
    }

    private static boolean ensureSpace(Context ctx, long bytesNeeded,
                                       long safetyBuffer, EvictMode mode, int batch,
                                       Integer excludeSetId) {
        File base = ctx.getFilesDir();
        long spare = base.getUsableSpace() - safetyBuffer;
        if (spare >= bytesNeeded) return true;
        if (mode == EvictMode.SETS) evictOldestSets(ctx, batch, excludeSetId);
        else                        evictOldestFiles(ctx, EVICT_FILES_COUNT);
        spare = base.getUsableSpace() - safetyBuffer;
        return spare >= bytesNeeded;
    }

    private static boolean ensureSpaceDuringWrite(Context ctx, long safetyBuffer,
                                                  EvictMode mode, int batch,
                                                  Integer excludeSetId, long remainEstBytes) {
        File base = ctx.getFilesDir();
        long spare = base.getUsableSpace() - safetyBuffer;
        if (spare >= Math.max(remainEstBytes, 0)) return true;
        if (mode == EvictMode.SETS) evictOldestSets(ctx, batch, excludeSetId);
        else                        evictOldestFiles(ctx, EVICT_FILES_COUNT);
        spare = base.getUsableSpace() - safetyBuffer;
        return spare >= Math.max(remainEstBytes, 0);
    }

    /** Copy + progress + guard mỗi ~512KB (dùng CHUNG) */
    private static void copyWithProgressAndGuard(InputStream in, OutputStream out, long total,
                                                 DownloadInternalCallback cb,
                                                 Context ctx, Integer excludeSetId,
                                                 EvictMode mode, int batch) throws Exception {
        byte[] buf = new byte[8 * 1024];
        long written = 0, nextCheckAt = SPACE_CHECK_INTERVAL_BYTES;
        int lastPercent = -1, r;
        while ((r = in.read(buf)) != -1) {
            out.write(buf, 0, r);
            written += r;

            if (total > 0 && cb != null) {
                int pct = (int)(written * 100 / total);
                if (pct != lastPercent) { lastPercent = pct; cb.onProgress(pct); }
            }
            if (written >= nextCheckAt) {
                long remain = (total > 0) ? Math.max(total - written, 0) : DEFAULT_EST_IMAGE_BYTES;
                boolean ok = ensureSpaceDuringWrite(ctx, SAFETY_BUFFER_BYTES, mode, batch, excludeSetId, remain);
                if (!ok) throw new IllegalStateException("Không đủ dung lượng (đã thử dọn).");
                nextCheckAt = written + SPACE_CHECK_INTERVAL_BYTES;
            }
        }
        out.flush();
        if (total <= 0 && cb != null) cb.onProgress(100);
    }

    /** Tải 1 file với guard (dùng CHUNG cho single/chain) – ghi .part rồi rename */
    private static String guardedDownloadToFile(Context ctx, String url, File target,
                                                DownloadInternalCallback cb,
                                                Integer excludeSetId,
                                                EvictMode mode, int batch) throws Exception {
        if (target.exists()) { if (cb!=null) cb.onProgress(100); return target.getAbsolutePath(); }

        long est = headContentLength(url);
        long need = (est > 0 ? est : DEFAULT_EST_IMAGE_BYTES);
        if (!ensureSpace(ctx, need, SAFETY_BUFFER_BYTES, mode, batch, excludeSetId))
            throw new IllegalStateException("Không đủ dung lượng (đã thử dọn).");

        File temp = new File(target.getParentFile(), target.getName() + ".part");

        Request req = new Request.Builder().url(url).build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new IllegalStateException("HTTP " + resp.code());
            long total = resp.body()!=null ? resp.body().contentLength() : -1L;
            InputStream in = resp.body()!=null ? resp.body().byteStream() : null;
            if (in == null) throw new IllegalStateException("Empty body");
            try (OutputStream out = new FileOutputStream(temp)) {
                copyWithProgressAndGuard(in, out, total, cb, ctx, excludeSetId, mode, batch);
            }
        } catch (Exception e) { if (temp.exists()) temp.delete(); throw e; }

        if (target.exists()) target.delete();
        if (!temp.renameTo(target)) {
            try (InputStream in2 = new java.io.FileInputStream(temp);
                 OutputStream out2 = new FileOutputStream(target)) {
                byte[] b = new byte[16*1024]; int n; while ((n=in2.read(b))!=-1) out2.write(b,0,n);
            }
            temp.delete();
        }
        return target.getAbsolutePath();
    }


    /* ======================= CHECK & INFO (FILES DIR) ======================= */

    /** Lấy đối tượng File trong filesDir từ fileName */
    public static File getFileInFiles(Context context, String fileName) {
        return new File(context.getFilesDir(), fileName);
    }

    /** Đã tải (tồn tại) trong filesDir chưa? */
    public static boolean isDownloaded(Context context, String fileName) {
        return getFileInFiles(context, fileName).exists();
    }

    /** Lấy thông tin ảnh theo fileName (filesDir) */
    public static ImageInfo getImageInfo(Context context, String fileName) {
        File f = getFileInFiles(context, fileName);
        return buildImageInfoFromFile(f, fileName);
    }

    public static boolean isFileDownloadedById(Context context, int id, String type, String url) {
        FileNameUtils utils = new FileNameUtils();
        String fileName = utils.generateFileNameById(id, type, url);

        return isDownloaded(context, fileName);
    }


    /**
     * Kiểm tra file chain đã tồn tại theo ID và loại (left/right)
     */
    public static boolean isChainFileDownloadedById(Context context, int id, String type, String url, boolean isLeft) {
        FileNameUtils utils = new FileNameUtils();
        String fileName = utils.generateFileNameByIdChains(id, type, url, isLeft);
        return isDownloaded(context, fileName);
    }

    public static void downloadToInternalFilesAsyncGuardedBySets(
            Context context, String imageUrl, String fileName, DownloadInternalCallback callback
    ) {
        new Thread(() -> {
            File target = new File(context.getFilesDir(), fileName);
            Integer setId = extractSetIdFromName(fileName); // để không xóa nhầm bộ đang tải
            try {
                String path = guardedDownloadToFile(
                        context, imageUrl, target, callback, setId, EvictMode.SETS, EVICT_BATCH
                );
                if (callback != null) callback.onSuccess(path);
            } catch (Exception e) {
                if (callback != null) callback.onFailed(e.getMessage());
            }
        }).start();
    }

    /** Tải ảnh về filesDir (private). Gọi callback trên luồng hiện tại (đang chạy trong Thread riêng). */
    public static void downloadToInternalFilesAsync(Context context,
                                                    String imageUrl,
                                                    String fileName,
                                                    DownloadInternalCallback callback) {
        new Thread(() -> {
            try {
                File target = new File(context.getFilesDir(), fileName);
                Request req = new Request.Builder().url(imageUrl).build();
                try (Response resp = client.newCall(req).execute()) {
                    if (!resp.isSuccessful()) throw new IllegalStateException("HTTP " + resp.code());
                    long total = resp.body() != null ? resp.body().contentLength() : -1L;
                    InputStream in = resp.body() != null ? resp.body().byteStream() : null;
                    if (in == null) throw new IllegalStateException("Empty body");

                    try (OutputStream out = new FileOutputStream(target)) {
                        copyWithProgress(in, out, total, callback);
                    }
                }
                if (callback != null) callback.onSuccess(target.getAbsolutePath());
            } catch (Exception e) {
                if (callback != null) callback.onFailed(e.getMessage());
            }
        }).start();
    }


    public static void downloadChainImagesInternalAsyncGuardedBySets(
            Context context, int id, String leftUrl, String rightUrl, String type, ChainDownloadCallback callback
    ) {
        if ((leftUrl==null||leftUrl.isEmpty()) && (rightUrl==null||rightUrl.isEmpty())) {
            if (callback!=null) callback.onFailed("Không có URL nào để download"); return;
        }
        FileNameUtils utils = new FileNameUtils();
        final int total = (leftUrl==null||leftUrl.isEmpty()?0:1) + (rightUrl==null||rightUrl.isEmpty()?0:1);
        if (callback!=null) callback.onProgress(0);

        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(2);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(total);
        java.util.List<String> errors = java.util.Collections.synchronizedList(new java.util.ArrayList<>());

        java.util.concurrent.atomic.AtomicReference<String> leftPath  = new java.util.concurrent.atomic.AtomicReference<>(null);
        java.util.concurrent.atomic.AtomicReference<String> rightPath = new java.util.concurrent.atomic.AtomicReference<>(null);
        java.util.concurrent.atomic.AtomicInteger leftProg  = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger rightProg = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger overallLast = new java.util.concurrent.atomic.AtomicInteger(-1);

        Runnable report = () -> {
            int parts=0,sum=0;
            if (leftUrl!=null && !leftUrl.isEmpty())  { parts++; sum+=leftProg.get(); }
            if (rightUrl!=null&& !rightUrl.isEmpty()) { parts++; sum+=rightProg.get(); }
            int overall = parts==0?100:(sum/parts);
            int prev = overallLast.getAndSet(overall);
            if (callback!=null && overall!=prev) callback.onProgress(overall);
        };

        // LEFT
        if (leftUrl!=null && !leftUrl.isEmpty()) {
            pool.execute(() -> {
                try {
                    String fn = utils.generateFileNameByIdChains(id, type, leftUrl, true);
                    File out = new File(context.getFilesDir(), fn);
                    String path = guardedDownloadToFile(
                            context, leftUrl, out,
                            new DownloadInternalCallback() {
                                @Override public void onProgress(int p){ leftProg.set(p); report.run(); }
                                @Override public void onSuccess(String fp){ /* not used here */ }
                                @Override public void onFailed(String err){ /* caught outside */ }
                            },
                            id, EvictMode.SETS, EVICT_BATCH
                    );
                    leftPath.set(path); leftProg.set(100); report.run();
                } catch (Exception e) {
                    errors.add("Left failed: "+e.getMessage());
                    leftProg.set(100); report.run();
                } finally { latch.countDown(); }
            });
        }

        // RIGHT
        if (rightUrl!=null && !rightUrl.isEmpty()) {
            pool.execute(() -> {
                try {
                    String fn = utils.generateFileNameByIdChains(id, type, rightUrl, false);
                    File out = new File(context.getFilesDir(), fn);
                    String path = guardedDownloadToFile(
                            context, rightUrl, out,
                            new DownloadInternalCallback() {
                                @Override public void onProgress(int p){ rightProg.set(p); report.run(); }
                                @Override public void onSuccess(String fp){ /* not used here */ }
                                @Override public void onFailed(String err){ /* caught outside */ }
                            },
                            id, EvictMode.SETS, EVICT_BATCH
                    );
                    rightPath.set(path); rightProg.set(100); report.run();
                } catch (Exception e) {
                    errors.add("Right failed: "+e.getMessage());
                    rightProg.set(100); report.run();
                } finally { latch.countDown(); }
            });
        }

        pool.execute(() -> {
            try {
                latch.await(); pool.shutdown();
                boolean leftOk  = (leftUrl==null||leftUrl.isEmpty())  || (leftPath.get()!=null && new File(leftPath.get()).exists());
                boolean rightOk = (rightUrl==null||rightUrl.isEmpty()) || (rightPath.get()!=null && new File(rightPath.get()).exists());
                if (leftOk && rightOk) {
                    if (callback!=null) callback.onSuccess(leftPath.get(), rightPath.get());
                } else if (leftOk || rightOk) {
                    if (callback!=null) callback.onPartialSuccess(leftPath.get(), rightPath.get(), new java.util.ArrayList<>(errors));
                } else {
                    if (callback!=null) callback.onFailed(errors.isEmpty() ? "Tất cả downloads đều thất bại" : String.join(", ", errors));
                }
            } catch (InterruptedException e) {
                if (callback!=null) callback.onFailed("Interrupted: "+e.getMessage());
            }
        });
    }

    /**
     * Tải 2 ảnh chain (left & right) song song vào Internal Storage: filesDir/<type>/...
     * - Nếu file đã tồn tại: bỏ qua tải, coi như 100% cho ảnh đó
     * - Progress tổng = trung bình progress của các ảnh có URL
     * - Kết quả: onSuccess nếu cả 2 OK, onPartialSuccess nếu 1 OK 1 lỗi, onFailed nếu cả 2 lỗi
     */
    public static void downloadChainImagesInternalAsync(
            Context context,
            int id,
            String leftUrl,
            String rightUrl,
            String type,
            ChainDownloadCallback callback
    ) {
        FileNameUtils utils = new FileNameUtils();
        if ((leftUrl == null || leftUrl.isEmpty()) && (rightUrl == null || rightUrl.isEmpty())) {
            if (callback != null) callback.onFailed("Không có URL nào để download");
            return;
        }

        final int totalDownloads =
                (leftUrl == null || leftUrl.isEmpty() ? 0 : 1) +
                        (rightUrl == null || rightUrl.isEmpty() ? 0 : 1);

        if (callback != null) callback.onProgress(0);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(totalDownloads == 0 ? 1 : totalDownloads);
        List<String> errors = Collections.synchronizedList(new ArrayList<>());

        // kết quả đường dẫn
        AtomicReference<String> leftPath = new AtomicReference<>(null);
        AtomicReference<String> rightPath = new AtomicReference<>(null);

        // progress từng ảnh
        AtomicInteger leftProgress = new AtomicInteger(0);
        AtomicInteger rightProgress = new AtomicInteger(0);
        AtomicInteger overallLast = new AtomicInteger(-1);

        // hàm gộp progress
        Runnable reportProgress = () -> {
            int sum = 0;
            int parts = 0;
            if (leftUrl != null && !leftUrl.isEmpty()) {
                sum += leftProgress.get();
                parts++;
            }
            if (rightUrl != null && !rightUrl.isEmpty()) {
                sum += rightProgress.get();
                parts++;
            }
            int overall = parts == 0 ? 100 : (sum / parts);
            int prev = overallLast.getAndSet(overall);
            if (callback != null && overall != prev) {
                callback.onProgress(overall);
            }
        };

        // LEFT
        if (leftUrl != null && !leftUrl.isEmpty()) {
            pool.execute(() -> {
                try {
                    String leftFileName = utils.generateFileNameByIdChains(id, type, leftUrl, true);
                    File out = new File(context.getFilesDir(), leftFileName);

                    if (out.exists()) {
                        leftPath.set(out.getAbsolutePath());
                        leftProgress.set(100);
                        reportProgress.run();
                    } else {
                        downloadToFile(context, leftUrl, out, p -> {
                            leftProgress.set(p);
                            reportProgress.run();
                        });
                        leftPath.set(out.getAbsolutePath());
                        leftProgress.set(100);
                        reportProgress.run();
                    }
                } catch (Exception e) {
                    errors.add("Left failed: " + e.getMessage());
                    leftProgress.set(100); // coi như hoàn tất nhánh này để tổng tiến độ không kẹt
                    reportProgress.run();
                } finally {
                    latch.countDown();
                }
            });
        }

        // RIGHT
        if (rightUrl != null && !rightUrl.isEmpty()) {
            pool.execute(() -> {
                try {
                    String rightFileName = utils.generateFileNameByIdChains(id, type, rightUrl, false);
                    File out = new File(context.getFilesDir(), rightFileName);

                    if (out.exists()) {
                        rightPath.set(out.getAbsolutePath());
                        rightProgress.set(100);
                        reportProgress.run();
                    } else {
                        downloadToFile(context, rightUrl, out, p -> {
                            rightProgress.set(p);
                            reportProgress.run();
                        });
                        rightPath.set(out.getAbsolutePath());
                        rightProgress.set(100);
                        reportProgress.run();
                    }
                } catch (Exception e) {
                    errors.add("Right failed: " + e.getMessage());
                    rightProgress.set(100);
                    reportProgress.run();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Nếu chỉ có 1 URL: tránh latch = 0 gây deadpath
        if (totalDownloads == 0) {
            if (callback != null) callback.onFailed("Không có tác vụ nào để chạy");
            pool.shutdown();
            return;
        }

        // chờ hoàn tất rồi trả callback tổng
        pool.execute(() -> {
            try {
                latch.await();
                pool.shutdown();

                boolean leftOk  = (leftUrl == null || leftUrl.isEmpty()) || (leftPath.get()  != null && new File(leftPath.get()).exists());
                boolean rightOk = (rightUrl == null || rightUrl.isEmpty()) || (rightPath.get() != null && new File(rightPath.get()).exists());

                if (leftOk && rightOk) {
                    if (callback != null) callback.onSuccess(leftPath.get(), rightPath.get());
                } else if (leftOk || rightOk) {
                    if (callback != null) callback.onPartialSuccess(leftPath.get(), rightPath.get(), new ArrayList<>(errors));
                } else {
                    if (callback != null) callback.onFailed(errors.isEmpty() ? "Tất cả downloads đều thất bại" : String.join(", ", errors));
                }
            } catch (InterruptedException e) {
                if (callback != null) callback.onFailed("Interrupted: " + e.getMessage());
            }
        });
    }

    private static void copyWithProgress(InputStream in, OutputStream out, long total, IntConsumer cb) throws Exception {
        byte[] buf = new byte[8 * 1024];
        long sum = 0;
        int last = -1;
        int r;
        while ((r = in.read(buf)) != -1) {
            out.write(buf, 0, r);
            sum += r;
            if (total > 0 && cb != null) {
                int p = (int) (sum * 100 / total);
                if (p != last) {
                    last = p;
                    cb.accept(p);
                }
            }
        }
        out.flush();
        if (total <= 0 && cb != null) cb.accept(100);
    }

    private static void downloadToFile(Context context, String url, File target, IntConsumer progressCb) throws Exception {
        Request req = new Request.Builder().url(url).build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new IllegalStateException("HTTP " + resp.code());
            long total = resp.body() != null ? resp.body().contentLength() : -1L;
            InputStream in = resp.body() != null ? resp.body().byteStream() : null;
            if (in == null) throw new IllegalStateException("Empty body");
            try (OutputStream out = new FileOutputStream(target)) {
                copyWithProgress(in, out, total, progressCb);
            }
        }
    }

    private static void copyWithProgress(InputStream in, OutputStream out, long total, DownloadInternalCallback cb) throws Exception {
        byte[] buf = new byte[8 * 1024];
        long sum = 0;
        int last = -1;
        int r;
        while ((r = in.read(buf)) != -1) {
            out.write(buf, 0, r);
            sum += r;
            if (total > 0 && cb != null) {
                int percent = (int) (sum * 100 / total);
                if (percent != last) {
                    last = percent;
                    cb.onProgress(percent);
                }
            }
        }
        out.flush();
        if (total <= 0 && cb != null) cb.onProgress(100);
    }

    /* ======================= CORE ======================= */

    private static ImageInfo buildImageInfoFromFile(File f, String fileName) {
        ImageInfo info = new ImageInfo();
        info.fileName = fileName;
        info.path = f.getAbsolutePath();
        info.exists = f.exists();

        if (!info.exists) {
            // chưa tải
            return info;
        }

        info.sizeBytes = f.length();
        info.lastModified = f.lastModified();

        // Lấy width/height + outMimeType nhanh (không decode bitmap thật)
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
        info.width = opts.outWidth;
        info.height = opts.outHeight;

        // MIME: ưu tiên từ decoder; nếu null thì đoán theo phần mở rộng
        info.mimeType = (opts.outMimeType != null) ? opts.outMimeType : guessMimeFromName(fileName);

        // Tuỳ chọn: checksum để tránh tải trùng (có thể bỏ nếu không cần)
        try {
            info.sha256 = sha256OfFile(f);
        } catch (Exception ignored) { }

        return info;
    }

    private static String guessMimeFromName(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".heic") || lower.endsWith(".heif")) return "image/heif";
        return "image/jpeg";
    }

    private static String sha256OfFile(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(file), md)) {
            byte[] buf = new byte[16 * 1024];
            while (dis.read(buf) != -1) { /* read to update digest */ }
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /* ======================= MODEL ======================= */

    /* ======================= MODEL ======================= */

    /** Thông tin ảnh nội bộ */
    public static class ImageInfo {
        public String fileName;
        public String path;
        public boolean exists;

        public long sizeBytes;
        public long lastModified;

        public int width;
        public int height;

        public String mimeType;   // vd: image/jpeg
        public String sha256;     // tuỳ chọn
    }
}

