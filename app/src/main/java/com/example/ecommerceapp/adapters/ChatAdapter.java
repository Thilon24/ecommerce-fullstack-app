package com.example.ecommerceapp.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerceapp.R;
import com.example.ecommerceapp.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatMessage> messageList;
    private String currentUserId;

    public ChatAdapter(List<ChatMessage> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);
        holder.messageText.setText(msg.getText());

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageText.getLayoutParams();


        if (msg.getIsAdmin()) {
            holder.messageText.setBackgroundResource(R.drawable.receiver);
            params.gravity = Gravity.START;
        } else {
            holder.messageText.setBackgroundResource(R.drawable.sender);
            params.gravity = Gravity.END;
        }
        holder.messageText.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
        }
    }
}