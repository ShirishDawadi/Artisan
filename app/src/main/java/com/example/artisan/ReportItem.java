package com.example.artisan;

public class ReportItem {
    private String id;
    private String name;
    private int count;
    private boolean isProfile;

    public ReportItem(String id, String name, int count, boolean isProfile) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.isProfile = isProfile;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getCount() { return count; }
    public boolean isProfile() { return isProfile; }
}

