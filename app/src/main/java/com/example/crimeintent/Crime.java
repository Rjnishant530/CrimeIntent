package com.example.crimeintent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;

public class Crime {

    private String title;
    private UUID id;
    private LocalDateTime mdate;
    private Boolean solved;
    private String suspect;
    private long number;

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public String getSuspect() {
        return suspect;
    }

    public void setSuspect(String suspect) {
        this.suspect = suspect;
    }

    public Crime() {
        title="";
        id = UUID.randomUUID();
        mdate = LocalDateTime.now();
        solved=false;
    }
    public Crime(UUID uuid){
        id=uuid;
        mdate=LocalDateTime.now();
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UUID getId() {
        return id;
    }


    public LocalDate getMdate() {
        return mdate.toLocalDate();
    }

    public LocalTime getMTime(){
        return mdate.toLocalTime();
    }

    public void setMdate(LocalDate mdate) {
       this.mdate= LocalDateTime.of(mdate,this.mdate.toLocalTime());
}
    public void setMTime(LocalTime mTime){
        this.mdate=LocalDateTime.of(this.mdate.toLocalDate(),mTime);
    }

    public LocalDateTime getDateTime(){
        return mdate;
    }
    public void setDateTime(LocalDateTime c){
        this.mdate=c;
    }

    public Boolean isSolved() {
        return solved;
    }

    public void setSolved(Boolean solved) {
        this.solved = solved;
    }

    public String getPhotoFilename() {
        return "IMG_" + getId().toString() + ".jpg";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof Crime && ((Crime) obj).getId() == this.getId()) {
                return true;
            }
        }

        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return getId().toString()+" : "+ getTitle();
    }
}
