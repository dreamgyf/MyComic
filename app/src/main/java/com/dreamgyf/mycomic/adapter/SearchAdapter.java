package com.dreamgyf.mycomic.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dreamgyf.mycomic.ComicItemActivity;
import com.dreamgyf.mycomic.MainActivity;
import com.dreamgyf.mycomic.R;
import com.dreamgyf.mycomic.entity.SearchResultEntity;

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
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> implements View.OnClickListener {

    private Handler handler = new Handler();

    private TextView count;

    private List<SearchResultEntity> searchResultEntityList = new ArrayList<>();

    private RecyclerView recyclerView;

    private OnItemClickListener onItemClickListener = null;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        private TextView title;

        private TextView author;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
            author = itemView.findViewById(R.id.author);
        }
    }

    public SearchAdapter(final TextView textView) {
        count = textView;
        onItemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(final RecyclerView recyclerView, View view, int position, final SearchResultEntity result) {
                Intent intent = new Intent(textView.getContext(), ComicItemActivity.class);
                intent.putExtra("url","https://www.manhuadb.com" + result.getHref());
                textView.getContext().startActivity(intent);
            }
        };
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleview_search_result,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResultEntity searchResultEntity = searchResultEntityList.get(position);
        holder.imageView.setImageBitmap(searchResultEntity.getImage());
        holder.title.setText(searchResultEntity.getTitle());
        holder.author.setText(searchResultEntity.getAuthor());
    }

    @Override
    public int getItemCount() {
        return searchResultEntityList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    public interface OnItemClickListener{
        void onItemClick(RecyclerView recyclerView,View view,int position,SearchResultEntity result);
    }

    public void addOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onClick(View view) {
        int position = recyclerView.getChildAdapterPosition(view);
        if(onItemClickListener != null)
            onItemClickListener.onItemClick(recyclerView,view,position,searchResultEntityList.get(position));
    }

    public void search(final String keyword){
        count.setText("");
        searchResultEntityList.clear();
        Runnable searchThread = new Runnable() {
            @Override
            public void run() {
                String html = null;
                try {
                    URL url = new URL("https://www.manhuadb.com/search?q=" + keyword);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    if(httpURLConnection.getResponseCode() == 200){
                        InputStream in = httpURLConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuffer sb = new StringBuffer();
                        String temp;
                        while((temp = reader.readLine()) != null){
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
                    final String countText = document.getElementsByClass("text-muted").get(0).text();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            count.setText(countText);
                        }
                    });
                    Element mainDiv = document.getElementsByClass("comic-main-section").get(0);
                    Elements item = mainDiv.getElementsByClass("row");
                    Elements comicDiv;
                    if(item.isEmpty())
                        comicDiv = new Elements();
                    else
                        comicDiv = item.get(0).children();
                    for(Element comic : comicDiv){
                        Elements elements = comic.getElementsByTag("a");
                        final SearchResultEntity entity = new SearchResultEntity();
                        entity.setHref(elements.get(0).attr("href"));
                        final String imgUrl = elements.get(0).getElementsByTag("img").get(0).attr("src");
                        Runnable loadingImgThread = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    URL url = new URL(imgUrl);
                                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                                    httpURLConnection.setRequestMethod("GET");
                                    if(httpURLConnection.getResponseCode() == 200){
                                        InputStream in = httpURLConnection.getInputStream();
                                        Bitmap bitmap = BitmapFactory.decodeStream(in);
                                        entity.setImage(bitmap);
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
                        entity.setTitle(elements.get(1).text());
                        entity.setAuthor(elements.get(2).text());
                        searchResultEntityList.add(entity);
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                }
            }
        };

        MainActivity.executor.execute(searchThread);
    }
}
