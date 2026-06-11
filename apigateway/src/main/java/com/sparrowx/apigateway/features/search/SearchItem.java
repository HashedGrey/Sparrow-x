package com.sparrowx.apigateway.features.search;

import lombok.Getter;

@Getter
public class SearchItem {

    private String id;
    private String type;
    private String title;
    private String snippet;
    private String uri;
    private double score;

    public SearchItem() {
    }

    public SearchItem(String id, String type, String title, String snippet, String uri, double score) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.snippet = snippet;
        this.uri = uri;
        this.score = score;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setScore(double score) {
        this.score = score;
    }
}