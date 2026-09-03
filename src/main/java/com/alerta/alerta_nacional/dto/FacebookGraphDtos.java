package com.alerta.alerta_nacional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public class FacebookGraphDtos {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FeedResponse {
        private List<PostItem> data;
        private Paging paging;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostItem {
        private String id;
        private String message;
        private String story;

        @JsonProperty("created_time")
        private String createdTime;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Paging {
        private String next;
        private String previous;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeleteResponse {
        private boolean success;
    }
}
