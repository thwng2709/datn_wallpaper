package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Layers.Glszl_lockerLayer;

/**
 * Glszl_PreviewHelper - Helper class để tạo preview từ GameAdapter
 * Sử dụng Glszl_StaticRenderer để lấy Bitmap trực tiếp từ GameAdapter
 */
public class Glszl_PreviewHelper {
    private static final String TAG = "Glszl_PreviewHelper";
    
    /**
     * Tạo preview với kích thước mặc định (1080x1920)
     * @param context Context của ứng dụng
     * @return Bitmap đã render hoặc null nếu thất bại
     */
    public static Bitmap createDefaultPreview(Context context) {
        return createPreview(context, 1080, 1920);
    }
    
    /**
     * Tạo preview với kích thước tùy chỉnh
     * @param context Context của ứng dụng
     * @param width Chiều rộng
     * @param height Chiều cao
     * @return Bitmap đã render hoặc null nếu thất bại
     */
    public static Bitmap createPreview(Context context, int width, int height) {
        return createPreview(context, width, height, true);
    }
    
    /**
     * Tạo preview với kích thước tùy chỉnh và tùy chọn fallback
     * @param context Context của ứng dụng
     * @param width Chiều rộng
     * @param height Chiều cao
     * @param useFallback Có sử dụng fallback không
     * @return Bitmap đã render hoặc null nếu thất bại
     */
    public static Bitmap createPreview(Context context, int width, int height, boolean useFallback) {
        try {
            Log.d(TAG, "Tạo preview: " + width + "x" + height + " với fallback: " + useFallback);
            
            // Sử dụng Glszl_StaticRenderer để lấy Bitmap từ GameAdapter
            Bitmap preview = Glszl_StaticRenderer.CreatePreviewBitmap(context, width, height, useFallback);
            
            if (preview != null) {
                Log.d(TAG, "Tạo preview thành công: " + preview.getWidth() + "x" + preview.getHeight());
                return preview;
            } else {
                Log.w(TAG, "Không thể tạo preview từ GameAdapter");
                return null;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo preview: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Tạo preview với padding an toàn để tránh zipper tràn
     * @param context Context của ứng dụng
     * @param width Chiều rộng của view container
     * @param height Chiều cao của view container
     * @return Bitmap đã render hoặc null nếu thất bại
     */
    public static Bitmap createSafePreview(Context context, int width, int height) {
        try {
            Log.d(TAG, "Tạo safe preview: " + width + "x" + height);
            
            // Tính toán kích thước preview với tỷ lệ an toàn
            // Giảm chiều cao một chút để zipper không tràn
            int safeHeight = (int)(height * 0.95); // Giảm 5% chiều cao
            
            // Tạo preview với kích thước an toàn
            Bitmap originalPreview = createPreview(context, width, safeHeight, true);
            
            if (originalPreview == null) {
                return null;
            }
            
            // Tạo bitmap mới với kích thước đầy đủ và padding
            Bitmap finalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(finalBitmap);
            
            // Vẽ background màu tối (giống với theme app)
            canvas.drawColor(android.graphics.Color.parseColor("#1A1B3A")); // Navy blue background
            
            // Vẽ preview ở giữa với padding top
            int topPadding = (height - safeHeight) / 2;
            canvas.drawBitmap(originalPreview, 0, topPadding, null);
            
            // Recycle bitmap cũ
            originalPreview.recycle();
            
            Log.d(TAG, "Tạo safe preview thành công với padding");
            return finalBitmap;
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo safe preview: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Tạo preview với tỷ lệ 9:16 (portrait)
     * @param context Context của ứng dụng
     * @param baseWidth Chiều rộng cơ sở
     * @return Bitmap đã render hoặc null nếu thất bại
     */
    public static Bitmap createPortraitPreview(Context context, int baseWidth) {
        int height = (int) (baseWidth * 16.0 / 9.0);
        return createPreview(context, baseWidth, height);
    }
    
    /**
     * Tạo preview với tỷ lệ 16:9 (landscape)
     * @param context Context của ứng dụng
     * @param baseHeight Chiều cao cơ sở
     * @return Bitmap đã render hoặc null nếu thất bại
     */
    public static Bitmap createLandscapePreview(Context context, int baseHeight) {
        int width = (int) (baseHeight * 16.0 / 9.0);
        return createPreview(context, width, baseHeight);
    }
    
    /**
     * Tạo preview với hệ số tỷ lệ
     * @param context Context của ứng dụng
     * @param scaleFactor Hệ số tỷ lệ (0.1 - 2.0)
     * @return Bitmap đã render hoặc null nếu thất bại
     */
    public static Bitmap createScaledPreview(Context context, float scaleFactor) {
        if (scaleFactor < 0.1f || scaleFactor > 2.0f) {
            Log.w(TAG, "Scale factor không hợp lệ: " + scaleFactor + ", sử dụng 1.0");
            scaleFactor = 1.0f;
        }
        
        int width = (int) (1080 * scaleFactor);
        int height = (int) (1920 * scaleFactor);
        
        Log.d(TAG, "Tạo scaled preview: " + width + "x" + height + " (scale: " + scaleFactor + ")");
        return createPreview(context, width, height);
    }
    
    /**
     * Tạo preview với khóa ở trạng thái mở
     * @param context Context của ứng dụng
     * @param width Chiều rộng
     * @param height Chiều cao
     * @return Bitmap đã render hoặc null nếu thất bại
     */
    public static Bitmap createUnlockedPreview(Context context, int width, int height) {
        try {
            Log.d(TAG, "Tạo unlocked preview: " + width + "x" + height);
            
            // Kiểm tra GameAdapter có đang chạy không
            if (Glszl_GameAdapter.Inicialed) {
                // Thiết lập khóa ở vị trí mở trước khi chụp
                if (Glszl_lockerLayer.locker != null) {
                    Glszl_lockerLayer.locker.setTop(0.0d);
                    Log.d(TAG, "Đã thiết lập khóa ở vị trí mở");
                }
                
                // Chụp Bitmap
                Bitmap preview = Glszl_StaticRenderer.CreatePreviewBitmap(context, width, height, true);
                
                // Khôi phục vị trí khóa về giữa màn hình
                if (Glszl_lockerLayer.locker != null) {
                    double centerY = Glszl_lockerLayer.DeviceHeight * 0.5d;
                    Glszl_lockerLayer.locker.setTop(centerY);
                    Log.d(TAG, "Đã khôi phục vị trí khóa về giữa màn hình");
                }
                
                return preview;
            } else {
                Log.w(TAG, "GameAdapter chưa chạy, sử dụng test preview");
                return Glszl_StaticRenderer.CreateSimpleTestPreview(context, width, height);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo unlocked preview: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Tạo preview với khóa ở trạng thái đóng
     * @param context Context của ứng dụng
     * @param width Chiều rộng
     * @param height Chiều cao
     * @return Bitmap đã render hoặc null nếu thất bại
     */
    public static Bitmap createLockedPreview(Context context, int width, int height) {
        try {
            Log.d(TAG, "Tạo locked preview: " + width + "x" + height);
            
            // Kiểm tra GameAdapter có đang chạy không
            if (Glszl_GameAdapter.Inicialed) {
                // Thiết lập khóa ở vị trí đóng trước khi chụp
                if (Glszl_lockerLayer.locker != null) {
                    double centerY = Glszl_lockerLayer.DeviceHeight * 0.5d;
                    Glszl_lockerLayer.locker.setTop(centerY);
                    Log.d(TAG, "Đã thiết lập khóa ở vị trí đóng");
                }
                
                // Chụp Bitmap
                Bitmap preview = Glszl_StaticRenderer.CreatePreviewBitmap(context, width, height, true);
                
                // Khôi phục vị trí khóa về giữa màn hình
                if (Glszl_lockerLayer.locker != null) {
                    double centerY = Glszl_lockerLayer.DeviceHeight * 0.5d;
                    Glszl_lockerLayer.locker.setTop(centerY);
                    Log.d(TAG, "Đã khôi phục vị trí khóa về giữa màn hình");
                }
                
                return preview;
            } else {
                Log.w(TAG, "GameAdapter chưa chạy, sử dụng test preview");
                return Glszl_StaticRenderer.CreateSimpleTestPreview(context, width, height);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo locked preview: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Tạo preview với tiến trình mở khóa tùy ý
     * @param context Context của ứng dụng
     * @param width Chiều rộng
     * @param height Chiều cao
     * @param progress Tiến trình từ 0.0 (mở) đến 1.0 (đóng)
     * @return Bitmap đã render hoặc null nếu thất bại
     */
    public static Bitmap createCustomPreview(Context context, int width, int height, double progress) {
        try {
            // Giới hạn progress trong khoảng 0.0 - 1.0
            if (progress < 0.0) progress = 0.0;
            if (progress > 1.0) progress = 1.0;
            
            Log.d(TAG, "Tạo custom preview: " + width + "x" + height + " với progress: " + progress);
            
            // Kiểm tra GameAdapter có đang chạy không
            if (Glszl_GameAdapter.Inicialed) {
                // Thiết lập khóa ở vị trí tùy ý trước khi chụp
                if (Glszl_lockerLayer.locker != null) {
                    double maxDistance = Glszl_lockerLayer.DeviceHeight * 0.8d;
                    double position = maxDistance * progress;
                    Glszl_lockerLayer.locker.setTop(position);
                    Log.d(TAG, "Đã thiết lập khóa ở vị trí: " + position + " (progress: " + progress + ")");
                }
                
                // Chụp Bitmap
                Bitmap preview = Glszl_StaticRenderer.CreatePreviewBitmap(context, width, height, true);
                
                // Khôi phục vị trí khóa về giữa màn hình
                if (Glszl_lockerLayer.locker != null) {
                    double centerY = Glszl_lockerLayer.DeviceHeight * 0.5d;
                    Glszl_lockerLayer.locker.setTop(centerY);
                    Log.d(TAG, "Đã khôi phục vị trí khóa về giữa màn hình");
                }
                
                return preview;
            } else {
                Log.w(TAG, "GameAdapter chưa chạy, sử dụng test preview");
                return Glszl_StaticRenderer.CreateSimpleTestPreview(context, width, height);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo custom preview: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Tạo preview với timeout
     * @param context Context của ứng dụng
     * @param width Chiều rộng
     * @param height Chiều cao
     * @param timeoutMs Timeout tính bằng milliseconds
     * @return Bitmap đã render hoặc null nếu thất bại
     */
    public static Bitmap createPreviewWithTimeout(Context context, int width, int height, long timeoutMs) {
        try {
            Log.d(TAG, "Tạo preview với timeout: " + width + "x" + height + " timeout: " + timeoutMs + "ms");
            
            long startTime = System.currentTimeMillis();
            
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                // Kiểm tra GameAdapter có sẵn sàng không
                if (Glszl_GameAdapter.Inicialed && 
                    Glszl_GameAdapter.ctx != null && 
                    Glszl_GameAdapter.Background != null &&
                    Glszl_GameAdapter.Scene != null) {
                    
                    // GameAdapter đã sẵn sàng, tạo preview
                    return createPreview(context, width, height, true);
                }
                
                // Đợi một chút trước khi thử lại
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            Log.w(TAG, "Timeout khi chờ GameAdapter sẵn sàng, sử dụng test preview");
            return Glszl_StaticRenderer.CreateSimpleTestPreview(context, width, height);
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo preview với timeout: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Tạo preview với số lần thử và timeout
     * @param context Context của ứng dụng
     * @param width Chiều rộng
     * @param height Chiều cao
     * @param maxRetries Số lần thử tối đa
     * @param useFallback Có sử dụng fallback không
     * @param timeoutMs Timeout tính bằng milliseconds
     * @return Bitmap đã render hoặc null nếu thất bại
     */
    public static Bitmap createCustomPreview(Context context, int width, int height, int maxRetries, boolean useFallback, long timeoutMs) {
        try {
            Log.d(TAG, "Tạo custom preview: " + width + "x" + height + " retries: " + maxRetries + " fallback: " + useFallback + " timeout: " + timeoutMs + "ms");
            
            // Thử tạo preview với timeout trước
            Bitmap preview = createPreviewWithTimeout(context, width, height, timeoutMs);
            if (preview != null) {
                return preview;
            }
            
            // Nếu timeout thất bại, thử tạo preview bình thường
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                Log.d(TAG, "Thử tạo preview lần " + attempt + "/" + maxRetries);
                
                preview = createPreview(context, width, height, useFallback);
                if (preview != null) {
                    Log.d(TAG, "Tạo preview thành công sau " + attempt + " lần thử");
                    return preview;
                }
                
                // Đợi một chút trước khi thử lại
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            Log.w(TAG, "Không thể tạo preview sau " + maxRetries + " lần thử");
            return null;
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo custom preview: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Kiểm tra xem có thể tạo preview từ GameAdapter không
     * @return true nếu có thể tạo preview
     */
    public static boolean canCreatePreview() {
        try {
            return Glszl_GameAdapter.Inicialed && 
                   Glszl_GameAdapter.ctx != null && 
                   Glszl_GameAdapter.Background != null &&
                   Glszl_GameAdapter.Scene != null;
        } catch (Exception e) {
            Log.w(TAG, "Lỗi khi kiểm tra khả năng tạo preview: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Debug trạng thái GameAdapter
     */
    public static void debugGameAdapterState() {
        Glszl_StaticRenderer.debugGameAdapterState();
    }
}
