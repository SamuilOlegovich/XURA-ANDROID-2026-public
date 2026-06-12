package com.samuilolegovich.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;

// Три независимых метода: build-теги, su-бинарники, пакеты root-менеджеров.
// Продвинутые инструменты (Magisk Hide) могут обойти файловые проверки,
// но проверка пакетов и build-тегов остаётся рабочей.
public class RootDetector {

    private static final String[] SU_PATHS = {
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/su",
            "/system/app/Superuser.apk",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su",
            "/system/bin/failsafe/su",
            "/dev/com.koushikdutta.superuser.daemon"
    };

    private static final String[] ROOT_PACKAGES = {
            "com.topjohnwu.magisk",         // Magisk
            "eu.chainfire.supersu",          // SuperSU
            "com.noshufou.android.su",       // Superuser
            "com.koushikdutta.superuser",    // CWM Superuser
            "com.kingroot.kinguser",         // KingRoot
            "com.kingo.root",               // KingoRoot
            "com.smedialink.oneclickroot",  // One Click Root
            "com.alephzain.framaroot",      // FramaRoot
            "com.formyhm.hideroot"          // HideRoot
    };

    public static boolean isRooted(Context context) {
        return checkBuildTags()
                || checkSuBinaries()
                || checkRootPackages(context);
    }

    // Устройства с test-keys подписью — неофициальная прошивка
    private static boolean checkBuildTags() {
        String tags = Build.TAGS;
        return tags != null && tags.contains("test-keys");
    }

    // Наличие su в стандартных расположениях
    private static boolean checkSuBinaries() {
        for (String path : SU_PATHS) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    // Установленные пакеты root-менеджеров
    private static boolean checkRootPackages(Context context) {
        PackageManager pm = context.getPackageManager();
        for (String pkg : ROOT_PACKAGES) {
            try {
                pm.getPackageInfo(pkg, 0);
                return true;
            } catch (PackageManager.NameNotFoundException ignored) {}
        }
        return false;
    }
}
