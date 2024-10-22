package com.mvnh.rythmap.responses.yandex.entities;

import java.util.List;

public class YandexInfo {
    private String id;
    private String title;
    private List<Artist> artists;

    public String getTitle() {
        return title;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public static class Artist {
        private String name;

        public String getName() {
            return name;
        }
    }
}

