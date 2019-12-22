package com.dreamgyf.mycomic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.dreamgyf.mycomic.adapter.CollectGridViewAdapter;
import com.dreamgyf.mycomic.adapter.HomeViewPagerAdapter;
import com.dreamgyf.mycomic.entity.ComicInfo;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ImageView searchButton;

    private ExecutorService executor = Executors.newFixedThreadPool(10);

    private List<String> titleList = new ArrayList<>();

    private List<View> viewList = new ArrayList<>();

    private ViewPager viewPager;

    private HomeViewPagerAdapter homeViewPagerAdapter;

    private CollectGridViewAdapter collectGridViewAdapter;

    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        initViewPager();
    }

    private void initViewPager(){
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewpager);
        tabLayout.setupWithViewPager(viewPager);
        //收藏页面
        View view = LayoutInflater.from(this).inflate(R.layout.viewpager_collect,null);
        GridView gridView = view.findViewById(R.id.gridview);
        collectGridViewAdapter = new CollectGridViewAdapter(this);
        gridView.setAdapter(collectGridViewAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ComicInfo comicInfo = ((CollectGridViewAdapter) adapterView.getAdapter()).getComicInfoList().get(i);
                Intent intent = new Intent(MainActivity.this, ComicItemActivity.class);
                intent.putExtra("comicInfo",comicInfo);
                startActivity(intent);
            }
        });
        viewList.add(view);
        titleList.add("收藏");
        homeViewPagerAdapter = new HomeViewPagerAdapter(titleList,viewList);
        viewPager.setAdapter(homeViewPagerAdapter);
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    @Override
    protected void onResume() {
        super.onResume();
        collectGridViewAdapter.refresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
