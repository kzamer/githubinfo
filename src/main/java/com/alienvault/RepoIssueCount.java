package com.alienvault;

public class RepoIssueCount {
    private String repo;
    private int issueCount;

    public RepoIssueCount(String repo, int issueCount) {
        this.repo = repo;
        this.issueCount = issueCount;
    }

    public String getRepo() {
        return repo;
    }

    public int getIssueCount() {
        return issueCount;
    }

    public void setIssueCount(final int issueCount) {
        this.issueCount = issueCount;
    }

    @Override public String toString() {
        return "\"" + repo + "\"" + ":" + issueCount;
    }
}
