package com.dreamgyf.mycomic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ComicItemActivity extends AppCompatActivity {

    private Handler handler = new Handler();

    private Toolbar toolbar;

    private TextView description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_item);

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        description = findViewById(R.id.description);

        final String url = getIntent().getStringExtra("url");

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
                            final String description = document.getElementsByClass("comic_story").text();
                            Elements tabs = document.getElementById("myTab").getElementsByTag("a");
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    toolbar.setTitle(title);
                                    ComicItemActivity.this.description.setText(description);
                                }
                            });
                        }
                    }
                }).start();
    }
}
