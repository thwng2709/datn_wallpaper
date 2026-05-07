package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_pinLockView;

import java.util.Random;

public class Glszl_ShuffleArrayUtils {
    static int[] shuffle(int[] iArr) {
        int length = iArr.length;
        Random random = new Random();
        random.nextInt();
        for (int i = 0; i < length; i++) {
            swap(iArr, i, random.nextInt(length - i) + i);
        }
        return iArr;
    }

    private static void swap(int[] iArr, int i, int i2) {
        int i3 = iArr[i];
        iArr[i] = iArr[i2];
        iArr[i2] = i3;
    }
}
