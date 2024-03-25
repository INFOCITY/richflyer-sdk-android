package net.richflyer.app;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import jp.co.infocity.richflyer.history.RFContent;

import static jp.co.infocity.richflyer.RichFlyer.showHistoryNotification;

public class NotificationHistoryAdapter extends RecyclerView.Adapter<NotificationHistoryAdapter.MyViewHolder> {

    private List<RFContent> notificationList;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView titleView;
        public TextView messageView;
        public ImageView imageView;
        public LinearLayout linearLayout;

        public MyViewHolder(View v) {
            super(v);
            titleView = (TextView) v.findViewById(R.id.notification_list_item_title);
            messageView = v.findViewById(R.id.notification_list_item_message);
            imageView = v.findViewById(R.id.notification_list_item_image);
            linearLayout = itemView.findViewById(R.id.notification_list);
        }
    }

    public NotificationHistoryAdapter(List<RFContent> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationHistoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_history_list_item, parent, false);

        //ViewHolderを生成
        return new NotificationHistoryAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if (notificationList != null && notificationList.get(position) != null) {
            holder.titleView.setText(notificationList.get(position).getTitle());
            holder.messageView.setText(notificationList.get(position).getMessage());
            if (notificationList.get(position).getImagePath() != null) {
                holder.imageView.setVisibility(View.VISIBLE);
                holder.imageView.setImageURI(Uri.parse(notificationList.get(position).getImagePath()));
            }
        }

        // リストをタップしたときの処理
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                showHistoryNotification(v.getContext(), notificationList.get(position).getNotificationId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }
}
