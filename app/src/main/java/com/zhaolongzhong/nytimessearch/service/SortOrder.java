package com.zhaolongzhong.nytimessearch.service;

public enum SortOrder {
    NEWEST(0, "Newest", "newest"),
    OLDEST(1, "Oldest", "oldest"),
    ASC(2, "Ascending", "asc"),
    DESC(3, "Descending", "desc");

    private int id;
    private String name;
    private String value;

    SortOrder(int id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public static SortOrder instanceFromId(int id) {
        for (SortOrder sortOrder : values()) {
            if (id == sortOrder.id) {
                return sortOrder;
            }
        }

        return NEWEST;
    }

    public static SortOrder instanceFromName(String name) {
        for (SortOrder sortOrder : values()) {
            if (name.equals(sortOrder.name)) {
                return sortOrder;
            }
        }

        return NEWEST;
    }
}
