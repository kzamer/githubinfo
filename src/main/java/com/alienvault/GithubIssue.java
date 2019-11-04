package com.alienvault;

import java.util.Date;

// { "id": 1, "state": "open", "title": "Found a bug", "repository": "owner1/repository1", "created_at": "2011-04-22T13:33:48Z" }

/*@Value @AllArgsConstructor */public class GithubIssue {
	private long id;
    private String status;
    private String title;
    private String repository;
    private Date created_at;
    private String pretty_created_at;

    public GithubIssue(final long id, final String status, final String title, final String repository, Date created_at, String pretty_created_at) {
        this.id = id;
        this.status = status;
        this.title = title;
        this.repository = repository;
        this.created_at = created_at;
        this.pretty_created_at = pretty_created_at;
    }

    public long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getRepository() {
        return repository;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public String getPretty_created_at() {
        return pretty_created_at;
    }

    @Override public String toString() {
        return "id: " + id + ",\n" + 
               "state: " + status + ",\n" +
               "title: " + title + ",\n" + 
               "repository: " + repository + ",\n" +
               "created_at: " + created_at;
    }
}