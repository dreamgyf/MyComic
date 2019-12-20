package com.dreamgyf.mycomic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dreamgyf.mycomic.adapter.SectionGridViewAdapter;
import com.dreamgyf.mycomic.adapter.TabViewPagerAdapter;
import com.dreamgyf.mycomic.entity.ComicDetail;
import com.dreamgyf.mycomic.entity.ComicInfo;
import com.dreamgyf.mycomic.entity.ComicTab;
import com.dreamgyf.mycomic.entity.Section;
import com.dreamgyf.mycomic.utils.BeanUtils;
import com.dreamgyf.mycomic.utils.SharedPreferencesUtils;
import com.google.android.material.tabs.TabLayout;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ComicItemActivity extends AppCompatActivity {

    private Handler handler = new Handler();

    private ComicInfo comicInfo;

    private ComicDetail comic;

    private Toolbar toolbar;

    private TextView description;

    private TabLayout tabLayout;

    private ViewPager viewPager;

    private List<View> viewList = new ArrayList<>();

    private List<String> titleList = new ArrayList<>();

    private LinearLayout collectButton;

    private ImageView collectImage;

    private TextView collectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_item);

        comic = new ComicDetail();

        toolbar = findViewById(R.id.toolbar);
        collectButton = findViewById(R.id.collect_button);
        collectImage = findViewById(R.id.collect_image);
        collectText = findViewById(R.id.collect_text);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        description = findViewById(R.id.description);

        comicInfo = (ComicInfo) getIntent().getSerializableExtra("comicInfo");
        final String url = "https://www.manhuadb.com" + comicInfo.getHref();
        int pos = url.lastIndexOf("/");
        comic.setId(url.substring(pos + 1));

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


        collectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ArrayList<ComicInfo> comicInfoList = SharedPreferencesUtils.getComicInfoList(ComicItemActivity.this);
                    if(comicInfoList != null) {
                        for(int i = 0;i < comicInfoList.size();i++){
                            if(comicInfoList.get(i).getHref().equals(comicInfo.getHref())){
                                comicInfoList.remove(i);
                                SharedPreferencesUtils.setComicInfoList(ComicItemActivity.this,comicInfoList);
                                collectImage.setImageResource(R.drawable.ic_not_collect);
                                collectText.setText("收藏");
                                Toast toast = Toast.makeText(ComicItemActivity.this,"",Toast.LENGTH_SHORT);
                                toast.setText("再见了您呐~");
                                toast.show();
                                return;
                            }
                        }
                        comicInfoList.add(0,comicInfo);
                        SharedPreferencesUtils.setComicInfoList(ComicItemActivity.this,comicInfoList);
                        collectImage.setImageResource(R.drawable.ic_is_collect);
                        collectText.setText("已收藏");
                        Toast toast = Toast.makeText(ComicItemActivity.this,"",Toast.LENGTH_SHORT);
                        toast.setText("我来啦~");
                        toast.show();
                    }
                    else {
                        comicInfoList = new ArrayList<>();
                        comicInfoList.add(0,comicInfo);
                        SharedPreferencesUtils.setComicInfoList(ComicItemActivity.this,comicInfoList);
                        collectImage.setImageResource(R.drawable.ic_is_collect);
                        collectText.setText("已收藏");
                        Toast toast = Toast.makeText(ComicItemActivity.this,"",Toast.LENGTH_SHORT);
                        toast.setText("我来啦~");
                        toast.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Runnable getDataThread = new Runnable() {
            @Override
            public void run() {
                String html = null;
                try {
                    HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
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
                if(html != null) {
                    Document document = Jsoup.parse(html);
                    final String title = document.getElementsByClass("comic-title").text();
                    comic.setTitle(title);
                    final String description = document.getElementsByClass("comic_story").text();
                    comic.setDescription(description);
                    Elements tabs = document.getElementById("myTab").getElementsByTag("a");
                    List<ComicTab> comicTabList = new ArrayList<>();
                    for(Element tab : tabs){
                        ComicTab comicTab = new ComicTab();
                        comicTab.setId(tab.attr("aria-controls"));
                        comicTab.setName(tab.child(0).text());
                        Element bookList = document.getElementById("comic-book-list");
                        Element ol = bookList.getElementById(comicTab.getId()).getElementsByTag("ol").get(0);
                        Elements sections = ol.getElementsByTag("a");
                        LinkedList<Section> sectionList = new LinkedList<>();
                        for(Element sectionA : sections){
                            Section section = new Section();
                            section.setName(sectionA.text());
                            section.setHref(sectionA.attr("href"));
                            sectionList.add(section);
                        }
                        comicTab.setSections(sectionList);
                        comicTabList.add(comicTab);
                    }
                    comic.setTabs(comicTabList);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            toolbar.setTitle(title);
                            ComicItemActivity.this.description.setText(description);
                            tabLayout = findViewById(R.id.tabLayout);
                            viewPager = findViewById(R.id.viewpager);
                            for(final ComicTab tab : comic.getTabs()){
                                //针对每个标签绘制标签页
                                View view = LayoutInflater.from(ComicItemActivity.this).inflate(R.layout.viewpager_tab,null);
                                GridView gridView = view.findViewById(R.id.gridview);
                                SectionGridViewAdapter sectionGridViewAdapter = new SectionGridViewAdapter(tab);
                                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        Intent intent = new Intent(ComicItemActivity.this,ComicContentActivity.class);
                                        intent.putExtra("comicTab",tab);
                                        intent.putExtra("position",i);
                                        startActivity(intent);
                                    }
                                });
                                gridView.setAdapter(sectionGridViewAdapter);
                                viewList.add(view);
                                titleList.add(tab.getName());
                            }
                            tabLayout.setupWithViewPager(viewPager);
                            viewPager.setAdapter(new TabViewPagerAdapter(titleList, viewList));
                        }
                    });
                }
            }
        };
        MainActivity.executor.execute(getDataThread);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
