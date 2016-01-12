package com.mona.personalizedtwitter.CustomTweetManger;

/**
 * Created by mona on 1/11/2016.
 */
public class CustomTweetModal  {
    String update_text,user_screen,user_img;
    Long t_id,update_time;

    public String getUpdate_text() {
        return update_text;
    }

    public Long getUpdate_time() {
        return update_time;
    }

    public String getUser_img() {
        return user_img;
    }

    public String getUser_screen() {
        return user_screen;
    }

    public void setUpdate_text(String update_text) {
        this.update_text = update_text;
    }

    public void setUser_img(String user_img) {
        this.user_img = user_img;
    }

    public void setUpdate_time(Long update_time) {
        this.update_time = update_time;
    }

    public void setUser_screen(String user_screen) {
        this.user_screen = user_screen;
    }

    public void setT_id(Long t_id) {
        this.t_id = t_id;
    }

    public Long getT_id() {
        return t_id;
    }
}
