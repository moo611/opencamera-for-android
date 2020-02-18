package com.atech.cameraapi;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterHolder> {

    private List<FilterInfo> infos;
    private Context c;
    private SelectedListener selectedListener;
    private int selected = 0;

    public FilterAdapter(Context c, List<FilterInfo> infos) {

        this.c = c;
        this.infos = infos;

    }

    @NonNull
    @Override
    public FilterHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(c).inflate(R.layout.item_filter,
                viewGroup, false);

        return new FilterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterHolder holder, final int i) {

        holder.thumbImage.setImageResource(infos.get(i).filterImg);
        holder.filterName.setText(infos.get(i).filterName);
        holder.filterName.setBackgroundColor(c.getResources().getColor(R.color.colorAccent));

        if(i == selected){
            holder.thumbSelected.setVisibility(View.VISIBLE);
            holder.thumbSelected_bg.setBackgroundColor(c.getResources().getColor(R.color.colorAccent));
            holder.thumbSelected_bg.setAlpha(0.7f);
        }else {
            holder.thumbSelected.setVisibility(View.GONE);
        }
        holder.filterRoot.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(selected == i)
                    return;
                int lastSelected = selected;
                selected = i;
                notifyItemChanged(lastSelected);
                notifyItemChanged(i);
                selectedListener.onFilterSelected(i);
            }
        });

    }


    @Override
    public int getItemCount() {
        return infos.size();
    }

    public void setFilterSeletedListener(SelectedListener listener) {

        this.selectedListener = listener;

    }

    public class FilterHolder extends RecyclerView.ViewHolder {

        ImageView thumbImage;
        TextView filterName;
        FrameLayout thumbSelected;
        FrameLayout filterRoot;
        View thumbSelected_bg;


        public FilterHolder(@NonNull View itemView) {
            super(itemView);

            thumbImage = (ImageView) itemView
                    .findViewById(R.id.filter_thumb_image);
            filterName = (TextView) itemView
                    .findViewById(R.id.filter_thumb_name);
            filterRoot = (FrameLayout) itemView
                    .findViewById(R.id.filter_root);
            thumbSelected = (FrameLayout) itemView
                    .findViewById(R.id.filter_thumb_selected);
            thumbSelected_bg = itemView.
                    findViewById(R.id.filter_thumb_selected_bg);

        }
    }
}
