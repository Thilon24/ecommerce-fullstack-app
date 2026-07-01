package com.example.ecommerceapp.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerceapp.R;
import com.example.ecommerceapp.adapters.ChatAdapter; // 👈 මේක හරියට import කරගන්න
import com.example.ecommerceapp.models.ChatMessage; // 👈 මේක හරියට import කරගන්න
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private EditText messageInput;
    private ImageButton sendBtn;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        messageInput = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendBtn);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);


        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);


        listenForMessages();

        sendBtn.setOnClickListener(v -> {
            String msg = messageInput.getText().toString().trim();
            if (!msg.isEmpty()) {
                sendMessage(msg);
            }
        });
    }

    private void listenForMessages() {
        db.collection("Chats")
                .document(currentUserId)
                .collection("Messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                messageList.add(dc.getDocument().toObject(ChatMessage.class));
                            }
                        }
                        chatAdapter.notifyDataSetChanged();
                        chatRecyclerView.smoothScrollToPosition(messageList.size());
                    }
                });
    }

    private void sendMessage(String text) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("text", text);
        messageData.put("senderId", currentUserId);
        messageData.put("isAdmin", false);
        messageData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Chats")
                .document(currentUserId)
                .collection("Messages")
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    messageInput.setText("");
                });

        Map<String, Object> chatSummary = new HashMap<>();
        chatSummary.put("lastMessage", text);
        chatSummary.put("timestamp", FieldValue.serverTimestamp());
        chatSummary.put("userId", currentUserId);

        db.collection("Chats")
                .document(currentUserId)
                .set(chatSummary, SetOptions.merge());
    }
}