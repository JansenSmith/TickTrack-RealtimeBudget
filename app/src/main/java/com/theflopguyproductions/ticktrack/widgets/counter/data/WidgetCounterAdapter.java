package com.theflopguyproductions.ticktrack.widgets.counter.data;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.theflopguyproductions.ticktrack.R;
import com.theflopguyproductions.ticktrack.counter.CounterData;
import com.theflopguyproductions.ticktrack.counter.CounterDiffUtilCallback;
import com.theflopguyproductions.ticktrack.utils.database.TickTrackDatabase;
import com.theflopguyproductions.ticktrack.utils.helpers.TimeAgo;

import java.util.ArrayList;
import java.util.Random;

public class WidgetCounterAdapter extends RecyclerView.Adapter<WidgetCounterAdapter.counterDataViewHolder>  {


    private ArrayList<CounterData> counterDataArrayList;
    private Context context;
    private counterDataViewHolder.RecyclerViewClickListener mListener;


    public WidgetCounterAdapter(Context context, ArrayList<CounterData> counterDataArrayList, counterDataViewHolder.RecyclerViewClickListener listener ){
        this.context = context;
        this.counterDataArrayList = counterDataArrayList;
        mListener = listener;
    }

    private void setTheme(WidgetCounterAdapter.counterDataViewHolder holder, int theme) {
        if(theme == 1){
            holder.counterLayout.setBackgroundResource(R.drawable.recycler_layout_light);
            holder.counterLabel.setTextColor(holder.context.getResources().getColor(R.color.Gray));
            holder.lastModified.setTextColor(holder.context.getResources().getColor(R.color.Gray));
        } else {
            holder.counterLayout.setBackgroundResource(R.drawable.recycler_layout_dark);
            holder.counterLabel.setTextColor(holder.context.getResources().getColor(R.color.LightText));
            holder.lastModified.setTextColor(holder.context.getResources().getColor(R.color.LightText));
        }
        holder.countValue.setTextColor(holder.context.getResources().getColor(R.color.Accent));
    }

    private void setColor(WidgetCounterAdapter.counterDataViewHolder holder) {
        if(holder.itemColor==1){
            holder.counterFlag.setImageResource(R.drawable.ic_flag_red);
        }
        else if(holder.itemColor==2){
            holder.counterFlag.setImageResource(R.drawable.ic_flag_green);
        }
        else if(holder.itemColor==3){
            holder.counterFlag.setImageResource(R.drawable.ic_flag_orange);
        }
        else if(holder.itemColor==4){
            holder.counterFlag.setImageResource(R.drawable.ic_flag_purple);
        }
        else if(holder.itemColor==5){
            holder.counterFlag.setImageResource(R.drawable.ic_flag_blue);
        } else {
            holder.counterFlag.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public WidgetCounterAdapter.counterDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView;

        if(viewType == R.layout.counter_item_layout){
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.counter_item_layout, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_footer_layout, parent, false);
        }

        return new WidgetCounterAdapter.counterDataViewHolder(itemView, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull WidgetCounterAdapter.counterDataViewHolder holder, int position) {

        int currentTheme = holder.tickTrackDatabase.getThemeMode();

        if(position == counterDataArrayList.size()) {
            if(currentTheme == 1){
                holder.footerCounterTextView.setTextColor(holder.context.getResources().getColor(R.color.DarkText));
            } else {
                holder.footerCounterTextView.setTextColor(holder.context.getResources().getColor(R.color.LightText));
            }
            Resources resources = holder.context.getResources();
            String[] footerArray = resources.getStringArray(R.array.footer_string_array);
            int randomFooter = new Random().nextInt(footerArray.length);
            holder.footerCounterTextView.setText(footerArray[randomFooter]);
        } else {
            holder.countValue.setText(""+counterDataArrayList.get(position).getCounterValue());
            holder.counterLabel.setText(counterDataArrayList.get(position).getCounterLabel());

            if(counterDataArrayList.get(position).getCounterTimestamp()!=-1){
                holder.lastModified.setText("Last edited: "+ TimeAgo.getTimeAgo(counterDataArrayList.get(position).getCounterTimestamp()));
            }

            holder.itemColor = counterDataArrayList.get(position).getCounterFlag();
            setColor(holder);
            setTheme(holder, currentTheme);

//            holder.counterLayout.setOnClickListener(v -> {
//                CounterFragment.startCounterActivity(counterDataArrayList.get(holder.getAdapterPosition()).getCounterID(), (Activity) holder.context);
//                Toast.makeText(holder.context, "Position:" + holder.getAdapterPosition(), Toast.LENGTH_SHORT).show();
//            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == counterDataArrayList.size()) ? R.layout.recycler_footer_layout : R.layout.counter_item_layout;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getItemCount() {
        return counterDataArrayList.size()+1;
    }

    public void diffUtilsChangeData(ArrayList<CounterData> counterDataArrayList){

        CounterDiffUtilCallback counterDiffUtilCallback = new CounterDiffUtilCallback(counterDataArrayList, this.counterDataArrayList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(counterDiffUtilCallback, false);
        diffResult.dispatchUpdatesTo(this);
        this.counterDataArrayList = counterDataArrayList;

    }



    public static class counterDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView countValue, lastModified, counterLabel;
        public ConstraintLayout counterLayout;
        private int itemColor;
        private ImageView counterFlag;
        private Context context;
        private TextView footerCounterTextView;
        TickTrackDatabase tickTrackDatabase;
        private RecyclerViewClickListener mListener;

        public counterDataViewHolder(@NonNull View parent, RecyclerViewClickListener listener) {
            super(parent);

            mListener = listener;
            parent.setOnClickListener(this);

            countValue = parent.findViewById(R.id.counterValueItemTextView);
            counterLabel = parent.findViewById(R.id.counterLabelItemTextView);
            lastModified = parent.findViewById(R.id.counterLastUpdateItemTextView);
            counterLayout = parent.findViewById(R.id.counterItemRootLayout);
            counterFlag = parent.findViewById(R.id.counterFlagItemImageView);
            footerCounterTextView = parent.findViewById(R.id.recylerFooterTextView);

            context=parent.getContext();
            tickTrackDatabase = new TickTrackDatabase(context);

        }

        @Override
        public void onClick(View view) {
            mListener.onItemClick(view, getAdapterPosition());
        }

        public interface RecyclerViewClickListener {
            void onItemClick(View view, int position);
        }

    }
}