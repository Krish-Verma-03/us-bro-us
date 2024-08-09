package com.example.usbrous;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usbrous.Adapters.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register extends AppCompatActivity {
    ProgressDialog progressDialog;
    FirebaseDatabase db;
    FirebaseAuth auth;
    FirebaseStorage storage;
    Button signup;
    EditText userName,password,repassword,email;
    TextView login;
    CircleImageView img;
    Uri uriImg;
    String imgurlfordb;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();
        getSupportActionBar().hide();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                Intent intent = new Intent(Register.this,Login.class);
                startActivity(intent);
                finish();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = userName.getText().toString();
                String Email = email.getText().toString();
                String Password = password.getText().toString();
                String RePassword = repassword.getText().toString();
                String status = "";
                
                if(TextUtils.isEmpty(username) || TextUtils.isEmpty(Email) || TextUtils.isEmpty(Password) || TextUtils.isEmpty(RePassword)){
                    progressDialog.dismiss();
                    Toast.makeText(Register.this, "Please Enter valid information", Toast.LENGTH_SHORT).show();
                }else if (!Email.matches(emailPattern)) {
                    progressDialog.dismiss();
                    email.setError("Give Valid Email Address");
                } else if (Password.length() < 6) {
                    progressDialog.dismiss();
                    password.setError("Password length should be 6 or more");
                } else if (!Password.equals(RePassword)){
                    progressDialog.dismiss();
                    repassword.setError("The Password Doesn't Match");
                } else{
                    auth.createUserWithEmailAndPassword(Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String id = task.getResult().getUser().getUid();
                                DatabaseReference dbref = db.getReference().child("user").child(id);
                                StorageReference storageref = storage.getReference().child("user").child(id);
                                if(uriImg!=null){
                                    storageref.putFile(uriImg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if(task.isSuccessful()){
                                                storageref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        imgurlfordb=uri.toString();
                                                        Users users = new Users(id,username,Email,Password,imgurlfordb,status);
                                                        dbref.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                progressDialog.show();
                                                                if(task.isSuccessful()){

                                                                    Intent intent = new Intent(Register.this,MainActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }else{
                                                                    Toast.makeText(Register.this, "Error in creating the user", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }else{
                                                Toast.makeText(Register.this, "Failed Upload.Please try again after sometime", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }else{
                                    String status = "Hey ! I am able to use Us Bro Us";
                                    imgurlfordb="https://firebasestorage.googleapis.com/v0/b/us-bro-us-b979b.appspot.com/o/man.png?alt=media&token=114ee072-c0a3-4ec4-b0c2-2b3142d8a885&_gl=1*1fw6ef0*_ga*NTEyNTYwOTU2LjE2OTE5MDIyMjI.*_ga_CW55HF8NVT*MTY5NzI3NTc4MS4zNy4xLjE2OTcyNzU5NTcuNTcuMC4w";
                                    Users users = new Users(id,username,Email,Password,imgurlfordb,status);
                                    dbref.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressDialog.show();
                                            if(task.isSuccessful()){
                                                Intent intent = new Intent(Register.this,MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }else{
                                                Toast.makeText(Register.this, "Error in creating the user", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }else{
                                Toast.makeText(Register.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"),100);
            }
        });
    }
    private void initViews() {
        progressDialog = new ProgressDialog(Register.this);
        progressDialog.setMessage("Establishing account...");
        progressDialog.setCancelable(false);
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        db = FirebaseDatabase.getInstance();
        email = findViewById(R.id.regEmail);
        password = findViewById(R.id.regPassword);
        repassword = findViewById(R.id.regRePassword);
        login = findViewById(R.id.textLogin);
        signup = findViewById(R.id.regButton);
        userName = findViewById(R.id.regName);
        img = findViewById(R.id.profilerg0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100){
            if(data!=null) {
                uriImg = data.getData();
                img.setImageURI(uriImg);
            }
        }else{
            Toast.makeText(this, "haaaaaaaa", Toast.LENGTH_SHORT).show();
        }
    }
}