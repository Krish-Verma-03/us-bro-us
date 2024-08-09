package com.example.usbrous;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.usbrous.Adapters.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class Settings extends AppCompatActivity {
    EditText forname,forstatus;
    ImageView forprofile;
    Button forsave;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;
    String settingemail,settingpass;
    Uri imgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();

        DatabaseReference reference = database.getReference().child("user").child(auth.getUid());
        StorageReference storageReference = storage.getReference().child("user").child(auth.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                settingemail = snapshot.child("mail").getValue().toString();
                settingpass = snapshot.child("password").getValue().toString();
                String personpic = snapshot.child("profilePic").getValue().toString();
                String personstatus = snapshot.child("status").getValue().toString();
                String personname = snapshot.child("userName").getValue().toString();
                forname.setText(personname);
                forstatus.setText(personstatus);
                Picasso.get().load(personpic).into(forprofile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        forprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"),101);

            }
        });

        forsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                String newname = forname.getText().toString();
                String newstatus = forstatus.getText().toString();
                if(imgUri!=null){
                    storageReference.putFile(imgUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imgfordb = uri.toString();
                                        Users users = new Users(auth.getUid(),newname,settingemail,settingpass,imgfordb,newstatus);
                                        reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    progressDialog.dismiss();
                                                    Toast.makeText(Settings.this, "Data is saved ", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(Settings.this,MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(Settings.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }else{
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imgfordb = uri.toString();
                            Users users = new Users(auth.getUid(),newname,settingemail,settingpass,imgfordb,newstatus);
                            reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        progressDialog.dismiss();
                                        Toast.makeText(Settings.this, "Data is saved ", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Settings.this,MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else {
                                        progressDialog.dismiss();
                                        Toast.makeText(Settings.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==101){
            if(data!=null){
                imgUri=data.getData();
                forprofile.setImageURI(imgUri);
            }
        }
    }

    private void initViews() {
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        forname = findViewById(R.id.settingname);
        forprofile = findViewById(R.id.settingprofile);
        forstatus = findViewById(R.id.settingstatus);
        forsave = findViewById(R.id.donebutt);
        progressDialog = new ProgressDialog(Settings.this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);
    }
}