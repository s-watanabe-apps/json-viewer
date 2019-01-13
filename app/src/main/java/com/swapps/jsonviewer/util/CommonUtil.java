package com.swapps.jsonviewer.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommonUtil {
    /**
     * SDカードのfilesディレクトリパスのリストを取得する
     * Android5.0以上対応
     *
     * @param context
     * @return SDカードのfilesディレクトリパスのリスト
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static String getSdCardFilesDirPathListForLollipop(Context context) {
        List<String> sdCardFilesDirPathList = new ArrayList<>();

        // getExternalFilesDirsはAndroid4.4から利用できるAPI
        // filesディレクトリのリストを取得できる
        File[] dirArr = context.getExternalFilesDirs(null);

        for (File dir : dirArr) {
            if (dir != null) {
                String path = dir.getAbsolutePath();

                // isExternalStorageRemovableはAndroid5.0から利用できるAPI
                // 取り外し可能かどうか（SDカードかどうか）を判定している
                if (Environment.isExternalStorageRemovable(dir)) {

                    // 取り外し可能であればSDカード
                    if (!sdCardFilesDirPathList.contains(path)) {
                        String[] paths = path.replaceFirst("^/+", "").split("/");
                        if(paths.length > 1) {
                            return String.format("/%s/%s/", paths[0], paths[1]);
                        }
                    }

                } else {
                    // 取り外し不可能であれば内部ストレージ
                }
            }
        }

        return null;
    }

}
