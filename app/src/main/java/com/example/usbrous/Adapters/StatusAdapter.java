package com.example.usbrous.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devlomi.circularstatusview.CircularStatusView;
import com.example.usbrous.MainActivity;
import com.example.usbrous.modal.MyStatus;
import com.example.usbrous.R;
import com.example.usbrous.modal.Status;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {
    Context context;
    ArrayList<MyStatus> userStatuses;

    public StatusAdapter(Context context, ArrayList<MyStatus> userStatuses) {
        this.context = context;
        this.userStatuses = userStatuses;
    }

    @NonNull
    @Override
    public StatusAdapter.StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status,parent,false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusAdapter.StatusViewHolder holder, int position) {

            Log.d("info", String.valueOf(userStatuses.size()));
            MyStatus myStatus = userStatuses.get(position);
            //this -1 is used for getting last index of last status
                Status lastStatus = myStatus.getStatuses().get(myStatus.getStatuses().size() - 1);
                Picasso.get().load(lastStatus.getImageUrl()).into(holder.storyimg);
                holder.statuscount.setPortionsCount(myStatus.getStatuses().size());

                holder.statuscount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<MyStory> myStoryArrayList = new ArrayList<>();
                        for (Status status : myStatus.getStatuses()
                        ) {
                            myStoryArrayList.add(new MyStory(status.getImageUrl()));
                        }
                        new StoryView.Builder(((MainActivity) context).getSupportFragmentManager())
                                .setStoriesList(myStoryArrayList) // Required
                                .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                                .setTitleText(myStatus.getName()) // Default is Hidden
                                .setSubtitleText("") // Default is Hidden
                                .setTitleLogoUrl(myStatus.getProfileImage()) // Default is Hidden
                                .setStoryClickListeners(new StoryClickListeners() {
                                    @Override
                                    public void onDescriptionClickListener(int position) {
                                        //your action
                                    }

                                    @Override
                                    public void onTitleIconClickListener(int position) {
                                        //your action
                                    }
                                }) // Optional Listeners
                                .build() // Must be called before calling show method
                                .show();
                    }
                });
     }
//            else if(myStatus.getStatuses().size() == 0) {
//                holder.storyimg.setImageDrawable(null);
//                holder.statuscount.setPortionsCount(myStatus.getStatuses().size());
//                holder.statuscount.setOnClickListener(new View.OnClickListener() {
//                                                          @Override
//                                                          public void onClick(View view) {
//                                                              ArrayList<MyStory> myStoryArrayList = new ArrayList<>();
//                                                              for (Status status : myStatus.getStatuses()
//                                                              ) {
//                                                                  myStoryArrayList.add(new MyStory(status.getImageUrl()));
//                                                              }
//                                                          }
//                                                      });
//                Toast.makeText(context, "zero status", Toast.LENGTH_SHORT).show();
//            }


    @Override
    public int getItemCount() {
        return userStatuses.size();
    }

    public class StatusViewHolder extends RecyclerView.ViewHolder {
        CircleImageView storyimg;
        CircularStatusView statuscount;
        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            storyimg = itemView.findViewById(R.id.imageforstory);
            statuscount= itemView.findViewById(R.id.circular_status_view);
        }
    }
}
