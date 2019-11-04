package com.alienvault;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;


/**
 * Approach: get it working as crude as possible, then clean up given the time allowed.
 * Given more time I could continue to refactor/modularize and write more unit tests.
 * I would also consider a more efficient approach to store issues for each repo so that there is 
 * less linear O(N) recounting of issues for the reporting that's returned.
 * I would also make proper REST endpoints and given more time, use GraphQL. Spent a little time
 * with GQL early on and decided to fall back to the v3 REST interface instead.
 */
public class Main {

    /**
     * @param args String array with Github repositories with the format
     * "owner/repository"
     *
     */
    // personal access token: 5d50e3c4977ebc5e747df4d07a02efb711c16af8
    // args: ["owner/repo"]
    // https://graphql.org/code/#java
    public static void main(final String[] args) {
        long startTime = System.currentTimeMillis();
        System.out.println("Let's code!");
        final String[] repoOwnerList = args; //{"kzamer/fun", "kzamer/meeting-feedback", "kzamer/minesweeper"};

        // validate input
        if(!GithubRunner.isValidRepoOwnerList(repoOwnerList)) {
            System.out.println("One or more inputs was not in the expected format of 'owner/repo'");
            return;
        }
        
        String result = callRESTv3(repoOwnerList);
        System.out.println(result);
        System.out.println("Elapsed time: " + (System.currentTimeMillis()-startTime) + "ms");
    }

    // given a sorted map, return the max day
    // this is crude, but will improve later
    public static String getMaxDay(Map<String, Integer> map) {
        String day = "";
        for(String aDay : map.keySet()) {
            day = aDay;
        }
        return day;
    }

    // @TODO: Of course move this to GithubRunner or similar class outside of Main
    public static String callRESTv3(final String[] repoOwnerList) {
        try {
            GitHub github = GitHub.connect("kzamer", "5d50e3c4977ebc5e747df4d07a02efb711c16af8"); 
            List<GithubIssue> allIssues = new ArrayList<>();
            Map<String, Integer> dayIssuesMap = new HashMap<>();

            // list with all issues in it
            // create top_day dictionary with highest day in it, comparator to sort based on day
            for(String repoOwner: repoOwnerList) {
                String[] tokens = repoOwner.split("/");
                String owner = tokens[0];
                String repo = tokens[1];
                //System.out.println("Here's what I have: owner: " + owner + " repo: " + repo);

                GHUser user = github.getUser(owner);
                GHRepository ghRepo = user.getRepository(repo);
                List<GHIssue> repoIssues = ghRepo.getIssues(GHIssueState.ALL, null);
                //System.out.println("Here are my issues for repo: " + repo + ":");
                for(GHIssue issue: repoIssues) {
                    // @TODO: move this into its own method
                    String pattern = "yyyy-MM-dd";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                    String formattedDate = simpleDateFormat.format(issue.getCreatedAt());
                    //System.out.println("Here's formattedDate: " + formattedDate);

                    GithubIssue newIssue = new GithubIssue(issue.getId(),
                                                           issue.getState().name(),
                                                           issue.getTitle(),
                                                           issue.getRepository().getFullName(), 
                                                           issue.getCreatedAt(),
                                                           formattedDate);
                    allIssues.add(newIssue);
                    // System.out.println("Added issue: id: " + newIssue.getId() + 
                    //     " state: " + newIssue.getStatus() + " title: " + newIssue.getTitle() + " repo: " + newIssue.getRepository() +
                    //     " created_at: " + newIssue.getCreated_at() +
                    //     " pretty_created_at: " + newIssue.getPretty_created_at());
                  
                    // System.out.println("formatted date: " + formattedDate);
                    // print out the formatted date
                    if(dayIssuesMap.get(formattedDate) == null) {
                        //System.out.println("Starting a date at 1");
                        dayIssuesMap.put(formattedDate, 1);
                    } else {
                        //System.out.println("Adding to existing date");
                        dayIssuesMap.put(formattedDate, dayIssuesMap.get(formattedDate)+1);
                    }
                    // System.out.println("id: " + newIssue.getId() + 
                    //                    " state: " + newIssue.getStatus() + 
                    //                    " title: " + newIssue.getTitle() + 
                    //                    " repository: " + newIssue.getRepository() + 
                    //                    " created_at: " + newIssue.getCreated_at());
                    // @TODO: could also build up the list of issues by day, repo... maybe a heap
                }
            }
            //System.out.println("Here are my total issues across all repos: " + allIssues.size());
            StringBuilder allIssuesSB = new StringBuilder();
            allIssues = GithubRunner.sortByIssueDate(allIssues);
            allIssuesSB.append("\"issues\": [\n");
            for(GithubIssue githubIssue: allIssues) {
                // System.out.println("id: " + githubIssue.getId() + 
                //                        " state: " + githubIssue.getStatus() + 
                //                        " title: " + githubIssue.getTitle() + 
                //                        " repository: " + githubIssue.getRepository() + 
                //                        " created_at: " + githubIssue.getCreated_at());
                allIssuesSB.append("{" + githubIssue + "},\n");
            }
            String allIssuesFakeJSON = allIssuesSB.substring(0, allIssuesSB.length()-2) + "]\n";
            // System.out.println("allIssuesFakeJSON: " + allIssuesFakeJSON);

            // dayIssuesMap.put("2011-01-05", 3);
            // dayIssuesMap.put("2012-02-14", 1);
            // dayIssuesMap.put("2013-03-15", 2);
            // System.out.println("Before:");
            // for(String dayKey: dayIssuesMap.keySet()) {
            //     System.out.println("day: " + dayKey + " count: " + dayIssuesMap.get(dayKey));
            // }

            dayIssuesMap = GithubRunner.sortMapKeysByValue(dayIssuesMap);
            // System.out.println("After:");
            // for(String dayKey: dayIssuesMap.keySet()) {
            //     System.out.println("day: " + dayKey + " count: " + dayIssuesMap.get(dayKey));
            // }
            String maxDay = getMaxDay(dayIssuesMap);
            //System.out.println("maxDay: " + maxDay + " issues: " + dayIssuesMap.get(maxDay));
            // find the top day, along with occurrences for each day, multiple passes right now, but at least get the right answer first
            
            // given top day, find issue counts for each repo by searching entire list again. slow/dumb but get answer first.
            // day, occurrences: { "owner/repo": count, etc }
            // for a specific day, go find all repos and add things up
            Map<String, RepoIssueCount> repoIssuesForDay = new HashMap<>();
            for(GithubIssue githubIssue: allIssues) {
                //System.out.println("Checking issue: " + githubIssue.getId() + " maxDay: " + " pretty_created_at: " + githubIssue.getPretty_created_at());
                if(maxDay.equals(githubIssue.getPretty_created_at())) {
                    final String repo = githubIssue.getRepository();
                    // we found a day we care about, so let's add our repoissue
                    if(repoIssuesForDay.get(repo) == null) {
                        //System.out.println("Creating new issue count");
                        RepoIssueCount ric = new RepoIssueCount(repo, 1);
                        repoIssuesForDay.put(repo, ric);
                    } else {
                        //System.out.println("Adding to existing issue count");
                        RepoIssueCount ric = repoIssuesForDay.get(repo);
                        ric.setIssueCount(ric.getIssueCount()+1);
                        repoIssuesForDay.put(repo, ric);
                    }
                }
            }

            // System.out.println("Here are the issues for our maxDay: ");
            // for(RepoIssueCount ric: repoIssuesForDay.values()) {
            //     System.out.println("repo: " + ric.getRepo() + " count: " + ric.getIssueCount());
            // }



            TopGithubIssue topIssue = new TopGithubIssue(maxDay, repoIssuesForDay.values());
            //System.out.println("topIssue: " + topIssue);

            StringBuilder fakeResponse = new StringBuilder();
            fakeResponse.append("{\n" +
                                allIssuesFakeJSON + ",\n" +
                                topIssue + "\n" + 
                                "}\n");
            //System.out.println("Here's my fake response: " + fakeResponse);
            return fakeResponse.toString();

        } catch (IOException ioe) {
            System.out.println("Had an ioe: " + ioe);
            ioe.printStackTrace();
        }
        return "";
    }
}
