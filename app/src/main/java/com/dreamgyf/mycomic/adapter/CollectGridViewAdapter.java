package com.dreamgyf.mycomic.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.dreamgyf.mycomic.ComicContentActivity;
import com.dreamgyf.mycomic.MainActivity;
import com.dreamgyf.mycomic.R;
import com.dreamgyf.mycomic.entity.ComicContent;
import com.dreamgyf.mycomic.entity.ComicInfo;
import com.dreamgyf.mycomic.entity.ComicTab;
import com.dreamgyf.mycomic.utils.BeanUtils;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CollectGridViewAdapter extends BaseAdapter {

    private Context context;

    private Handler handler = new Handler();

    private ViewHolder viewHolder;

    private ArrayList<ComicInfo> comicInfoList;

    private class ViewHolder {
        ImageView image;
        TextView title;
        TextView author;
    }

    public CollectGridViewAdapter(Context context) {
        super();
        this.context = context;
        comicInfoList = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences("comic",Context.MODE_PRIVATE);
        String collectListBase64 = sharedPreferences.getString("collectList",null);
        if(collectListBase64 != null){
            try {
                comicInfoList = BeanUtils.decodeBean(collectListBase64,ArrayList.class);
                for(int i = 0;i < comicInfoList.size();i++){
                    final int pos = i;
                    Runnable loadingImgThread = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                URL url = new URL(comicInfoList.get(pos).getImageUrl());
                                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                                httpURLConnection.setRequestMethod("GET");
                                if(httpURLConnection.getResponseCode() == 200){
                                    InputStream in = httpURLConnection.getInputStream();
                                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                                    comicInfoList.get(pos).setImage(bitmap);
                                    httpURLConnection.disconnect();
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            notifyDataSetChanged();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    MainActivity.executor.execute(loadingImgThread);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCount() {
        return comicInfoList.size();
    }

    @Override
    public Object getItem(int i) {
        return comicInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.gridview_collect,viewGroup,false);
            viewHolder.image = view.findViewById(R.id.image);
            viewHolder.title = view.findViewById(R.id.title);
            viewHolder.author = view.findViewById(R.id.author);
            view.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.image.setImageBitmap(comicInfoList.get(i).getImage());
        viewHolder.title.setText(comicInfoList.get(i).getTitle());
        viewHolder.author.setText(comicInfoList.get(i).getAuthor());

        return view;
    }

    public ArrayList<ComicInfo> getComicInfoList() {
        return comicInfoList;
    }

    public void refresh(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("comic",Context.MODE_PRIVATE);
        String collectListBase64 = sharedPreferences.getString("collectList",null);
        if(collectListBase64 != null) {
            try {
                ArrayList<ComicInfo> tempComicInfoList = BeanUtils.decodeBean(collectListBase64, ArrayList.class);
                for (int i = 0; i < comicInfoList.size(); i++) {
                    if(tempComicInfoList.isEmpty()){
                        comicInfoList.clear();
                        break;
                    }
                    for (int j = 0; j < tempComicInfoList.size(); j++) {
                        if (comicInfoList.get(i).getHref().equals(tempComicInfoList.get(j).getHref())) {
                            tempComicInfoList.remove(j);
                            break;
                        } else if (j == tempComicInfoList.size() - 1) {
                            comicInfoList.remove(i);
                            i--;
                        }
                    }
                }
                for(int i = tempComicInfoList.size() - 1;i >= 0;i--){
                    comicInfoList.add(0,tempComicInfoList.get(i));
                }
                notifyDataSetChanged();
                for (final ComicInfo tempComicInfo : tempComicInfoList) {
                    Runnable loadingImgThread = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                URL url = new URL(tempComicInfo.getImageUrl());
                                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                                httpURLConnection.setRequestMethod("GET");
                                if (httpURLConnection.getResponseCode() == 200) {
                                    InputStream in = httpURLConnection.getInputStream();
                                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                                    tempComicInfo.setImage(bitmap);
                                    httpURLConnection.disconnect();
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            notifyDataSetChanged();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    MainActivity.executor.execute(loadingImgThread);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            comicInfoList.clear();
            notifyDataSetChanged();
        }
    }
}
