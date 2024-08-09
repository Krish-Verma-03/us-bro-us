package com.example.usbrous.modal;

public class MessageModal {
    String typedmsg;
    String myUid;
    String messageId;


    String fileType ="";
    String fileName="";
    long timeStamp;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public MessageModal() {
    }

    public MessageModal(String typedmsg, String myUid, long timeStamp) {
        this.typedmsg = typedmsg;
        this.myUid = myUid;
        this.timeStamp = timeStamp;
    }
    public MessageModal(String typedmsg, String myUid, long timeStamp,String fileType,String fileName) {
        this.typedmsg = typedmsg;
        this.myUid = myUid;
        this.timeStamp = timeStamp;
        this.fileType = fileType;
        this.fileName = fileName;
    }

    public String getTypedmsg() {
        return typedmsg;
    }

    public void setTypedmsg(String typedmsg) {
        this.typedmsg = typedmsg;
    }

    public String getmyUid() {
        return myUid;
    }

    public void setmyUid(String myUid) {
        this.myUid = myUid;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }



}
