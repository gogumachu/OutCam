package org.androidtown.maptest2;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder>{
    private final List<CardItem> mDateList;
    private MyRecyclerViewClickListener mListener;


    public MyRecyclerAdapter(List<CardItem> mDateList) {
        this.mDateList = mDateList;
    }

    // 최초의 레이아웃을 생성하고, 뷰홀더에 보관하는 부분
    // 뷰 홀더를 생성하는 부분, 레이아웃을 만드는 부분
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    // 뷰 홀더에 데이터를 설정하는 부분
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        CardItem item = mDateList.get(i);
        viewHolder.cameraLocationTitle.setText(item.getCameraLocationTitle());
        viewHolder.contents.setText(item.getContent());
        viewHolder.date.setText(item.getDate());
        final String t1 = item.getCameraLocationTitle();
        final String t2 = item.getContent();
        final String t3 = item.getDate();
        final String t4 = item.getUrl();
        // 일단 drawalbe 파일에 있는 걸로

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Log.d("Clicke!!","clicked!!");
                Intent intent = new Intent(context, MarkerInfoActivity.class);
                intent.putExtra("locainfo",t1);
                intent.putExtra("content", t2);
                intent.putExtra("date", t3);
                intent.putExtra("url",t4);
                context.startActivity(intent);
               // Toast.makeText(context, position +"", Toast.LENGTH_LONG).show();
            }
        });

    }

    // 아이템의 수
    @Override
    public int getItemCount() {
        return mDateList.size();
    }

    // 각각의 아이템의 레퍼런스를 저장할 뷰 홀더 클래스
    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView cameraLocationTitle;   // 카메라 위치 상세 정보 text
        TextView contents;              // 게시글 text
        TextView date;                  // 게시 날짜  text
        //ImageView imageView;            // 사진 image

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cameraLocationTitle = itemView.findViewById(R.id.cameraLocationInfo);
            contents = itemView.findViewById(R.id.contents);
            date = itemView.findViewById(R.id.date);
            //imageView = itemView.findViewById(R.id.imageView);
        }
    }


    public void setOnClickListener(MyRecyclerViewClickListener listener){
        mListener = listener;
    }

    // 여기서 클릭했을 때의 activity 를 처리
    public interface MyRecyclerViewClickListener {
        // 아이템 전체 부분 클릭
        void onItemClicked(int position);
    }
}
