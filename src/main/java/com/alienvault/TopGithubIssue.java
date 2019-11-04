package com.alienvault;

import java.util.Collection;

public class TopGithubIssue {
    private String day;
    private Collection<RepoIssueCount> occurrences;

    public TopGithubIssue(final String day, final Collection<RepoIssueCount> occurrences) {
        this.day = day;
        this.occurrences = occurrences;
    }

    public String getDay() {
        return day;
    }

    public Collection<RepoIssueCount> getOccurrences() {
        return occurrences;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for(RepoIssueCount ric: occurrences) {
            sb.append(ric.toString() + ", ");
        }
        String result = sb.substring(0, sb.length()-2);
        return "top_day: {\n" +
        "  day: \"" + day + "\",\n" +
        "  occurrences: {\n" + result + "\n  }\n}";
    }
}