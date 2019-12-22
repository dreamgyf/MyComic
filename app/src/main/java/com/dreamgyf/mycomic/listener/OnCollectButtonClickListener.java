package com.dreamgyf.mycomic.listener;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dreamgyf.mycomic.ComicItemActivity;
import com.dreamgyf.mycomic.R;
import com.dreamgyf.mycomic.entity.ComicInfo;
import com.dreamgyf.mycomic.utils.SharedPreferencesUtils;

import java.util.ArrayList;

public class OnCollectButtonClickListener implements View.OnClickListener {

    private Context context;

    private ComicInfo comicInfo;

    private ImageView collectImage;

    private TextView collectText;

    public OnCollectButtonClickListener(Context context, ComicInfo comicInfo, ImageView collectImage, TextView collectText){
        this.context = context;
        this.comicInfo = comicInfo;
        this.collectImage = collectImage;
        this.collectText = collectText;
    }

    @Override
    public void onClick(View view) {
        try {
            ArrayList<ComicInfo> comicInfoList = SharedPreferencesUtils.getComicInfoList(context);
            if(comicInfoList != null) {
                for(int i = 0;i < comicInfoList.size();i++){
                    if(comicInfoList.get(i).getHref().equals(comicInfo.getHref())){
                        comicInfoList.remove(i);
                        SharedPreferencesUtils.setComicInfoList(context,comicInfoList);
                        collectImage.setImageResource(R.drawable.ic_not_collect);
                        collectText.setText("收藏");
                        Toast toast = Toast.makeText(context,"",Toast.LENGTH_SHORT);
                        toast.setText("再见了您呐~");
                        toast.show();
                        return;
                    }
                }
                comicInfoList.add(0,comicInfo);
                SharedPreferencesUtils.setComicInfoList(context,comicInfoList);
                collectImage.setImageResource(R.drawable.ic_is_collect);
                collectText.setText("已收藏");
                Toast toast = Toast.makeText(context,"",Toast.LENGTH_SHORT);
                toast.setText("我来啦~");
                toast.show();
            }
            else {
                comicInfoList = new ArrayList<>();
                comicInfoList.add(0,comicInfo);
                SharedPreferencesUtils.setComicInfoList(context,comicInfoList);
                collectImage.setImageResource(R.drawable.ic_is_collect);
                collectText.setText("已收藏");
                Toast toast = Toast.makeText(context,"",Toast.LENGTH_SHORT);
                toast.setText("我来啦~");
                toast.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
