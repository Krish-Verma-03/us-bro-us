package com.example.usbrous;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.usbrous.Adapters.StatusAdapter;
import com.example.usbrous.Adapters.UserAdapter;
import com.example.usbrous.Adapters.Users;
import com.example.usbrous.modal.MyStatus;
import com.example.usbrous.modal.Status;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseDatabase db;
    FirebaseStorage storage;
    RecyclerView recyclerView,recyclerViewforstatus;
    ArrayList<Users> usersArrayList;
    UserAdapter userAdapter;
    ImageView logoutimg;
    ImageView camerabtn;
    ImageView storystatus;
    ImageView settingsbtn;
    ShimmerFrameLayout shimmerFrameLayout, shimmerFrameLayoutforstatus;
    StatusAdapter statusAdapter;
    ArrayList<MyStatus> myStatusArrayList,filteredUserStatuses;
    String myname,myprofpic,userId;
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        initViews();

        DatabaseReference databaseReference = db.getReference().child("user");

        myStatusArrayList = new ArrayList<>();
        filteredUserStatuses = new ArrayList<>();
        db.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    myStatusArrayList.clear();
                    filteredUserStatuses.clear();
                    for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                        //setting up each person's status
                        MyStatus myStatus = new MyStatus();
                        myStatus.setName(storySnapshot.child("name").getValue(String.class));
                        myStatus.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        myStatus.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));
                        myStatus.setUserId(storySnapshot.child("userId").getValue(String.class));

                        //setting up list of statuses of a single person
                        ArrayList<Status> statusArrayList = new ArrayList<>();

                        for (DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()) {
                            Status sampleStatus = statusSnapshot.getValue(Status.class);
                            sampleStatus.setStatusId(statusSnapshot.getKey());
                            statusArrayList.add(sampleStatus);

                        }

                        myStatus.setStatuses(statusArrayList);
                        //setting statuses list of every person
                        myStatusArrayList.add(myStatus);
                    }
                    Collections.sort(myStatusArrayList, new Comparator<MyStatus>() {
                        @Override
                        public int compare(MyStatus status1, MyStatus status2) {
                            return Long.compare(status1.getLastUpdated(), status2.getLastUpdated());
                        }
                    });
                    if (auth.getCurrentUser() != null) {
                        int currentUserStatusIndex = -1;
                        for (int i = 0; i < myStatusArrayList.size(); i++) {
                            if (myStatusArrayList.get(i).getUserId().equals(auth.getCurrentUser().getUid())) {
                                currentUserStatusIndex = i;
                                break;
                            }
                        }
                       if (currentUserStatusIndex != -1) {
                            MyStatus currentUserStatus = myStatusArrayList.remove(currentUserStatusIndex);
                            myStatusArrayList.add(0, currentUserStatus);
                        }
                        for (MyStatus myStatus : myStatusArrayList) {
                            if (myStatus.getStatuses().size() > 0) {
                                filteredUserStatuses.add(myStatus);
                            }
                        }

                    }
                    shimmerFrameLayoutforstatus.stopShimmer();
                    shimmerFrameLayoutforstatus.setVisibility(View.GONE);
                    statusAdapter.notifyDataSetChanged();
                    if (auth.getCurrentUser() != null) {
                        DatabaseReference storiesRef = db.getReference()
                                .child("stories")
                                .child(auth.getUid())
                                .child("statuses");
                        storiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot statusSnapshot : snapshot.getChildren()) {
                                        Status sampleStatus = statusSnapshot.getValue(Status.class);
                                        sampleStatus.setStatusId(statusSnapshot.getKey());
                                        scheduleStoryCleanup(sampleStatus);
                                    }
                                    statusAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                }
            }

                @Override
                public void onCancelled (@NonNull DatabaseError error){

                }
            });
        if(filteredUserStatuses!=null) {
            statusAdapter = new StatusAdapter(MainActivity.this, filteredUserStatuses);

            recyclerViewforstatus.setAdapter(statusAdapter);
        }
        shimmerFrameLayout.startShimmer();

        usersArrayList = new ArrayList<>();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayList.clear();
                for (DataSnapshot datasnap: snapshot.getChildren()
                     ) {
                    Users users = datasnap.getValue(Users.class);
                    usersArrayList.add(users);
                }
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //For buttons
        camerabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,103);
            }
        });
        storystatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"),105);
            }
        });
        settingsbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,Settings.class);
                startActivity(intent);
            }
        });

        //For getting chat users
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userAdapter = new UserAdapter(MainActivity.this,usersArrayList);

        recyclerView.setAdapter(userAdapter);
        shimmerFrameLayout.startShimmer();

        //For logout
        logoutimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(MainActivity.this,R.style.dialogue);
                dialog.setContentView(R.layout.dialog);
                Button yes,no;
                yes = dialog.findViewById(R.id.yesbnt);
                no = dialog.findViewById(R.id.nobnt);

                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(auth.getCurrentUser()!=null) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(MainActivity.this, Login.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });


        //For staying logged in
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser()==null){
            Intent intent = new Intent(MainActivity.this,Login.class);
            startActivity(intent);
            finish();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==105) {
            if (data != null) {
                if (data.getData() != null && auth.getCurrentUser()!=null) {
                    progressDialog.show();
                    DatabaseReference reference = db.getReference().child("user").child(auth.getUid());
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            myprofpic = snapshot.child("profilePic").getValue().toString();
                            myname = snapshot.child("userName").getValue().toString();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    Date date=new Date();
                    StorageReference storageReference = storage.getReference().child("status").child(auth.getUid()).child(date.getTime()+"");

                    storageReference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                     MyStatus myStatus = new MyStatus();
                                     myStatus.setName(myname);
                                     myStatus.setProfileImage(myprofpic);
                                     myStatus.setLastUpdated(date.getTime());
                                     myStatus.setUserId(auth.getUid());

                                        HashMap<String, Object> obj = new HashMap<>();
                                        obj.put("name", myStatus.getName());
                                        obj.put("profileImage", myStatus.getProfileImage());
                                        obj.put("lastUpdated", myStatus.getLastUpdated());
                                        obj.put("userId", myStatus.getUserId());

                                        String imageUrl = uri.toString();
                                        Status status = new Status(imageUrl, myStatus.getLastUpdated());

                                        db.getReference()
                                                .child("stories")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .updateChildren(obj);
                                        String randomKey = db.getReference().push().getKey();
                                        db.getReference().child("stories")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .child("statuses")
                                                .child(randomKey)
                                                .setValue(status);
                                        progressDialog.dismiss();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }
    public void scheduleStoryCleanup(Status status){
        // Schedule a task to run every hour
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Trigger the story cleanup
                cleanupOldStories(status);
            }
        };
        timer.schedule(task, 0, 30000);
    }
    private void cleanupOldStories(Status status) {
        long currentTimestamp = System.currentTimeMillis();
        long twentyFourHoursInMillis = 24*3600*1000;

        // Calculate the timestamp 24 hours ago
        long twentyFourHoursAgo = currentTimestamp - twentyFourHoursInMillis;

        // Reference to the stories node in Firebase
        DatabaseReference storiesRef = db.getReference()
                .child("stories")
                .child(FirebaseAuth.getInstance().getUid())
                .child("statuses")
                .child(status.getStatusId());
       StorageReference storagestoryref=storage.getReference().child("status").child(auth.getUid());

        // Query stories that are older than 24 hours
        storiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot timeStampSnapshot = dataSnapshot.child("timeStamp");
                if (timeStampSnapshot.exists()) {
                    Long storyTimestamp = timeStampSnapshot.getValue(Long.class);
                    if (storyTimestamp != null && storyTimestamp < twentyFourHoursAgo) {
                        // Delete the status
                        storiesRef.removeValue();
                        storagestoryref.child(String.valueOf(storyTimestamp)).delete();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Error cleaning up old stories: " + databaseError.getMessage());
            }
        });

    }

    private void initViews() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        recyclerView = findViewById(R.id.mainUserRecyclerView);
        logoutimg= findViewById(R.id.logoutimg);
        camerabtn= findViewById(R.id.camBut);
        storystatus= findViewById(R.id.storyid);
        settingsbtn= findViewById(R.id.settingBut);
        shimmerFrameLayout = findViewById(R.id.mainUserRecyclerView1);
        shimmerFrameLayoutforstatus = findViewById(R.id.statusList);
        recyclerViewforstatus = findViewById(R.id.statusListrv);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Updating Status");
        progressDialog.setCancelable(false);
    }
}

