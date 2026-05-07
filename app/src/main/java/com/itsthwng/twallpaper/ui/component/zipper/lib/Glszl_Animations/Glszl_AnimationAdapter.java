package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Animations;

import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_Timer;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_ULabel;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_UTriangle;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_Uimage;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_UimagePart;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_Urect;

import java.util.ArrayList;

public class Glszl_AnimationAdapter {
    public static void Init() {
        Glszl_Deplace.heart_zipper_list = new ArrayList();
        Glszl_Fade.heart_zipper_list = new ArrayList();
        Glszl_Sizer.heart_zipper_list = new ArrayList();
        Glszl_FadeInOut.heart_zipper_list = new ArrayList();
        Glszl_Rotation.heart_zipper_list = new ArrayList();
        Glszl_Timer.heart_zipper_list = new ArrayList();
    }

    public static void Update() {
        Glszl_Deplace.update();
        Glszl_Fade.update();
        Glszl_Sizer.update();
        Glszl_FadeInOut.update();
        Glszl_Rotation.update();
    }

    public static void CleareMemory() {
        if (Glszl_Deplace.heart_zipper_list != null) {
            Glszl_Deplace.heart_zipper_list.clear();
            Glszl_Deplace.heart_zipper_list = null;
        }
        if (Glszl_Timer.heart_zipper_list != null) {
            Glszl_Timer.heart_zipper_list.clear();
            Glszl_Timer.heart_zipper_list = null;
        }
        if (Glszl_Fade.heart_zipper_list != null) {
            Glszl_Fade.heart_zipper_list.clear();
            Glszl_Fade.heart_zipper_list = null;
        }
        if (Glszl_Sizer.heart_zipper_list != null) {
            Glszl_Sizer.heart_zipper_list.clear();
            Glszl_Sizer.heart_zipper_list = null;
        }
        if (Glszl_FadeInOut.heart_zipper_list != null) {
            Glszl_FadeInOut.heart_zipper_list.clear();
            Glszl_FadeInOut.heart_zipper_list = null;
        }
        if (Glszl_Rotation.heart_zipper_list != null) {
            Glszl_Rotation.heart_zipper_list.clear();
            Glszl_Rotation.heart_zipper_list = null;
        }
        if (Glszl_Urect.list != null) {
            Glszl_Urect.list.clear();
            Glszl_Urect.list = null;
        }
        if (Glszl_Uimage.list != null) {
            Glszl_Uimage.list.clear();
            Glszl_Uimage.list = null;
        }
        if (Glszl_UimagePart.list != null) {
            Glszl_UimagePart.list.clear();
            Glszl_UimagePart.list = null;
        }
        if (Glszl_ULabel.list != null) {
            Glszl_ULabel.list.clear();
            Glszl_ULabel.list = null;
        }
        if (Glszl_UTriangle.list != null) {
            Glszl_UTriangle.list.clear();
            Glszl_UTriangle.list = null;
        }
    }
}
