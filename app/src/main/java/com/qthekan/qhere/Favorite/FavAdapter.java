package com.qthekan.qhere.Favorite;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qthekan.qhere.R;
import com.qthekan.qhere.radar.listview.ListViewItem;
import com.qthekan.util.qlog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;


public class FavAdapter extends BaseAdapter
{
    public ArrayList<Data> mItemList = new ArrayList<>();


    public FavAdapter()
    {

    }


    @Override
    public int getCount()
    {
        return mItemList.size() ;
    }


    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_favorite_item, parent, false);
        }

        Data item = mItemList.get(position);

        TextView tvName = convertView.findViewById(R.id.favTvName);
        tvName.setText(item.mName);

        return convertView;
    }


    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }


    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Data getItem(int position) {
        return mItemList.get(position) ;
    }


    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String name, String latlng) {
        Data item = new Data(name, latlng);

        mItemList.add(item);
        sortByName();
    }


    public void delItem(int index)
    {
        mItemList.remove(index);
        sortByName();
    }


    public void updateItem(int index, Data d)
    {
        mItemList.get(index).mName = d.mName;
        mItemList.get(index).mLatLng = d.mLatLng;
        sortByName();
    }


    private void sortByName()
    {
        Comparator<Data> comp = new Comparator<Data>() {
            @Override
            public int compare(Data t1, Data t2) {
                return t1.mName.compareTo(t2.mName);
            }
        };

        Collections.sort(mItemList, comp);
        notifyDataSetChanged();
    }

}
