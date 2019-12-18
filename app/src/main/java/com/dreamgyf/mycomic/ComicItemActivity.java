package com.dreamgyf.mycomic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.dreamgyf.mycomic.adapter.SectionGridViewAdapter;
import com.dreamgyf.mycomic.adapter.TabViewPagerAdapter;
import com.dreamgyf.mycomic.entity.ComicEntity;
import com.dreamgyf.mycomic.entity.ComicTab;
import com.dreamgyf.mycomic.entity.Section;
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

    private ComicEntity comic;

    private Toolbar toolbar;

    private TextView description;

    private TabLayout tabLayout;

    private ViewPager viewPager;

    private List<View> viewList = new ArrayList<>();

    private List<String> titleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_item);

        comic = new ComicEntity();

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        description = findViewById(R.id.description);

        final String url = getIntent().getStringExtra("url");
        int pos = url.lastIndexOf("/");
        comic.setId(url.substring(pos + 1));

        new Thread(new Runnable() {
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
                }).start();
    }
}
