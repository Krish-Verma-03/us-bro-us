package com.example.usbrous;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usbrous.Adapters.MessageAdapter;
import com.example.usbrous.modal.MessageModal;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatWindow extends AppCompatActivity {
    String chatperson , personprofilepic,personuid , myid ;
    CircleImageView personpic;
    TextView personname;
    TextView statusupdate;
    EditText mymsg;
    CardView sendmsg,attachments,cardAttach;
    RecyclerView rvformsgs;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    private  String checker="";
    public static String myimg;
    public static String personimg;
    String senderRoom,recieverRoom;
    ArrayList<MessageModal> messageModalArrayList;
    MessageAdapter messageAdapter;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);
        getSupportActionBar().hide();
        initViews();

        chatperson = getIntent().getStringExtra("nameofsender");
        personprofilepic = getIntent().getStringExtra("picofsender");
        personuid = getIntent().getStringExtra("uidofsender");
        myid = auth.getUid();

        Picasso.get().load(personprofilepic).into(personpic);
        personname.setText(""+chatperson);

        senderRoom= personuid+myid;
        recieverRoom = myid+personuid;

        //Setting chat messages on Chatwindow by bringing data from adapter into a Linear Layout
        messageModalArrayList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        rvformsgs.setLayoutManager(linearLayoutManager);
        messageAdapter = new MessageAdapter(ChatWindow.this,messageModalArrayList,senderRoom,recieverRoom);
        rvformsgs.setAdapter(messageAdapter);

        DatabaseReference reference = database.getReference().child("user").child(myid);
        DatabaseReference chatreference = database.getReference().child("chats").child(senderRoom).child("messages");


        chatreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageModalArrayList.clear();
                for (DataSnapshot datasnapshot:snapshot.getChildren()
                     ) {
                    MessageModal messageModal = datasnapshot.getValue(MessageModal.class);
                    messageModal.setMessageId(datasnapshot.getKey());
                    messageModalArrayList.add(messageModal);
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myimg = snapshot.child("profilePic").getValue().toString();
                personimg =  personprofilepic;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String typedmsg = mymsg.getText().toString();
                if(typedmsg.isEmpty()){
                    Toast.makeText(ChatWindow.this, "Enter Message First", Toast.LENGTH_SHORT).show();
                    return;
                }
                mymsg.setText("");
                Date date = new Date();
                MessageModal messageModal = new MessageModal(typedmsg,myid,date.getTime());
                database= FirebaseDatabase.getInstance();

                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg", messageModal.getTypedmsg());
                lastMsgObj.put("lastMsgTime", date.getTime());
                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                database.getReference().child("chats").child(recieverRoom).updateChildren(lastMsgObj);
                String randomKey = database.getReference().push().getKey();
                database.getReference().child("chats").child(senderRoom).child("messages")
                        .child(randomKey).setValue(messageModal).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                database.getReference()
                                        .child("chats")
                                        .child(recieverRoom)
                                        .child("messages")
                                        .child(randomKey).setValue(messageModal)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                            }
                                        });
                            }
                        });
            }
        });

        attachments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cardAttach.getVisibility() == View.VISIBLE) {
                    cardAttach.setVisibility(View.GONE);
                } else {
                    cardAttach.setVisibility(View.VISIBLE);
                }



                ImageView imageIcon =findViewById(R.id.imageIcon);
                ImageView audioIcon =findViewById(R.id.audioIcon);
                ImageView videoIcon = findViewById(R.id.videoIcon);
                ImageView documentIcon = findViewById(R.id.documentIcon);
                ImageView contactIcon = findViewById(R.id.contactIcon);

                imageIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checker = "image";
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent.createChooser(intent,"Select Image"),443);
                    }
                });

                audioIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checker = "audio";
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("audio/*");
                        startActivityForResult(intent.createChooser(intent,"Select Audio"),443);
                    }
                });

                videoIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checker = "video";
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("video/*");
                        startActivityForResult(intent.createChooser(intent,"Select Video"),443);
                    }
                });

                documentIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checker = "document";
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("documents/*");
                        startActivityForResult(intent.createChooser(intent,"Select Document"),443);
                    }
                });

                contactIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checker = "contact";
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("contacts/*");
                        startActivityForResult(intent.createChooser(intent,"Select Contacts"),443);
                    }
                });


            }
        });



        database.getReference().child("presence").child(personuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String status = snapshot.getValue(String.class);
                    if(!status.isEmpty()) {
                        if(status.equals("Offline")) {
                            statusupdate.setVisibility(View.GONE);
                        } else {
                            statusupdate.setText(status);
                            statusupdate.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        final Handler handler = new Handler();
        mymsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(auth.getUid()).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping,1000);
            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(auth.getUid()).setValue("Online");
                }
            };
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 443 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();
            String fileType = checker;

            String fileExtension = getFileExtension(fileUri);

            if (fileExtension != null) {
                uploadFileToFirebaseStorage(fileUri, fileExtension,fileType);
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadFileToFirebaseStorage(Uri fileUri, final String fileExtension , String fileType) {
        StorageReference storageReference = storage.getReference().child("uploads");
        String uniqueKey = database.getReference().child("chats").child(senderRoom).child("messages").push().getKey().substring(0, 8);
        final String fileName = "DOC" +uniqueKey+ "." + fileExtension;



        storageReference.child(fileName).putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageReference.child(fileName).getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        updateRealtimeDatabase(downloadUrl, fileName,fileType);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateRealtimeDatabase(String downloadUrl, String fileName, String fileType) {
        Date date = new Date();
        MessageModal messageModal = new MessageModal(downloadUrl,myid,date.getTime(),fileType,fileName);
        DatabaseReference databaseReference = database.getReference().child("chats").child(senderRoom).child("messages");
                databaseReference.push().setValue(messageModal).addOnCompleteListener(task -> {
                            database.getReference()
                                    .child("chats")
                                    .child(recieverRoom)
                                    .child("messages")
                                    .push().setValue(messageModal)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    }).addOnFailureListener(e -> {
                                        // Handle any errors that occurred during database update
                                        Toast.makeText(this, "Database update failed", Toast.LENGTH_SHORT).show();
                                    });
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occurred during database update
                    Toast.makeText(this, "Database update failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void initViews() {
        personpic = findViewById(R.id.profileimgg);
        personname = findViewById(R.id.recivername);
        mymsg = findViewById(R.id.textmsg);
        sendmsg = findViewById(R.id.sendbtnn);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        rvformsgs = findViewById(R.id.msgadapter);
        statusupdate = findViewById(R.id.statusupdate);
        attachments = findViewById(R.id.sendfiles);
        progressDialog = new ProgressDialog(ChatWindow.this);
        cardAttach=findViewById(R.id.cardattach);
    }
}