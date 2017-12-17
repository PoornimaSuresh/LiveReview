package com.mobile.project.livereview;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.mobile.project.livereview.entity.ChatBubble;

import java.util.List;

/**
 * Created by PS-Student on 11/25/17.
 */

public class MessageAdapter extends ArrayAdapter<ChatBubble> {

    private Context context;
    private List<ChatBubble> messages;

    public MessageAdapter(@NonNull Context context, int resource, List<ChatBubble> bubbles) {
        super(context, resource, bubbles);
        this.context = context;
        this.messages = bubbles;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        int layoutResource = 0; // determined by view type
        ChatBubble chatBubble = getItem(position);
        int viewType = getItemViewType(position);

        //Use blue bubble if message is mine. Else, use gray
        if (chatBubble.myMessage()) {
            layoutResource = R.layout.right_message;
        } else {
            layoutResource = R.layout.left_message;
        }

        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        //set message content
        holder.msg.setText(chatBubble.getContent());

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        // return the total number of view types. this value should never change
        // at runtime. Value 2 is returned because of left and right views.
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        // return a value between 0 and (getViewTypeCount - 1)
        return position % 2;
    }

    private class ViewHolder {
        private TextView msg;

        public ViewHolder(View v) {
            msg = (TextView) v.findViewById(R.id.txt_msg);
        }
    }
}
