package com.dreamgyf.mycomic.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.dreamgyf.mycomic.entity.ComicInfo;
import com.dreamgyf.mycomic.entity.ComicTab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public static Map<String, Pair<ComicTab, Integer>> getHistory(Context context) throws Exception {
        SharedPreferences sharedPreferences = context.getSharedPreferences("comic",Context.MODE_PRIVATE);
        String collectListBase64 = sharedPreferences.getString("history",null);
        return collectListBase64 != null ? BeanUtils.decodeBean(collectListBase64, HashMap.class) : null;
    }

    public static void setHistory(Context context,Map<String, Pair<ComicTab, Integer>> history) throws Exception {
        SharedPreferences sharedPreferences = context.getSharedPreferences("comic", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("history", BeanUtils.encodeBean(history));
        editor.apply();
    }
}
