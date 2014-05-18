package ru.ifmo.findmyfriend.drawer;

public class DrawerItem {
    private String title;
    private int icon;
    private String url;

    public DrawerItem(String title, int icon) {
        this.title = title;
        this.icon = icon;
    }

    public DrawerItem(String title, String url) {
        this(title, 0);
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public int getIcon() {
        return icon;
    }

    public String getUrl() {
        return url;
    }
}