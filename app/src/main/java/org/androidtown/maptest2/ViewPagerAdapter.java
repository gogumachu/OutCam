package org.androidtown.maptest2;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {
    // Declare Variables
    Context context;
    List<String> contents=new ArrayList();
//    int[] flag;
    LayoutInflater inflater;
    // Declare Variables
    TextView text1;
    TextView text2;

    String t1;
    String t2;

    public ViewPagerAdapter(Context context,  List<String> contents) {
        this.context = context;
        this.contents = contents;
   //     this.flag = flag;
    //    this.rankArray.addAll(rankArray);
        //this.rankArray=rankArray;
    }

    @Override
    public int getCount() {
        return contents.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((ConstraintLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {



 //       ImageView imgflag;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.activity_swipe_item, container,
                false);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override //각각의 스와이프 눌렀을 때 서버로 text 정보 넘겨줘야함
            public void onClick(View view) {
                Intent intent = new Intent(context, MarkerInfoActivity.class);
                t1 = text1.getText().toString();
                t2 = text2.getText().toString();
                intent.putExtra("minfo1",t1);
                intent.putExtra("minfo2", t2);
                context.startActivity(intent);
            }
        });

        // Locate the TextViews in viewpager_item.xml
        text1 = (TextView) itemView.findViewById(R.id.swipe_text1);
        text2 = (TextView) itemView.findViewById(R.id.swipe_text2);

        // Capture position and set to the TextViews
        //txtrank.setText(rank[position]);

        text1.setText("아직 없음");
        text2.setText(contents.get(position));



        // Locate the ImageView in viewpager_item.xml
   ///     imgflag = (ImageView) itemView.findViewById(R.id.flag);
        // Capture position and set to the ImageView
   //     imgflag.setImageResource(flag[position]);

        // Add viewpager_item.xml to ViewPager
        ((ViewPager) container).addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // Remove viewpager_item.xml from ViewPager
        ((ViewPager) container).removeView((ConstraintLayout) object);

    }
}