package ru.ifmo.findmyfriend.drawer;

public class DrawerItem {
    private String title;
    private int iconResource;
    private String url;

    public DrawerItem(String title, int iconResource) {
        this.title = title;
        this.iconResource = iconResource;
    }

    public DrawerItem(String title, String url) {
        this(title, 0);
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResource() {
        return iconResource;
    }

    public String getUrl() {
        return url;
    }
}