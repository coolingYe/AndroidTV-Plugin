package com.zwn.user.data.model;

public class UserCenterCategory {

    public String title;
    public int iconResId;
    public int iconSelectedResId;

    public UserCenterCategory(String title, int iconResId, int iconSelectedResId) {
        this.title = title;
        this.iconResId = iconResId;
        this.iconSelectedResId = iconSelectedResId;
    }
}
