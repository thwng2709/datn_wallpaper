package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_Urect;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_UimagePart;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_Uimage;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Layers.Glszl_BackgroundLayer;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Layers.Glszl_actionLayer;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Layers.Glszl_lockerLayer;

/**
 * Glszl_StaticRenderer - Lấy Bitmap trực tiếp từ GameAdapter
 * KHÔNG thay đổi gì về GameAdapter, chỉ đọc dữ liệu từ nó
 */
public class Glszl_StaticRenderer {
    private static final String TAG = "Glszl_StaticRenderer";
    
    /**
     * Lấy Bitmap trực tiếp từ GameAdapter hiện tại
     * @param context Context của ứng dụng
     * @param width Chiều rộng của Bitmap output
     * @param height Chiều cao của Bitmap output
     * @return Bitmap đã được render hoặc null nếu thất bại
     */
    public static Bitmap CreatePreviewBitmap(Context context, int width, int height) {
        return CreatePreviewBitmap(context, width, height, true);
    }
    
    /**
     * Lấy Bitmap trực tiếp từ GameAdapter hiện tại
     * @param context Context của ứng dụng
     * @param width Chiều rộng của Bitmap output
     * @param height Chiều cao của Bitmap output
     * @param useFallback Có sử dụng fallback không
     * @return Bitmap đã được render hoặc null nếu thất bại
     */
    public static Bitmap CreatePreviewBitmap(Context context, int width, int height, boolean useFallback) {
        try {
            Log.d(TAG, "Tạo preview từ GameAdapter: " + width + "x" + height);
            
            // Kiểm tra GameAdapter có đang chạy không
            if (!isGameAdapterReady()) {
                Log.w(TAG, "GameAdapter chưa sẵn sàng");
                if (useFallback) {
                    return CreateFallbackPreview(context, width, height);
                }
                return null;
            }
            
            // Lấy Bitmap trực tiếp từ GameAdapter
            Bitmap bitmap = captureGameAdapterBitmap(width, height);
            
            if (bitmap != null) {
                Log.d(TAG, "Tạo preview thành công từ GameAdapter - Bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                return bitmap;
            } else {
                Log.w(TAG, "Không thể lấy Bitmap từ GameAdapter");
                if (useFallback) {
                    return CreateFallbackPreview(context, width, height);
                }
                return null;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo preview từ GameAdapter: " + e.getMessage());
            if (useFallback) {
                return CreateFallbackPreview(context, width, height);
            }
            return null;
        }
    }
    
    /**
     * Kiểm tra GameAdapter có sẵn sàng để lấy Bitmap không
     * @return true nếu GameAdapter sẵn sàng
     */
    private static boolean isGameAdapterReady() {
        try {
            return Glszl_GameAdapter.Inicialed && 
                   Glszl_GameAdapter.ctx != null && 
                   Glszl_GameAdapter.Background != null &&
                   Glszl_GameAdapter.Scene != null;
        } catch (Exception e) {
            Log.w(TAG, "Lỗi khi kiểm tra GameAdapter: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Chụp Bitmap trực tiếp từ GameAdapter
     * @param width Chiều rộng
     * @param height Chiều cao
     * @return Bitmap đã chụp hoặc null
     */
    private static Bitmap captureGameAdapterBitmap(int width, int height) {
        try {
            // Tạo Bitmap với kích thước đã định
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            
            // Vẽ trực tiếp từ Scene của GameAdapter
            if (Glszl_GameAdapter.GetMainRect() != null) {
//                // Lưu trạng thái hiện tại của GameAdapter
//                saveGameAdapterState();
//
//                // LUÔN thiết lập vị trí khóa về trạng thái đóng hoàn toàn
//                // để đảm bảo hiển thị UI hoàn chỉnh, không phụ thuộc vào trạng thái hiện tại
//                setLockerToLockedPosition();
//
//                // Đợi một chút để đảm bảo các thay đổi vị trí được áp dụng
//                try {
//                    Thread.sleep(50);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
                
                // Vẽ Scene của GameAdapter lên canvas
                Glszl_GameAdapter.GetMainRect().Draw(canvas);
                
//                // Khôi phục trạng thái GameAdapter
//                restoreGameAdapterState();
                
                return bitmap;
            } else {
                Log.e(TAG, "GameAdapter.Scene null");
                return null;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi chụp Bitmap từ GameAdapter: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Lưu trạng thái hiện tại của GameAdapter
     */
    private static void saveGameAdapterState() {
        try {
            // Lưu vị trí hiện tại của locker
            if (Glszl_lockerLayer.locker != null) {
                // Lưu vị trí hiện tại để khôi phục sau
                double currentLockerTop = Glszl_lockerLayer.locker.getTop();
                Log.d(TAG, "Đã lưu vị trí locker hiện tại: " + currentLockerTop);
            }
            
            // Lưu vị trí hiện tại của right panel
            if (Glszl_lockerLayer.RightPanelHolcer != null) {
                double currentRightPanelLeft = Glszl_lockerLayer.RightPanelHolcer.getLeft();
                Log.d(TAG, "Đã lưu vị trí right panel hiện tại: " + currentRightPanelLeft);
            }
            
            // Lưu trạng thái các action elements
            if (Glszl_actionLayer.left != null) {
                for (int i = 0; i < Glszl_actionLayer.left.size(); i++) {
                    Glszl_UimagePart part = Glszl_actionLayer.left.get(i);
                    if (part != null) {
                        Log.d(TAG, "Đã lưu trạng thái left action element " + i);
                    }
                }
            }
            
            if (Glszl_actionLayer.right != null) {
                for (int i = 0; i < Glszl_actionLayer.right.size(); i++) {
                    Glszl_UimagePart part = Glszl_actionLayer.right.get(i);
                    if (part != null) {
                        Log.d(TAG, "Đã lưu trạng thái right action element " + i);
                    }
                }
            }
            
            Log.d(TAG, "Đã lưu trạng thái GameAdapter hoàn chỉnh");
            
        } catch (Exception e) {
            Log.w(TAG, "Lỗi khi lưu trạng thái GameAdapter: " + e.getMessage());
        }
    }
    
    /**
     * Khôi phục trạng thái GameAdapter
     */
    private static void restoreGameAdapterState() {
        try {
            // Khôi phục vị trí của locker về vị trí ban đầu
            if (Glszl_lockerLayer.locker != null) {
                // Khôi phục về vị trí giữa màn hình (trạng thái đóng)
                double centerY = Glszl_lockerLayer.DeviceHeight * 0.5d;
                Glszl_lockerLayer.locker.setTop(centerY);
                Log.d(TAG, "Đã khôi phục locker về vị trí đóng: " + centerY);
            }
            
            // Khôi phục vị trí của right panel
            if (Glszl_lockerLayer.RightPanelHolcer != null) {
                double rightPanelX = Glszl_lockerLayer.DeviceWidth - (Glszl_lockerLayer.RightPanelHolcer.Width() / 9.0d);
                Glszl_lockerLayer.RightPanelHolcer.setLeft(rightPanelX);
                Log.d(TAG, "Đã khôi phục right panel về vị trí hiển thị");
            }
            
            // Khôi phục trạng thái các action elements
            if (Glszl_actionLayer.left != null) {
                for (Glszl_UimagePart part : Glszl_actionLayer.left) {
                    if (part != null) {
                        // Khôi phục về vị trí mặc định
                        part.setLeft(-Glszl_actionLayer.heart_zipper_space2);
                        part.setWidth(Glszl_lockerLayer.DeviceWidth / 2.0d);
                        
                        // Reset rotation của các child elements
                        for (Glszl_Urect child : part.getChildrens()) {
                            if (child instanceof Glszl_Uimage) {
                                child.setRotate(0.0d);
                            }
                        }
                    }
                }
                Log.d(TAG, "Đã khôi phục left action elements");
            }
            
            if (Glszl_actionLayer.right != null) {
                for (Glszl_UimagePart part : Glszl_actionLayer.right) {
                    if (part != null) {
                        // Khôi phục về vị trí mặc định
                        part.setLeft((Glszl_lockerLayer.DeviceWidth / 2.0d) + Glszl_actionLayer.heart_zipper_space2);
                        part.setWidth(Glszl_lockerLayer.DeviceWidth / 2.0d);
                        
                        // Reset rotation của các child elements
                        for (Glszl_Urect child : part.getChildrens()) {
                            if (child instanceof Glszl_Uimage) {
                                child.setRotate(0.0d);
                            }
                        }
                    }
                }
                Log.d(TAG, "Đã khôi phục right action elements");
            }
            
            Log.d(TAG, "Đã khôi phục trạng thái GameAdapter hoàn chỉnh");
            
        } catch (Exception e) {
            Log.w(TAG, "Lỗi khi khôi phục trạng thái GameAdapter: " + e.getMessage());
        }
    }
    
    /**
     * Thiết lập vị trí khóa ở trạng thái đóng hoàn toàn (để hiển thị UI hoàn chỉnh)
     */
    private static void setLockerToLockedPosition() {
        try {
            // Thiết lập vị trí khóa ở giữa màn hình (trạng thái đóng hoàn toàn)
            if (Glszl_lockerLayer.locker != null) {
                double centerY = Glszl_lockerLayer.DeviceHeight * 0.5d;
                Glszl_lockerLayer.locker.setTop(centerY);
                Log.d(TAG, "Đã thiết lập locker ở vị trí đóng hoàn toàn: " + centerY);
            }
            
            // Đảm bảo right panel được hiển thị
            if (Glszl_lockerLayer.RightPanelHolcer != null) {
                double rightPanelX = Glszl_lockerLayer.DeviceWidth - (Glszl_lockerLayer.RightPanelHolcer.Width() / 9.0d);
                Glszl_lockerLayer.RightPanelHolcer.setLeft(rightPanelX);
                Log.d(TAG, "Đã hiển thị right panel");
            }
            
            // Đảm bảo các action layer elements ở vị trí đóng
            if (Glszl_actionLayer.left != null) {
                for (Glszl_UimagePart part : Glszl_actionLayer.left) {
                    if (part != null) {
                        // Đặt lại vị trí mặc định cho left action elements
                        part.setLeft(-Glszl_actionLayer.heart_zipper_space2);
                        part.setWidth(Glszl_lockerLayer.DeviceWidth / 2.0d);
                        
                        // Reset rotation của các child elements
                        for (Glszl_Urect child : part.getChildrens()) {
                            if (child instanceof Glszl_Uimage) {
                                child.setRotate(0.0d);
                            }
                        }
                        
                        Log.d(TAG, "Đã reset left action element");
                    }
                }
            }
            
            if (Glszl_actionLayer.right != null) {
                for (Glszl_UimagePart part : Glszl_actionLayer.right) {
                    if (part != null) {
                        // Đặt lại vị trí mặc định cho right action elements
                        part.setLeft((Glszl_lockerLayer.DeviceWidth / 2.0d) + Glszl_actionLayer.heart_zipper_space2);
                        part.setWidth(Glszl_lockerLayer.DeviceWidth / 2.0d);
                        
                        // Reset rotation của các child elements
                        for (Glszl_Urect child : part.getChildrens()) {
                            if (child instanceof Glszl_Uimage) {
                                child.setRotate(0.0d);
                            }
                        }
                        
                        Log.d(TAG, "Đã reset right action element");
                    }
                }
            }
            
        } catch (Exception e) {
            Log.w(TAG, "Lỗi khi thiết lập vị trí khóa: " + e.getMessage());
        }
    }
    
    /**
     * Tạo fallback preview đơn giản khi không thể lấy từ GameAdapter
     * @param context Context của ứng dụng
     * @param width Chiều rộng
     * @param height Chiều cao
     * @return Bitmap đơn giản
     */
    private static Bitmap CreateFallbackPreview(Context context, int width, int height) {
        try {
            Log.d(TAG, "Tạo fallback preview " + width + "x" + height);
            
            // Tạo Bitmap đơn giản với màu nền
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            
            // Vẽ nền xanh
            Paint paint = new Paint();
            paint.setColor(android.graphics.Color.rgb(100, 150, 200));
            canvas.drawRect(0, 0, width, height, paint);
            
            // Vẽ text thông báo
            paint.setColor(android.graphics.Color.WHITE);
            paint.setTextSize(width / 20.0f);
            paint.setTextAlign(Paint.Align.CENTER);
            
            String message = "Preview không khả dụng";
            float textX = width / 2.0f;
            float textY = height / 2.0f;
            
            canvas.drawText(message, textX, textY, paint);
            
            return bitmap;
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo fallback preview: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Tạo preview đơn giản để test
     * @param context Context của ứng dụng
     * @param width Chiều rộng
     * @param height Chiều cao
     * @return Bitmap đơn giản
     */
    public static Bitmap CreateSimpleTestPreview(Context context, int width, int height) {
        try {
            Log.d(TAG, "Tạo simple test preview " + width + "x" + height);
            
            // Tạo Bitmap
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            
            // Vẽ nền xanh
            Paint paint = new Paint();
            paint.setColor(android.graphics.Color.rgb(100, 150, 200));
            canvas.drawRect(0, 0, width, height, paint);
            
            // Vẽ một số hình đơn giản
            paint.setColor(android.graphics.Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            
            // Vẽ hình tròn
            canvas.drawCircle(width / 2.0f, height / 3.0f, 100, paint);
            
            // Vẽ hình chữ nhật
            paint.setColor(android.graphics.Color.rgb(255, 100, 100));
            canvas.drawRect(width / 4.0f, height / 2.0f, width * 3.0f / 4.0f, height * 3.0f / 4.0f, paint);
            
            // Vẽ text
            paint.setColor(android.graphics.Color.BLACK);
            paint.setTextSize(width / 15.0f);
            paint.setTextAlign(Paint.Align.CENTER);
            
            String message = "Test Preview - " + width + "x" + height;
            float textX = width / 2.0f;
            float textY = height * 0.9f;
            
            canvas.drawText(message, textX, textY, paint);
            
            Log.d(TAG, "Simple test preview tạo thành công");
            return bitmap;
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo simple test preview: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Debug: Kiểm tra trạng thái của GameAdapter
     */
    public static void debugGameAdapterState() {
        Log.d(TAG, "=== DEBUG GAME ADAPTER STATE ===");
        Log.d(TAG, "GameAdapter.Inicialed: " + Glszl_GameAdapter.Inicialed);
        Log.d(TAG, "GameAdapter.ctx: " + (Glszl_GameAdapter.ctx != null ? "not null" : "null"));
        Log.d(TAG, "GameAdapter.Background: " + (Glszl_GameAdapter.Background != null ? "not null" : "null"));
        Log.d(TAG, "GameAdapter.Scene: " + (Glszl_GameAdapter.Scene != null ? "not null" : "null"));
        
        if (Glszl_GameAdapter.Background != null) {
            Log.d(TAG, "GameAdapter.Background children count: " + Glszl_GameAdapter.Background.getChildrens().size());
        }
        
        // Kiểm tra các layer
        try {
            Log.d(TAG, "BackgroundLayer.heart_zipper_f0bg: " + (Glszl_BackgroundLayer.heart_zipper_f0bg != null ? "not null" : "null"));
            Log.d(TAG, "ActionLayer.left: " + (Glszl_actionLayer.left != null ? "size " + Glszl_actionLayer.left.size() : "null"));
            Log.d(TAG, "ActionLayer.right: " + (Glszl_actionLayer.right != null ? "size " + Glszl_actionLayer.right.size() : "null"));
            Log.d(TAG, "LockerLayer.locker: " + (Glszl_lockerLayer.locker != null ? "not null" : "null"));
            Log.d(TAG, "LockerLayer.RightPanelHolcer: " + (Glszl_lockerLayer.RightPanelHolcer != null ? "not null" : "null"));
        } catch (Exception e) {
            Log.w(TAG, "Lỗi khi kiểm tra layers: " + e.getMessage());
        }
        
        Log.d(TAG, "=== END DEBUG ===");
    }
}
