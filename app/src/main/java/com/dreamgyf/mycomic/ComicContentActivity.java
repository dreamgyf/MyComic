package com.dreamgyf.mycomic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dreamgyf.mycomic.adapter.ComicContentViewPagerAdapter;
import com.dreamgyf.mycomic.adapter.SectionGridViewAdapter;
import com.dreamgyf.mycomic.entity.ComicContent;
import com.dreamgyf.mycomic.entity.ComicInfo;
import com.dreamgyf.mycomic.entity.ComicTab;
import com.dreamgyf.mycomic.entity.Section;
import com.dreamgyf.mycomic.listener.OnCollectButtonClickListener;
import com.dreamgyf.mycomic.utils.Pair;
import com.dreamgyf.mycomic.utils.SharedPreferencesUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComicContentActivity extends AppCompatActivity {

    private ExecutorService executor = Executors.newFixedThreadPool(30);

    private Handler handler = new Handler();

    private ComicInfo comicInfo;

    private ComicTab comicTab;

    private Section section;

    private int position;

    private int page;

    private List<ComicContent> comicContentList;

    private ViewPager viewPager;

    private List<View> viewList = new ArrayList<>();

    private BottomSheetDialog dialog;

    private TextView sectionText;

    private LinearLayout collectButton;

    private ImageView collectImage;

    private TextView collectText;

    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_comic_content);

        comicInfo = (ComicInfo) getIntent().getSerializableExtra("comicInfo");
        comicTab = (ComicTab) getIntent().getSerializableExtra("comicTab");
        position = getIntent().getIntExtra("position",-1);
        section = position == -1 ? null : comicTab.getSections().get(position);
        page = getIntent().getIntExtra("page",0);
        viewPager = findViewById(R.id.viewpager);

        initBottomDialog();
        //获取数据
        Runnable getDataThread = new Runnable() {
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
                } catch (Exception e) {
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
                    byte[] decode = Base64.decode(base64,Base64.DEFAULT);
                    String imgData = new String(decode);
                    Gson gson = new Gson();
                    comicContentList = gson.fromJson(imgData, new TypeToken<List<ComicContent>>(){}.getType());
                    Element data = document.getElementsByClass("vg-r-data").get(0);
                    final String headUrl = data.attr("data-host") + data.attr("data-img_pre");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            for(ComicContent comicContent : comicContentList){
                                View view = LayoutInflater.from(ComicContentActivity.this).inflate(R.layout.viewpager_content,null);
                                ImageView imageView = view.findViewById(R.id.image);
                                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                imageView.setImageResource(R.drawable.loading);
                                imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.show();
                                    }
                                });
                                ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                                layoutParams.width = ComicContentActivity.this.getResources().getDisplayMetrics().widthPixels / 2;
                                viewList.add(view);
                            }
                            viewPager.setAdapter(new ComicContentViewPagerAdapter(viewList));
                            seekBar.setMax(comicContentList.size() - 1);
                            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                @Override
                                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                                }

                                @Override
                                public void onPageSelected(int position) {
                                    seekBar.setProgress(position);
                                }

                                @Override
                                public void onPageScrollStateChanged(int state) {

                                }
                            });
                            viewPager.setCurrentItem(page,false);
                        }
                    });
                    for(int i = 0;i < comicContentList.size();i++){
                        final int pos = i;
                        Runnable getImgThread = new Runnable() {
                            @Override
                            public void run() {
                                boolean success = false;
                                while(!success){
                                    try {
                                        URL url = new URL(headUrl + "/" + comicContentList.get(pos).getImg());
                                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                                        httpURLConnection.setRequestMethod("GET");
                                        if(httpURLConnection.getResponseCode() == 200){
                                            InputStream in = httpURLConnection.getInputStream();
                                            final Bitmap bitmap = BitmapFactory.decodeStream(in);
                                            if(bitmap != null){
                                                success = true;
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ImageView imageView = viewList.get(pos).findViewById(R.id.image);
                                                        imageView.setScaleType(ImageView.ScaleType.MATRIX);
                                                        imageView.setImageBitmap(bitmap);
                                                        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                                                        layoutParams.width = ComicContentActivity.this.getResources().getDisplayMetrics().widthPixels;
                                                    }
                                                });
                                            }
                                        }
                                        httpURLConnection.disconnect();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        };
                        executor.execute(getImgThread);
                    }
                }
            }
        };
        executor.execute(getDataThread);
    }

    void initBottomDialog(){
        dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_content,null);
        dialog.setContentView(view);
        ((View) view.getParent()).getBackground().setAlpha(100);
        sectionText = view.findViewById(R.id.section);
        collectButton = view.findViewById(R.id.collect_button);
        collectImage = view.findViewById(R.id.collect_image);
        collectText = view.findViewById(R.id.collect_text);
        TextView previous = view.findViewById(R.id.previous);
        TextView next = view.findViewById(R.id.next);
        seekBar = view.findViewById(R.id.seekbar);
        //当前话标题
        sectionText.setText(section.getName());
        //收藏按钮
        //初始化收藏状态
        try {
            List<ComicInfo> comicInfoList = SharedPreferencesUtils.getComicInfoList(this);
            if(comicInfoList != null){
                for(int i = 0;i < comicInfoList.size();i++){
                    if(comicInfoList.get(i).getHref().equals(comicInfo.getHref())){
                        collectImage.setImageResource(R.drawable.ic_is_collect);
                        collectText.setText("已收藏");
                        break;
                    }
                    else if(i == comicInfoList.size() - 1){
                        collectImage.setImageResource(R.drawable.ic_not_collect);
                        collectText.setText("收藏");
                    }
                }
            }
            else {
                collectImage.setImageResource(R.drawable.ic_not_collect);
                collectText.setText("收藏");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //收藏点击事件
        collectButton.setOnClickListener(new OnCollectButtonClickListener(this,comicInfo,collectImage,collectText));
        //上一话
        if(position - 1 >= 0){
            previous.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ComicContentActivity.this,ComicContentActivity.class);
                    intent.putExtra("comicInfo",comicInfo);
                    intent.putExtra("comicTab",comicTab);
                    intent.putExtra("position",position - 1);
                    startActivity(intent);
                    dialog.dismiss();
                    finish();
                    ComicContentActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }
        else {
            previous.setTextColor(Color.GRAY);
        }
        //下一话
        if(position + 1 < comicTab.getSections().size()){
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ComicContentActivity.this,ComicContentActivity.class);
                    intent.putExtra("comicInfo",comicInfo);
                    intent.putExtra("comicTab",comicTab);
                    intent.putExtra("position",position + 1);
                    startActivity(intent);
                    dialog.dismiss();
                    finish();
                    ComicContentActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }
        else {
            next.setTextColor(Color.GRAY);
        }
        //进度条
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b)
                    viewPager.setCurrentItem(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    @Override
    protected void onPause() {
        //保存历史记录
        try {
            Map<String, Map<String, Object>> history = SharedPreferencesUtils.getHistory(this);
            if(history == null)
                history = new HashMap<>();
            Map<String, Object> map = new HashMap<>();
            map.put("comicTab",comicTab);
            map.put("position",position);
            map.put("page",viewPager.getCurrentItem());
            history.put(comicInfo.getHref(),map);
            SharedPreferencesUtils.setHistory(this,history);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        dialog.dismiss();
        executor.shutdownNow();
        super.onDestroy();
    }
}
