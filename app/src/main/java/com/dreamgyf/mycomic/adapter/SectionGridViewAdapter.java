package com.dreamgyf.mycomic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dreamgyf.mycomic.R;
import com.dreamgyf.mycomic.entity.ComicTab;

public class SectionGridViewAdapter extends BaseAdapter {

    private ComicTab comicTab;

    private TextView textView;

    public SectionGridViewAdapter(ComicTab comicTab) {
        super();
        this.comicTab = comicTab;
    }

    @Override
    public int getCount() {
        return comicTab.getSections().size();
    }

    @Override
    public Object getItem(int i) {
        return comicTab.getSections().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.gridview_section,viewGroup,false);
            textView = view.findViewById(R.id.section);
            view.setTag(textView);
        }
        else {
            textView = (TextView) view.getTag();
        }
        textView.setText(comicTab.getSections().get(i).getName());

        return view;
    }
}
