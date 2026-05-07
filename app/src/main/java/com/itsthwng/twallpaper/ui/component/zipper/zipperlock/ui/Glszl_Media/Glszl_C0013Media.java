package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Media;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.itsthwng.twallpaper.App;
import com.itsthwng.twallpaper.R;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_GameAdapter;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_Screen;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Resource.Glszl_Resources;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_AppAdapter;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_LockScreenService;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_servutils.Glszl_utils.Glszl_LockscreenService;
import com.itsthwng.twallpaper.ui.component.zipper.PrivateImageStore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;



public class Glszl_C0013Media {
    public static Bitmap heart_zipper_BateryCover;
    public static Bitmap heart_zipper_ChainLeft;
    public static int chain_type;
    public static Bitmap heart_zipper_ChainRight;
    public static boolean heart_zipper_Initialed = false;
    public static Bitmap heart_zipper_SelectedBackBg;
    public static Bitmap heart_zipper_SelectedBg;
    public static Bitmap heart_zipper_batteryLevel;
    public static Bitmap heart_zipper_camera;
    public static int heart_zipper_chain_index;
    public static Typeface heart_zipper_font1;
    public static List<Bitmap> heart_zipper_list = new ArrayList();
    public static Bitmap heart_zipper_more_apps;
    public static Bitmap heart_zipper_r_panel;
    public static Bitmap heart_zipper_r_panel_flech;
    public static Bitmap heart_zipper_torch_off;
    public static Bitmap heart_zipper_torch_on;
    public static boolean heart_zipper_versionFlag = false;
    public static Bitmap heart_zipper_wifi_off;
    public static Bitmap heart_zipper_wifi_on;
    public static Bitmap heart_zipper_zipper;
    public static int heart_zipper_zipper_index;

    public static void inicial() {
        if (Build.VERSION.SDK_INT < 26 && Build.VERSION.SDK_INT >= 23) {
            heart_zipper_versionFlag = false;
        }
        if (heart_zipper_Initialed) {
            LoadBgData();
            return;
        }
        heart_zipper_Initialed = true;
        if (heart_zipper_list == null) {
            heart_zipper_list = new ArrayList();
        }
        if (heart_zipper_versionFlag) {
            heart_zipper_font1 = Typeface.createFromAsset(Glszl_LockscreenService.heart_zipper_instance.getAssets(), "fonts/" + Glszl_AppAdapter.GetSelectedPhont(Glszl_GameAdapter.ctx));
        } else {
            heart_zipper_font1 = Typeface.createFromAsset(Glszl_LockScreenService.f10546cc.getAssets(), "fonts/" + Glszl_AppAdapter.GetSelectedPhont(Glszl_GameAdapter.ctx));
        }
        // Check if we're in preview mode and use preview values
        if (Glszl_LockScreenService.IsPreview && Glszl_LockScreenService.currentZipperId > 0) {
            heart_zipper_zipper_index = Glszl_LockScreenService.currentZipperId;
            heart_zipper_chain_index = Glszl_LockScreenService.currentChainId;
        } else {
            heart_zipper_zipper_index = Glszl_AppAdapter.getSelectedZiperNumber(Glszl_GameAdapter.ctx);
            heart_zipper_chain_index = Glszl_AppAdapter.getSelectedChainNumber(Glszl_GameAdapter.ctx);
        }
        if (heart_zipper_versionFlag) {
            heart_zipper_BateryCover = Glszl_Resources.CreateBitmap(R.drawable.batterycover, Glszl_LockscreenService.heart_zipper_instance);
            heart_zipper_batteryLevel = Glszl_Resources.CreateBitmap(R.drawable.batterylovel, Glszl_LockscreenService.heart_zipper_instance);
        } else {
            heart_zipper_BateryCover = Glszl_Resources.CreateBitmap(R.drawable.batterycover, Glszl_LockScreenService.f10546cc);
            heart_zipper_batteryLevel = Glszl_Resources.CreateBitmap(R.drawable.batterylovel, Glszl_LockScreenService.f10546cc);
        }
        
        // Load zipper images từ file đã download
        int zipperIndex = heart_zipper_zipper_index;
        android.util.Log.d("LoadZipperData", "Starting to load zipper image with index: " + zipperIndex);
        
        if (zipperIndex > 0) {
            try {
                // Tạo tên file theo ID đã lưu
                String fileName = "zippers_" + zipperIndex + ".webp";
                String fileNameJpg = "zippers_" + zipperIndex + ".jpg";
                String filenameFinal = "";
                if(PrivateImageStore.isDownloaded(getCurrentContext(), fileName)){
                    filenameFinal = fileName;
                } else if(PrivateImageStore.isDownloaded(getCurrentContext(), fileNameJpg)){
                    filenameFinal = fileNameJpg;
                }
                boolean isExist = !filenameFinal.isEmpty();
                
                if (isExist) {
                    // Load ảnh zipper từ file đã download
                    PrivateImageStore.ImageInfo info = PrivateImageStore.getImageInfo(getCurrentContext(), filenameFinal);

                    File imageFile = new File(info.path);
                    android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
                    android.graphics.Bitmap downloadedZipper = android.graphics.BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                    
                    if (downloadedZipper != null) {
                        heart_zipper_zipper = downloadedZipper;
                        android.util.Log.d("LoadZipperData", "Successfully loaded downloaded zipper image: " + imageFile.getAbsolutePath() + 
                            ", Size: " + downloadedZipper.getWidth() + "x" + downloadedZipper.getHeight());
                    } else {
                        // Fallback về drawable
                        android.util.Log.w("LoadZipperData", "Failed to decode downloaded zipper image, falling back to drawable");
                        loadZipperFromDrawable();
                    }
                } else {
                    // Fallback về drawable
//                    android.util.Log.w("LoadZipperData", "Zipper file not found, falling back to drawable. Expected path: " + imageFile.getAbsolutePath());
                    loadZipperFromDrawable();
                }
            } catch (Exception e) {
                android.util.Log.e("LoadZipperData", "Error loading downloaded zipper image: " + e.getMessage(), e);
                // Fallback về drawable
                loadZipperFromDrawable();
            }
        } else {
            // Default zipper images
            android.util.Log.d("LoadZipperData", "Zipper index is 0 or negative, using default drawable");
            loadZipperFromDrawable();
        }
        
        // Load chain images từ file đã download
        int chainIndex = heart_zipper_chain_index;
        if (chainIndex > 0) {
            try {
                System.out.println("NINVB => vao day chainIndex " + chainIndex);
                // Tạo tên file theo ID đã lưu chains_388_right
                String fileNameLeft = "chains_" + chainIndex +"_left" + ".webp";
                String fileNameRight = "chains_" + chainIndex + "_right" + ".webp";
                String fileNameLeftJpg = "chains_" + chainIndex +"_left" + ".jpg";
                String fileNameRightJpg = "chains_" + chainIndex + "_right" + ".jpg";

                String fileNameLeftFinal = "";
                String fileNameRightFinal = "";
                if(PrivateImageStore.isDownloaded(getCurrentContext(), fileNameLeft)){
                    fileNameLeftFinal = fileNameLeft;
                } else if(PrivateImageStore.isDownloaded(getCurrentContext(), fileNameLeftJpg)){
                    fileNameLeftFinal = fileNameLeftJpg;
                }
                if(PrivateImageStore.isDownloaded(getCurrentContext(), fileNameRight)){
                    fileNameRightFinal = fileNameRight;
                } else if(PrivateImageStore.isDownloaded(getCurrentContext(), fileNameRightJpg)){
                    fileNameRightFinal = fileNameRightJpg;
                }
                boolean isExistLeft = !fileNameLeftFinal.isEmpty();
                boolean isExistRight = !fileNameRightFinal.isEmpty();

                if (isExistLeft && isExistRight) {
                    // Load ảnh zipper từ file đã download
                    PrivateImageStore.ImageInfo infoLeft = PrivateImageStore.getImageInfo(getCurrentContext(), fileNameLeftFinal);
                    PrivateImageStore.ImageInfo infoRight = PrivateImageStore.getImageInfo(getCurrentContext(), fileNameRightFinal);

                    File imageFileLeft = new File(infoLeft.path);
                    File imageFileRight = new File(infoRight.path);
                    // Load ảnh chain từ file đã download
                    android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
                    android.graphics.Bitmap downloadedChainLeft = android.graphics.BitmapFactory.decodeFile(imageFileLeft.getAbsolutePath(), options);
                    android.graphics.Bitmap downloadedChainRight = android.graphics.BitmapFactory.decodeFile(imageFileRight.getAbsolutePath(), options);

                    if (downloadedChainRight != null && downloadedChainLeft != null) {
                        // Tạo chain left và right từ ảnh đã download
                        heart_zipper_ChainLeft = downloadedChainLeft;
                        heart_zipper_ChainRight = downloadedChainRight;
                    } else {
                        // Fallback về drawable
                        loadChainFromDrawable();
                    }
                } else {
                    // Fallback về drawable
                    loadChainFromDrawable();
                }
            } catch (Exception e) {
                android.util.Log.e("LoadChainData", "Error loading downloaded chain image: " + e.getMessage());
                // Fallback về drawable
                loadChainFromDrawable();
            }
        } else {
            // Default chain images
            loadChainFromDrawable();
        }
        int chainType = Glszl_AppAdapter.getChainType(Glszl_GameAdapter.ctx);
        chain_type = chainType;
        if (heart_zipper_versionFlag) {
            heart_zipper_r_panel = Glszl_Resources.CreateBitmap(R.drawable.right_panel, Glszl_LockscreenService.heart_zipper_instance);
            heart_zipper_r_panel_flech = Glszl_Resources.CreateBitmap(R.drawable.right_panel_flesh, Glszl_LockscreenService.heart_zipper_instance);
            heart_zipper_more_apps = Glszl_Resources.CreateBitmap(R.drawable.more, Glszl_LockscreenService.heart_zipper_instance);
            heart_zipper_camera = Glszl_Resources.CreateBitmap(R.drawable.camera, Glszl_LockscreenService.heart_zipper_instance);
            heart_zipper_wifi_on = Glszl_Resources.CreateBitmap(R.drawable.wifi_on, Glszl_LockscreenService.heart_zipper_instance);
            heart_zipper_wifi_off = Glszl_Resources.CreateBitmap(R.drawable.wifi_off, Glszl_LockscreenService.heart_zipper_instance);
            heart_zipper_torch_on = Glszl_Resources.CreateBitmap(R.drawable.torch_on, Glszl_LockscreenService.heart_zipper_instance);
            heart_zipper_torch_off = Glszl_Resources.CreateBitmap(R.drawable.torch, Glszl_LockscreenService.heart_zipper_instance);
        } else {
            heart_zipper_r_panel = Glszl_Resources.CreateBitmap(R.drawable.right_panel, Glszl_LockScreenService.f10546cc);
            heart_zipper_r_panel_flech = Glszl_Resources.CreateBitmap(R.drawable.right_panel_flesh, Glszl_LockScreenService.f10546cc);
            heart_zipper_more_apps = Glszl_Resources.CreateBitmap(R.drawable.more, Glszl_LockScreenService.f10546cc);
            heart_zipper_camera = Glszl_Resources.CreateBitmap(R.drawable.camera, Glszl_LockScreenService.f10546cc);
            heart_zipper_wifi_on = Glszl_Resources.CreateBitmap(R.drawable.wifi_on, Glszl_LockScreenService.f10546cc);
            heart_zipper_wifi_off = Glszl_Resources.CreateBitmap(R.drawable.wifi_off, Glszl_LockScreenService.f10546cc);
            heart_zipper_torch_on = Glszl_Resources.CreateBitmap(R.drawable.torch_on, Glszl_LockScreenService.f10546cc);
            heart_zipper_torch_off = Glszl_Resources.CreateBitmap(R.drawable.torch, Glszl_LockScreenService.f10546cc);
        }
        LoadBgData();
    }

    public static Bitmap changeBitmapColorChain(Bitmap bitmap, int i) {
        Bitmap copy = bitmap.copy(bitmap.getConfig(), true);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(i, PorterDuff.Mode.SRC_IN));
        new Canvas(copy).drawBitmap(copy, 0.0f, 0.0f, paint);
        return copy;
    }

    private static void LoadBgData() {
//        // Kiểm tra quyền trước khi load ảnh
//        if (!checkStoragePermission()) {
//            android.util.Log.w("LoadBgData", "Storage permission not granted, using drawable resources");
//            loadBackgroundFromDrawable();
//            loadBackgroundBackFromDrawable();
//            return;
//        }

        // Load ảnh foreground từ file đã download
        // Check if we're in preview mode and use preview values
        int selectedWallpaperNumber;
        if (Glszl_LockScreenService.IsPreview && Glszl_LockScreenService.currentWallpaperId > 0) {
            selectedWallpaperNumber = Glszl_LockScreenService.currentWallpaperId;
        } else {
            selectedWallpaperNumber = Glszl_AppAdapter.getSelectedWallpaperNumber(Glszl_GameAdapter.ctx);
        }

        // Kiểm tra xem có file đã download không
        if (selectedWallpaperNumber > 0) {
            try {
                // Tạo tên file theo ID đã lưu
                String fileName = "zipper_images_" + selectedWallpaperNumber + ".webp";
                String fileNameJpg = "zipper_images_" + selectedWallpaperNumber + ".jpg";
                String filenameFinal = "";
                if(PrivateImageStore.isDownloaded(getCurrentContext(), fileName)){
                    filenameFinal = fileName;
                } else if(PrivateImageStore.isDownloaded(getCurrentContext(), fileNameJpg)){
                    filenameFinal = fileNameJpg;
                }
                boolean isExist = !filenameFinal.isEmpty();
                if (isExist) {
                    // Load ảnh zipper từ file đã download
                    PrivateImageStore.ImageInfo info = PrivateImageStore.getImageInfo(getCurrentContext(), filenameFinal);

                    File imageFile = new File(info.path);
                    // Load ảnh từ file đã download
                    android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
                    heart_zipper_SelectedBg = android.graphics.BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                    android.util.Log.d("LoadBgData", "Loaded downloaded image: " + imageFile.getAbsolutePath());
                } else {
                    // Fallback: load từ drawable resources
                    android.util.Log.w("LoadBgData", "Downloaded image file not found, falling back to drawable");
                    loadBackgroundFromDrawable();
                }
            } catch (Exception e) {
                android.util.Log.e("LoadBgData", "Error loading downloaded image: " + e.getMessage());
                // Fallback to drawable
                loadBackgroundFromDrawable();
            }
        } else {
            // Default image
            android.util.Log.d("LoadBgData", "No selected wallpaper number, using default drawable");
            loadBackgroundFromDrawable();
        }

        // Load ảnh background từ file đã download
        // Check if we're in preview mode and use preview values
        int selectedWallpaperBgNumber;
        if (Glszl_LockScreenService.IsPreview && Glszl_LockScreenService.currentWallpaperBgId > 0) {
            selectedWallpaperBgNumber = Glszl_LockScreenService.currentWallpaperBgId;
        } else {
            selectedWallpaperBgNumber = Glszl_AppAdapter.getSelectedWallpaperBgNumber(Glszl_GameAdapter.ctx);
        }
        boolean isShowBackground = Glszl_AppAdapter.isShowBackground(Glszl_GameAdapter.ctx);
        // Kiểm tra xem có file background đã download không
        if (selectedWallpaperBgNumber > 0 && isShowBackground) {
            try {
                // Tạo tên file theo ID đã lưu
                String fileName = "zipper_images_" + selectedWallpaperBgNumber + ".webp";
                String fileNameJpg = "zipper_images_" + selectedWallpaperBgNumber + ".jpg";
                String filenameFinal = "";
                if(PrivateImageStore.isDownloaded(getCurrentContext(), fileName)){
                    filenameFinal = fileName;
                } else if(PrivateImageStore.isDownloaded(getCurrentContext(), fileNameJpg)){
                    filenameFinal = fileNameJpg;
                }
                boolean isExist = !filenameFinal.isEmpty();

                if (isExist) {
                    // Load ảnh zipper từ file đã download
                    PrivateImageStore.ImageInfo info = PrivateImageStore.getImageInfo(getCurrentContext(), filenameFinal);

                    File imageFile = new File(info.path);
                    // Load ảnh background từ file đã download
                    android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
                    heart_zipper_SelectedBackBg = android.graphics.BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                    android.util.Log.d("LoadBgData", "Loaded downloaded background image: " + imageFile.getAbsolutePath());
                } else {
                    // Fallback: load từ drawable resources
                    android.util.Log.w("LoadBgData", "Downloaded background image file not found, falling back to drawable");
                    loadBackgroundBackFromDrawable();
                }
            } catch (Exception e) {
                android.util.Log.e("LoadBgData", "Error loading downloaded background image: " + e.getMessage());
                // Fallback to drawable
                loadBackgroundBackFromDrawable();
            }
        } else if(isShowBackground) {
            // Default background image
            android.util.Log.d("LoadBgData", "No selected background wallpaper number, using default drawable");
            loadBackgroundBackFromDrawable();
        } else {
            heart_zipper_SelectedBackBg = null;
            android.util.Log.d("LoadBgData", "User select not to show background image");
        }
    }

    /**
     * Kiểm tra quyền truy cập storage
     */
    private static boolean checkStoragePermission() {
        Context context = getCurrentContext();
        if (context == null) {
            android.util.Log.e("LoadBgData", "Context is null, cannot check permission");
            return false;
        }

        boolean hasPermission = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ sử dụng READ_MEDIA_IMAGES
            hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12 trở xuống sử dụng READ_EXTERNAL_STORAGE
            hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED;
        }

        android.util.Log.d("LoadBgData", "Storage permission check result: " + hasPermission);
        return hasPermission;
    }

    /**
     * Lấy context hiện tại dựa trên version flag
     */
    private static Context getCurrentContext() {
        if (heart_zipper_versionFlag) {
            return Glszl_LockscreenService.heart_zipper_instance;
        } else {
            return Glszl_LockScreenService.f10546cc;
        }
    }

    /**
     * Load background image từ drawable resources (fallback method)
     */
    private static void loadBackgroundFromDrawable() {
        // Check if we're in preview mode and use preview values
        int selectedWallpaperNumber;
        if (Glszl_LockScreenService.IsPreview && Glszl_LockScreenService.currentWallpaperId > 0) {
            selectedWallpaperNumber = Glszl_LockScreenService.currentWallpaperId;
        } else {
            selectedWallpaperNumber = Glszl_AppAdapter.getSelectedWallpaperNumber(Glszl_GameAdapter.ctx);
        }
        int parseInt = Integer.parseInt(App.instance.getApplicationContext().getResources().getString(R.string.backgroundCound));
        String str = "image";
        if (selectedWallpaperNumber <= 0 || selectedWallpaperNumber >= parseInt + 1) {
            str = "image";
        } else {
            str = "image" + selectedWallpaperNumber;
        }
        int identifier = App.instance.getApplicationContext().getResources().getIdentifier(str, "drawable", App.instance.getApplicationContext().getPackageName());
        if (heart_zipper_versionFlag) {
            heart_zipper_SelectedBg = Glszl_Resources.CreateBitmap(identifier, Glszl_LockscreenService.heart_zipper_instance);
        } else {
            heart_zipper_SelectedBg = Glszl_Resources.CreateBitmap(identifier, Glszl_LockScreenService.f10546cc);
        }
        android.util.Log.d("LoadBgData", "Fallback to drawable: " + str);
    }

    /**
     * Load background back image từ drawable resources (fallback method)
     */
    private static void loadBackgroundBackFromDrawable() {
        // Check if we're in preview mode and use preview values
        int selectedWallpaperBgNumber;
        if (Glszl_LockScreenService.IsPreview && Glszl_LockScreenService.currentWallpaperBgId > 0) {
            selectedWallpaperBgNumber = Glszl_LockScreenService.currentWallpaperBgId;
        } else {
            selectedWallpaperBgNumber = Glszl_AppAdapter.getSelectedWallpaperBgNumber(Glszl_GameAdapter.ctx);
        }
        int parseInt2 = Integer.parseInt(App.instance.getApplicationContext().getResources().getString(R.string.backgroundCound));
        String str2 = "image";
        if (selectedWallpaperBgNumber > 1 && selectedWallpaperBgNumber < parseInt2 + 2) {
            StringBuilder sb = new StringBuilder();
            sb.append(str2);
            sb.append(selectedWallpaperBgNumber - 1);
            str2 = sb.toString();
        }
        if (selectedWallpaperBgNumber == 1) {
            str2 = "image1";
        }
        int identifier2 = App.instance.getApplicationContext().getResources().getIdentifier(str2, "drawable", App.instance.getApplicationContext().getPackageName());
        if (heart_zipper_versionFlag) {
            heart_zipper_SelectedBackBg = Glszl_Resources.CreateBitmap(identifier2, Glszl_LockscreenService.heart_zipper_instance);
        } else {
            heart_zipper_SelectedBackBg = Glszl_Resources.CreateBitmap(identifier2, Glszl_LockScreenService.f10546cc);
        }
        android.util.Log.d("LoadBgData", "Fallback to drawable background: " + str2);
    }

    public static Bitmap FixImageResolution(Bitmap bitmap) {
        if (((double) (bitmap.getWidth() / bitmap.getHeight())) >= Glszl_Screen.Width / Glszl_Screen.Height) {
            return Bitmap.createBitmap(bitmap, (bitmap.getWidth() / 2) - (bitmap.getHeight() / 2), 0, bitmap.getHeight(), bitmap.getHeight());
        }
        return Bitmap.createBitmap(bitmap, 0, (bitmap.getHeight() / 2) - (bitmap.getWidth() / 2), bitmap.getWidth(), bitmap.getWidth());
    }

    public static Bitmap scaleCenterCrop(Bitmap bitmap, int i, int i2) {
        float f = (float) i2;
        float width = (float) bitmap.getWidth();
        float f2 = (float) i;
        float height = (float) bitmap.getHeight();
        float max = Math.max(f / width, f2 / height);
        float f3 = width * max;
        float f4 = max * height;
        float f5 = (f - f3) / 2.0f;
        float f6 = (f2 - f4) / 2.0f;
        RectF rectF = new RectF(f5, f6, f3 + f5, f4 + f6);
        Bitmap createBitmap = Bitmap.createBitmap(i2, i, bitmap.getConfig());
        new Canvas(createBitmap).drawBitmap(bitmap, (Rect) null, rectF, (Paint) null);
        return createBitmap;
    }

    public static Bitmap FixImageResolution2(Bitmap bitmap) {
        if (((double) (bitmap.getWidth() / bitmap.getHeight())) >= Glszl_Screen.Width / Glszl_Screen.Height) {
            int i = (int) 0.0d;
            return Bitmap.createBitmap(bitmap, i, i, (int) ((double) bitmap.getWidth()), (int) ((Glszl_Screen.Height / Glszl_Screen.Width) * ((double) bitmap.getWidth())));
        }
        int i2 = (int) 0.0d;
        return Bitmap.createBitmap(bitmap, i2, i2, (int) ((Glszl_Screen.Width / Glszl_Screen.Height) * ((double) bitmap.getHeight())), (int) ((double) bitmap.getHeight()));
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap createBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return createBitmap;
    }

    public static InputStream bitmapToInputStream(Bitmap bitmap) {
        ByteBuffer allocate = ByteBuffer.allocate(bitmap.getHeight() * bitmap.getRowBytes());
        bitmap.copyPixelsToBuffer(allocate);
        return new ByteArrayInputStream(allocate.array());
    }
    
    /**
     * Load chain images từ drawable resources (fallback method)
     */
    private static void loadChainFromDrawable() {
        String str2 = "chainleft";
        String str3 = "chainright";
        int i2 = heart_zipper_chain_index;
        if (i2 > 1 && i2 < 21) {
            str2 = str2 + (heart_zipper_chain_index - 1);
            str3 = str3 + (heart_zipper_chain_index - 1);
        }
        int identifier2 = App.instance.getApplicationContext().getResources().getIdentifier(str2, "drawable", App.instance.getApplicationContext().getPackageName());
        int identifier3 = App.instance.getApplicationContext().getResources().getIdentifier(str3, "drawable", App.instance.getApplicationContext().getPackageName());
        if (heart_zipper_versionFlag) {
            heart_zipper_ChainLeft = Glszl_Resources.CreateBitmap(identifier2, Glszl_LockscreenService.heart_zipper_instance);
            heart_zipper_ChainRight = Glszl_Resources.CreateBitmap(identifier3, Glszl_LockscreenService.heart_zipper_instance);
        } else {
            heart_zipper_ChainLeft = Glszl_Resources.CreateBitmap(identifier2, Glszl_LockScreenService.f10546cc);
            heart_zipper_ChainRight = Glszl_Resources.CreateBitmap(identifier3, Glszl_LockScreenService.f10546cc);
        }
        android.util.Log.d("LoadChainData", "Fallback to drawable chain: " + str2 + ", " + str3);
    }

    /**
     * Load zipper images từ drawable resources (fallback method)
     */
    private static void loadZipperFromDrawable() {
        String str2 = "zipper";
        int i2 = heart_zipper_zipper_index;
        if (i2 > 1 && i2 < 21) {
            str2 = str2 + (heart_zipper_zipper_index - 1);
        }
        int identifier2 = App.instance.getApplicationContext().getResources().getIdentifier(str2, "drawable", App.instance.getApplicationContext().getPackageName());
        
        android.util.Log.d("LoadZipperData", "Loading zipper from drawable: " + str2 + " (identifier: " + identifier2 + ")");
        
        if (heart_zipper_versionFlag) {
            heart_zipper_zipper = Glszl_Resources.CreateBitmap(identifier2, Glszl_LockscreenService.heart_zipper_instance);
        } else {
            heart_zipper_zipper = Glszl_Resources.CreateBitmap(identifier2, Glszl_LockScreenService.f10546cc);
        }
        
        if (heart_zipper_zipper != null) {
            android.util.Log.d("LoadZipperData", "Successfully loaded zipper from drawable: " + str2 + 
                ", Size: " + heart_zipper_zipper.getWidth() + "x" + heart_zipper_zipper.getHeight());
        } else {
            android.util.Log.e("LoadZipperData", "Failed to load zipper from drawable: " + str2);
        }
        
        android.util.Log.d("LoadZipperData", "Fallback to drawable zipper: " + str2);
    }

    public static void Clear() {
        List<Bitmap> list2 = heart_zipper_list;
        if (list2 != null) {
            list2.clear();
        }
        heart_zipper_BateryCover = null;
        heart_zipper_list = null;
        heart_zipper_font1 = null;
        heart_zipper_SelectedBg = null;
        heart_zipper_zipper = null;
        heart_zipper_ChainLeft = null;
        heart_zipper_ChainRight = null;
        heart_zipper_batteryLevel = null;
        heart_zipper_Initialed = false;
        heart_zipper_r_panel = null;
        heart_zipper_r_panel_flech = null;
        heart_zipper_more_apps = null;
        heart_zipper_camera = null;
        heart_zipper_wifi_on = null;
        heart_zipper_wifi_off = null;
        heart_zipper_torch_on = null;
        heart_zipper_torch_off = null;
        heart_zipper_SelectedBackBg = null;
    }
}
