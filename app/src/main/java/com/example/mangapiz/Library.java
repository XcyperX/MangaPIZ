package com.example.mangapiz;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Library {
    @SerializedName("url")
    public String url;
    @SerializedName("chapter_name")
    public   String chapter_name;
    @SerializedName("number")
    public String number;
    @SerializedName("src")
    public List<String> src;

//    public Library(String url, String chapter_name, int number, List<String> src) {
//        this.url = url;
//        this.chapter_name = chapter_name;
//        this.number = number;
//        this.src = src;
//    }
}
