package com.qthekan.qhere.radar.listview;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qthekan.qhere.R;
import com.qthekan.util.qlog;

import java.util.ArrayList;


public class CustomAdapter extends BaseAdapter
{
    private ArrayList<ListViewItem> mItemList = new ArrayList<ListViewItem>() ;


    public CustomAdapter()
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
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageViewItem) ;
        TextView textTextView = (TextView) convertView.findViewById(R.id.textViewItem) ;

        ListViewItem item = mItemList.get(position);

        ImageView imageView = convertView.findViewById(R.id.imageViewItem);
        imageView.setImageBitmap(item.image);

        TextView textView = convertView.findViewById(R.id.textViewItem);
        textView.setText(item.id + "  " + item.name);

        return convertView;
    }


    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }


    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public ListViewItem getItem(int position) {
        return mItemList.get(position) ;
    }


    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(Bitmap icon, String id, String name) {
        ListViewItem item = new ListViewItem();

        item.image = icon;
        item.id = id;
        item.name = name;

        mItemList.add(item);
    }


    public ListViewItem getItemById(int id)
    {
        for(ListViewItem i : mItemList)
        {
            if( Integer.parseInt(i.id) == id )
            {
                return i;
            }
        }

        return null;
    }

}
