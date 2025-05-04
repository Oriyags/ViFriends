package com.oriya_s.tashtit.ACTIVITIES;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.oriya_s.model.Message;
import com.oriya_s.tashtit.ADPTERS.MessageAdapter;
import com.oriya_s.tashtit.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class ChatActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private RecyclerView recyclerView;
    private EditText inputMessage;
    private ImageButton btnSend;
    private MessageAdapter adapter;
    private List<Message> messageList;

    private String otherUserId;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recycler_messages);
        inputMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        otherUserId = getIntent().getStringExtra("userId");
        if (currentUser == null || otherUserId == null) {
            finish();
            return;
        }

        createOrLoadChat();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void createOrLoadChat() {
        DocumentReference currentChatRef = db.collection("users")
                .document(currentUser.getUid())
                .collection("chats")
                .document(otherUserId);

        currentChatRef.get().addOnSuccessListener(doc -> {
            if (doc.exists() && doc.contains("chatId")) {
                chatId = doc.getString("chatId");
                loadMessages();
            } else {
                // Create new chat
                chatId = db.collection("Chats").document().getId();
                Map<String, Object> chatMeta = new HashMap<>();
                chatMeta.put("chatId", chatId);
                chatMeta.put("withUser", otherUserId);
                chatMeta.put("lastSeen", System.currentTimeMillis());

                db.collection("users").document(currentUser.getUid())
                        .collection("chats").document(otherUserId).set(chatMeta);

                Map<String, Object> otherChatMeta = new HashMap<>();
                otherChatMeta.put("chatId", chatId);
                otherChatMeta.put("withUser", currentUser.getUid());
                otherChatMeta.put("lastSeen", System.currentTimeMillis());

                db.collection("users").document(otherUserId)
                        .collection("chats").document(currentUser.getUid()).set(otherChatMeta);

                Map<String, Object> chatDoc = new HashMap<>();
                chatDoc.put("participants", List.of(currentUser.getUid(), otherUserId));
                db.collection("Chats").document(chatId).set(chatDoc);

                loadMessages();
            }
        });
    }

    private void loadMessages() {
        CollectionReference messagesRef = db.collection("Chats")
                .document(chatId)
                .collection("messages");

        messagesRef.orderBy("timestamp")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    messageList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Message msg = doc.toObject(Message.class);
                        messageList.add(msg);
                    }
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageList.size() - 1);
                });
    }

    private void sendMessage() {
        String content = inputMessage.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        Map<String, Object> msgData = new HashMap<>();
        msgData.put("senderID", currentUser.getUid());
        msgData.put("text", content);
        msgData.put("timestamp", System.currentTimeMillis());

        db.collection("Chats")
                .document(chatId)
                .collection("messages")
                .add(msgData)
                .addOnSuccessListener(ref -> inputMessage.setText(""));

        db.collection("Chats")
                .document(chatId)
                .update("lastMessage", msgData);
    }
}