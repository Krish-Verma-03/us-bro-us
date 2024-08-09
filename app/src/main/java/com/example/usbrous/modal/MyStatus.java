package com.example.usbrous.modal;

import java.util.ArrayList;
public class MyStatus {

        private String name, profileImage;
        private long lastUpdated;
        private ArrayList<Status> statuses;
        private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public MyStatus() {
        }

        public MyStatus(String name, String profileImage, long lastUpdated, ArrayList<Status> statuses) {
            this.name = name;
            this.profileImage = profileImage;
            this.lastUpdated = lastUpdated;
            this.statuses = statuses;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProfileImage() {
            return profileImage;
        }

        public void setProfileImage(String profileImage) {
            this.profileImage = profileImage;
        }

        public long getLastUpdated() {
            return lastUpdated;
        }

        public void setLastUpdated(long lastUpdated) {
            this.lastUpdated = lastUpdated;
        }

        public ArrayList<Status> getStatuses() {
            return statuses;
        }

        public void setStatuses(ArrayList<Status> statuses) {
            this.statuses = statuses;
        }

}
