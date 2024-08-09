package com.example.usbrous.Adapters;

import static com.example.usbrous.ChatWindow.myimg;
import static com.example.usbrous.ChatWindow.personimg;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usbrous.ChatWindow;
import com.example.usbrous.modal.MessageModal;
import com.example.usbrous.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter {
    Context context;
    ArrayList<MessageModal> messageModalArrayList;
    String senderRoom,recieverRoom;
    int ITEM_SEND =1;
    int ITEM_RECIEVE =2;
    int FILE_RECIEVE =3;
    int FILE_SEND =4;

    public MessageAdapter(Context context, ArrayList<MessageModal> messageModalArrayList,String senderRoom,String recieverRoom) {
        this.context = context;
        this.messageModalArrayList = messageModalArrayList;
        this.senderRoom = senderRoom;
        this.recieverRoom = recieverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SEND){
            View view = LayoutInflater.from(context).inflate(R.layout.my_layout,parent,false);
            return  new senderViewHolder(view);
        }else if(viewType == ITEM_RECIEVE){
            View view = LayoutInflater.from(context).inflate(R.layout.person_layout,parent,false);
            return  new recieverViewHolder(view);
        } else if (viewType == FILE_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.attachments_layout,parent,false);
            return  new senderFileViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.attachment_layout_r,parent,false);
            return  new recieverFileViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModal messageModal = messageModalArrayList.get(position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Dialog dialog = new Dialog(context,R.style.dialogue);
                dialog.setContentView(R.layout.deletemsg_dialog);
                Button yes,no;
                yes = dialog.findViewById(R.id.yesbntfordel);
                no = dialog.findViewById(R.id.nobntfordel);

                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        messageModal.setTypedmsg("This message was deleted");
                        FirebaseDatabase.getInstance().getReference()
                                .child("chats")
                                .child(senderRoom)
                                .child("messages")
                                .child(messageModal.getMessageId()).setValue(messageModal);

                        FirebaseDatabase.getInstance().getReference()
                                .child("chats")
                                .child(recieverRoom)
                                .child("messages")
                                .child(messageModal.getMessageId()).setValue(messageModal);
                        dialog.dismiss();
                    }
                });
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return false;
            }
        });
        if(holder.getClass()==senderFileViewHolder.class||holder.getClass()==recieverFileViewHolder.class) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String documentUrl = messageModal.getTypedmsg();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(documentUrl));

                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Handle if there's no browser app installed on the device
                    Toast.makeText(context, "No browser app found", Toast.LENGTH_SHORT).show();
                }
            }
        });}
        if(holder.getClass()==senderViewHolder.class){
            senderViewHolder objsender = (senderViewHolder) holder;
            objsender.sendertextmsg.setText(messageModal.getTypedmsg());
            Picasso.get().load(myimg).into(objsender.senderimgg);
        }else if(holder.getClass()==recieverViewHolder.class){
            recieverViewHolder objReciever = (recieverViewHolder) holder;
            objReciever.recievertextmsg.setText(messageModal.getTypedmsg());
            Picasso.get().load(personimg).into(objReciever.recieverrimgg);
        } else if(holder.getClass()==senderFileViewHolder.class) {
            senderFileViewHolder senderFileobj = (senderFileViewHolder) holder;
            senderFileobj.sentFile.setText(messageModal.getFileName());
            senderFileobj.sendertexturl.setText(messageModal.getTypedmsg());
            Picasso.get().load(myimg).into(senderFileobj.senderfileimg);
        }else{
            recieverFileViewHolder objFileReciever = (recieverFileViewHolder) holder;
            objFileReciever.recievedFile.setText(messageModal.getFileName());
            objFileReciever.receivertexturl.setText(messageModal.getTypedmsg());
            Picasso.get().load(personimg).into(objFileReciever.recieverfileimg);
        }
    }

    @Override
    public int getItemCount() {
        return messageModalArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        MessageModal messageModal = messageModalArrayList.get(position);
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messageModal.getmyUid()) &&
                (messageModal.getFileType().contains("image")) ||
                (messageModal.getFileType().contains("video")) ||
                (messageModal.getFileType().contains("audio")) ||
                (messageModal.getFileType().contains("document")) ||
                (messageModal.getFileType().contains("contact"))) {
            return FILE_SEND;
        } else if (!(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messageModal.getmyUid())) &&
                (messageModal.getFileType().contains("image")) ||
                (messageModal.getFileType().contains("video")) ||
                (messageModal.getFileType().contains("audio")) ||
                (messageModal.getFileType().contains("document")) ||
                (messageModal.getFileType().contains("contact"))) {
            return FILE_RECIEVE;
        } else if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messageModal.getmyUid())) {
            return ITEM_SEND;
        } else {
            return ITEM_RECIEVE;
        }
    }


    class senderViewHolder extends RecyclerView.ViewHolder {
        CircleImageView senderimgg;
        TextView sendertextmsg;
        public senderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderimgg=itemView.findViewById(R.id.profilerggg);
            sendertextmsg=itemView.findViewById(R.id.msgsendertyp);
        }
    }
    class recieverViewHolder extends RecyclerView.ViewHolder {
        CircleImageView recieverrimgg;
        TextView recievertextmsg;
        public recieverViewHolder(@NonNull View itemView) {
            super(itemView);
            recieverrimgg=itemView.findViewById(R.id.pro);
            recievertextmsg=itemView.findViewById(R.id.recivertextset);
        }
    }
    class senderFileViewHolder extends RecyclerView.ViewHolder {
        CircleImageView senderfileimg;
        TextView sentFile,sendertexturl;
        public senderFileViewHolder(@NonNull View itemView) {
            super(itemView);
            senderfileimg=itemView.findViewById(R.id.profilergggfile);
            sentFile=itemView.findViewById(R.id.filename);
            sendertexturl=itemView.findViewById(R.id.filenameurl);
        }
    } class recieverFileViewHolder extends RecyclerView.ViewHolder {
        CircleImageView recieverfileimg;
        TextView recievedFile,receivertexturl;
        public recieverFileViewHolder(@NonNull View itemView) {
            super(itemView);
            recieverfileimg=itemView.findViewById(R.id.profileimgfile);
            recievedFile=itemView.findViewById(R.id.rfilename);
            receivertexturl=itemView.findViewById(R.id.filenameurl1);
        }
    }
}
