package com.dreamgyf.mycomic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.dreamgyf.mycomic.adapter.ComicContentViewPagerAdapter;
import com.dreamgyf.mycomic.adapter.SectionGridViewAdapter;
import com.dreamgyf.mycomic.entity.ComicContent;
import com.dreamgyf.mycomic.entity.ComicTab;
import com.dreamgyf.mycomic.entity.Section;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ComicContentActivity extends AppCompatActivity {

    private Handler handler = new Handler();

    private ComicTab comicTab;

    private Section section;

    private int position;

    private List<ComicContent> comicContentList;

    private ViewPager viewPager;

    private List<View> viewList = new ArrayList<>();

    private BottomSheetDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_comic_content);

        comicTab = (ComicTab) getIntent().getSerializableExtra("comicTab");
        position = getIntent().getIntExtra("position",-1);
        section = position == -1 ? null : comicTab.getSections().get(position);
        viewPager = findViewById(R.id.viewpager);

        initBottomDialog();
        //获取数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                String html = null;
                try {
                    HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("https://www.manhuadb.com" + section.getHref()).openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    if (httpURLConnection.getResponseCode() == 200) {
                        InputStream in = httpURLConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuffer sb = new StringBuffer();
                        String temp;
                        while ((temp = reader.readLine()) != null) {
                            sb.append(temp);
                        }
                        html = sb.toString();
                        httpURLConnection.disconnect();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(html != null){
                    Document document = Jsoup.parse(html);
                    final Elements scripts = document.getElementsByTag("script");
                    String base64 = null;
                    for(final Element script : scripts){
                        String str = script.data();
                        if(str.contains("img_data")){
                            base64 = str.substring(str.indexOf("'") + 1,str.lastIndexOf("'"));
                            break;
                        }
                    }
                    byte[] decode =Base64.decode(base64,Base64.DEFAULT);
                    String imgData = new String(decode);
                    Gson gson = new Gson();
                    comicContentList = gson.fromJson(imgData, new TypeToken<List<ComicContent>>(){}.getType());
                    Element data = document.getElementsByClass("vg-r-data").get(0);
                    String headUrl = data.attr("data-host") + data.attr("data-img_pre");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            for(ComicContent comicContent : comicContentList){
                                View view = LayoutInflater.from(ComicContentActivity.this).inflate(R.layout.viewpager_content,null);
                                view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.show();
                                    }
                                });
                                viewList.add(view);
                            }
                            viewPager.setAdapter(new ComicContentViewPagerAdapter(viewList));
                        }
                    });
                    for(int i = 0;i < comicContentList.size();i++){
                        try {
                            URL url = new URL(headUrl + "/" + comicContentList.get(i).getImg());
                            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                            httpURLConnection.setRequestMethod("GET");
                            if(httpURLConnection.getResponseCode() == 200){
                                InputStream in = httpURLConnection.getInputStream();
                                final Bitmap bitmap = BitmapFactory.decodeStream(in);
                                final int pos = i;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ImageView imageView = viewList.get(pos).findViewById(R.id.image);
                                        imageView.setImageBitmap(bitmap);
                                    }
                                });
                                httpURLConnection.disconnect();
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (ProtocolException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    void initBottomDialog(){
        dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_content,null);
        dialog.setContentView(view);
        ((View) view.getParent()).getBackground().setAlpha(100);
    }
}
