package com.dreamgyf.mycomic.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.dreamgyf.mycomic.entity.ComicInfo;

import java.util.ArrayList;

public class SharedPreferencesUtils {

    public static ArrayList<ComicInfo> getComicInfoList(Context context) throws Exception {
        SharedPreferences sharedPreferences = context.getSharedPreferences("comic",Context.MODE_PRIVATE);
        String collectListBase64 = sharedPreferences.getString("collectList",null);
        return collectListBase64 != null ? BeanUtils.decodeBean(collectListBase64, ArrayList.class) : null;
    }

    public static void setComicInfoList(Context context,ArrayList<ComicInfo> comicInfoList) throws Exception {
        SharedPreferences sharedPreferences = context.getSharedPreferences("comic", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("collectList", BeanUtils.encodeBean(comicInfoList));
        editor.apply();
    }
}
