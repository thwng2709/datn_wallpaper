package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui;

import android.content.Context;

import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.utils.PrefKey;

public class Glszl_AppAdapter {
    public static int getSelectedFontNumber(Context context) {
        boolean isPreviewMode = Glszl_UserDataAdapter.getIsPreview();
        int LoadPref = 0;
        if(isPreviewMode){
            int tempId = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_FONT_TEMP, context);
            if(tempId == 0 || tempId == -1){
                LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_FONT, context);
            } else {
                LoadPref = tempId;
            }
        } else {
            LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_FONT, context);
        }
        if (LoadPref == 0) {
            return 1;
        }
        return LoadPref;
    }

    public static void SetLock(Context context, String str) {
        Glszl_UserDataAdapter.SavePref(PrefKey.LOCKSCREEN, str, context);
    }

    public static int getSelectedZiperNumber(Context context) {
        boolean isPreviewMode = Glszl_UserDataAdapter.getIsPreview();
        int LoadPref = 0;
        if(isPreviewMode){
            int tempId = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_ZIPPER_TEMP, context);
            if(tempId == 0 || tempId == -1){
                LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_ZIPPER, context);
            } else {
                LoadPref = tempId;
            }
        } else {
            LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_ZIPPER, context);
        }
        if (LoadPref == 0) {
            return 1;
        }
        return LoadPref;
    }

    public static int getSelectedChainNumber(Context context) {
        boolean isPreviewMode = Glszl_UserDataAdapter.getIsPreview();
        int LoadPref = 0;
        if(isPreviewMode){
            int tempId = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_CHAIN_TEMP, context);
            if(tempId == 0 || tempId == -1){
                LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_CHAIN, context);
            } else {
                LoadPref = tempId;
            }
        } else {
            LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_CHAIN, context);
        }
        if (LoadPref == 0) {
            return 1;
        }
        return LoadPref;
    }

    public static int getSelectedChainType(Context context) {
        boolean isPreviewMode = Glszl_UserDataAdapter.getIsPreview();
        int LoadPref = 0;
        if(isPreviewMode){
            int tempId = Glszl_UserDataAdapter.LoadPref(PrefKey.CHAIN_TYPE_TEMP, context);
            if(tempId == 0 || tempId == -1){
                LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.CHAIN_TYPE, context);
            } else {
                LoadPref = tempId;
            }
        } else {
            LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.CHAIN_TYPE, context);
        }
        return LoadPref;
    }

    public static int getChainType(Context context) {
        return Glszl_UserDataAdapter.LoadPref(PrefKey.CHAIN_TYPE, context);
    }

    public static int getChainTypeTemp(Context context) {
        return Glszl_UserDataAdapter.LoadPref(PrefKey.CHAIN_TYPE_TEMP, context);
    }

    public static void SaveZipper(Context context, int i) {
        String str = PrefKey.SELECTED_ZIPPER;
        Glszl_UserDataAdapter.SavePref(str, i + "", context);
    }

    public static void SaveZipperTemp(Context context, int i) {
        String str = PrefKey.SELECTED_ZIPPER_TEMP;
        Glszl_UserDataAdapter.SavePref(str, i + "", context);
    }

    public static void SaveChainTemp(Context context, int i) {
        String str = PrefKey.SELECTED_CHAIN_TEMP;
        Glszl_UserDataAdapter.SavePref(str, i + "", context);
    }

    public static void SaveChainTypeTemp(Context context, int i) {
        String str = PrefKey.CHAIN_TYPE_TEMP;
        Glszl_UserDataAdapter.SavePref(str, i + "", context);
    }

    public static void SaveFontTemp(Context context, int i) {
        String str = PrefKey.SELECTED_FONT_TEMP;
        Glszl_UserDataAdapter.SavePref(str, i + "", context);
    }

    public static void SaveWallpaperTemp(Context context, int i) {
        String str = PrefKey.SELECTED_WALLPAPER_TEMP;
        Glszl_UserDataAdapter.SavePref(str, i + "", context);
    }

    public static void SaveWallpaperBgTemp(Context context, int i) {
        String str = PrefKey.SELECTED_WALLPAPER_BG_TEMP;
        Glszl_UserDataAdapter.SavePref(str, i + "", context);
    }

    public static int getTotalCoinsTemp(Context context) {
        int wallpaperIdsTemp = getSelectedWallpaperNumber(context);
        int wallpaperBgIdsTemp = getSelectedWallpaperBgNumber(context);
        int wallpaperCoinsTemp = getWallpaperCoinsTemp(context);
        int wallpaperBgCoinsTemp = getWallpaperBgCoinsTemp(context);
        if(wallpaperIdsTemp == wallpaperBgIdsTemp){
            return wallpaperBgCoinsTemp;
        } else {
            return wallpaperCoinsTemp + wallpaperBgCoinsTemp;
        }
    }
    public static int getWallpaperCoinsTemp(Context context) {
        return Glszl_UserDataAdapter.LoadPref(PrefKey.WALLPAPER_COINS_TEMP, context);
    }

    public static void SaveWallpaperCoinsTemp(Context context, int i) {
        String str = PrefKey.WALLPAPER_COINS_TEMP;
        Glszl_UserDataAdapter.SavePref(str, i + "", context);
    }

    public static int getWallpaperBgCoinsTemp(Context context) {
        return Glszl_UserDataAdapter.LoadPref(PrefKey.WALLPAPER_BG_COIN_TEMP, context);
    }

    public static void SaveWallpaperBgCoinsTemp(Context context, int i) {
        String str = PrefKey.WALLPAPER_BG_COIN_TEMP;
        Glszl_UserDataAdapter.SavePref(str, i + "", context);
    }

    public static int getZipperTemp(Context context) {
        int LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_ZIPPER_TEMP, context);
        if (LoadPref == 0) {
            return -1;
        }
        return LoadPref;
    }

    public static int getChainTemp(Context context) {
        int LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_CHAIN_TEMP, context);
        if (LoadPref == 0) {
            return -1;
        }
        return LoadPref;
    }

    public static int getFontTemp(Context context) {
        int LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_FONT_TEMP, context);
        if (LoadPref == 0) {
            return -1;
        }
        return LoadPref;
    }

    public static int getWallpaperTemp(Context context) {
        int LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_WALLPAPER_TEMP, context);
        if (LoadPref == 0) {
            return -1;
        }
        return LoadPref;
    }

    public static int getWallpaperBgTemp(Context context) {
        int LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_WALLPAPER_BG_TEMP, context);
        if (LoadPref == 0) {
            return -1;
        }
        return LoadPref;
    }

    public static void SaveChain(Context context, int i) {
        String str = PrefKey.SELECTED_CHAIN;
        Glszl_UserDataAdapter.SavePref(str, i + "", context);
    }

    public static void SaveChainType(Context context, int i) {
        String str = PrefKey.CHAIN_TYPE;
        Glszl_UserDataAdapter.SavePref(str, i + "", context);
    }

    public static String GetSelectedPhont(Context context) {
        int selectedFontNumber = getSelectedFontNumber(context);
        if (selectedFontNumber == 1) {
            return "A_Valentine_Story.ttf";
        }
        if (selectedFontNumber == 2) {
            return "Abraham.ttf";
        }
        if (selectedFontNumber == 3) {
            return "Almonte_Woodgrain.ttf";
        }
        if (selectedFontNumber == 4) {
            return "Aluna.ttf";
        }
        if (selectedFontNumber == 5) {
            return "android_7.ttf";
        }
        if (selectedFontNumber == 6) {
            return "Arizonia_Regular.ttf";
        }
        if (selectedFontNumber == 7) {
            return "ArnoProRegular.ttf";
        }
        if (selectedFontNumber == 8) {
            return "Auttie.ttf";
        }
        if (selectedFontNumber == 9) {
            return "Balloon_Pops.ttf";
        }
        if (selectedFontNumber == 10) {
            return "Baratta.ttf";
        }
        if (selectedFontNumber == 11) {
            return "Blue_highway_bd.ttf";
        }
        if (selectedFontNumber == 12) {
            return "Burnstown_Dam.ttf";
        }
        if (selectedFontNumber == 13) {
            return "Click_Medium_Stroked.ttf";
        }
        if (selectedFontNumber == 14) {
            return "Crack_Man_Front.ttf";
        }
        if (selectedFontNumber == 15) {
            return "Foo.ttf";
        }
        if (selectedFontNumber == 16) {
            return "GSTIGNRM.ttf";
        }
        if (selectedFontNumber == 17) {
            return "Maulydia.ttf";
        }
        if (selectedFontNumber == 18) {
            return "Melloner_Happy_Bold.ttf";
        }
        if (selectedFontNumber == 19) {
            return "Mellson.ttf";
        }
        if (selectedFontNumber == 20) {
            return "Montserrat_Regular.ttf";
        }
        if (selectedFontNumber == 21) {
            return "Mystery.ttf";
        }
        if (selectedFontNumber == 22) {
            return "Nasalization.ttf";
        }
        if (selectedFontNumber == 23) {
            return "Neuropol.ttf";
        }
        if (selectedFontNumber == 24) {
            return "Raleway_Light.ttf";
        }
        if (selectedFontNumber == 25) {
            return "Sun_island.ttf";
        }
        return selectedFontNumber == 26 ? "TitilliumText22L003.ttf" : "android_7.ttf";
    }

    public static void SaveFont(Context context, int i) {
        String str = PrefKey.SELECTED_FONT;
        Glszl_UserDataAdapter.SavePref(str, i + "", context);
    }

    public static int getSelectedWallpaperNumber(Context context) {
        boolean isPreviewMode = Glszl_UserDataAdapter.getIsPreview();
        int LoadPref = 0;
        if(isPreviewMode){
            int tempId = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_WALLPAPER_TEMP, context);
            if(tempId == 0 || tempId == -1){
                LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_WALLPAPER, context);
            } else {
                LoadPref = tempId;
            }
        } else {
            LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_WALLPAPER, context);
        }
        if (LoadPref == 0) {
            return 1;
        }
        return LoadPref;
    }

    public static int getSelectedWallpaperBgNumber(Context context) {
        boolean isPreviewMode = Glszl_UserDataAdapter.getIsPreview();
        int LoadPref = 0;
        if(isPreviewMode){
            int tempId = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_WALLPAPER_BG_TEMP, context);
            if(tempId == 0 || tempId == -1){
                LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_WALLPAPER_BG, context);
            } else {
                LoadPref = tempId;
            }
        } else {
            LoadPref = Glszl_UserDataAdapter.LoadPref(PrefKey.SELECTED_WALLPAPER_BG, context);
        }
        if (LoadPref == 0) {
            return 1;
        }
        return LoadPref;
    }

    public static boolean isShowBackground(Context context) {
        return Glszl_UserDataAdapter.LoadPrefDefault1(PrefKey.SHOW_BACKGROUND, context) != 0;
    }

    public static void setIsShowBackground(Context context, boolean isShow) {
        Glszl_UserDataAdapter.SavePref(PrefKey.SHOW_BACKGROUND, isShow ? "1" : "0", context);
    }

    public static void SaveWallpaper(Context context, int i) {
        String str = PrefKey.SELECTED_WALLPAPER;
        Glszl_UserDataAdapter.SavePref(str, i + "", context);
    }

    public static void SaveWallpaperBg(Context context, int i) {
        String str = PrefKey.SELECTED_WALLPAPER_BG;
        Glszl_UserDataAdapter.SavePref(str, i + "", context);
    }


    public static boolean IsSoundActive(Context context) {
        return Glszl_UserDataAdapter.LoadPref(PrefKey.SOUND_ACTIVE, context) != 0;
    }

    public static boolean IsVibrateActive(Context context) {
        return Glszl_UserDataAdapter.LoadPref(PrefKey.VIBRATION_ACTIVE, context) != 0;
    }
}
