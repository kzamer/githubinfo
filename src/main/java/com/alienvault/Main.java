package com.alienvault;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;


/**
 * GitHub Issues -------------
 *
 * Create a program that generates a report about the the Issues belonging to a
 * list of github repositories ordered by creation time, and information about
 * the day when most Issues were created.
 *
 * Input: ----- List of 1 to n Strings with Github repositories references with
 * the format "owner/repository"
 *
 *
 * Output: ------ String representation of a Json dictionary with the following
 * content:
 *
 * - "issues": List containing all the Issues related to all the repositories
 * provided. The list should be ordered by the Issue "created_at" field (From
 * oldest to newest) Each entry of the list will be a dictionary with basic
 * Issue information: "id", "state", "title", "repository" and "created_at"
 * fields. Issue entry example: { "id": 1, "state": "open", "title": "Found a
 * bug", "repository": "owner1/repository1", "created_at":
 * "2011-04-22T13:33:48Z" }
 *
 * - "top_day": Dictionary with the information of the day when most Issues were
 * created. It will contain the day and the number of Issues that were created
 * on each repository this day If there are more than one "top_day", the latest
 * one should be used. example: { "day": "2011-04-22", "occurrences": {
 * "owner1/repository1": 8, "owner2/repository2": 0, "owner3/repository3": 2 } }
 *
 *
 * Output example: --------------
 *
 * {
 * "issues": [ { "id": 38, "state": "open", "title": "Found a bug",
 * "repository": "owner1/repository1", "created_at": "2011-04-22T13:33:48Z" }, {
 * "id": 23, "state": "open", "title": "Found a bug 2", "repository":
 * "owner1/repository1", "created_at": "2011-04-22T18:24:32Z" }, { "id": 24,
 * "state": "closed", "title": "Feature request", "repository":
 * "owner2/repository2", "created_at": "2011-05-08T09:15:20Z" } ], "top_day": {
 * "day": "2011-04-22", "occurrences": { "owner1/repository1": 2,
 * "owner2/repository2": 0 } } }
 *
 * --------------------------------------------------------
 *
 * You can create the classes and methods you consider. You can use any library
 * you need. Good modularization, error control and code style will be taken
 * into account. Memory usage and execution time will be taken into account.
 *
 * Good Luck!
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
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        System.out.println("Let's code!");
        final String[] repoOwnerList = {"kzamer/fun", "kzamer/meeting-feedback", "kzamer/minesweeper"};

        if(!isValidRepoOwnerList(repoOwnerList)) {
            System.out.println("One or more inputs was not in the expected format of 'owner/repo'");
        }
        // validate input

        callRESTv3(repoOwnerList);
        //callGraphQL();
        System.out.println("Elapsed time: " + (System.currentTimeMillis()-startTime) + "ms");
    }

    public static boolean isValidRepoOwnerList(final String[] repoOwnerList) {
        if(repoOwnerList == null) {
            return false;
        }
        for(String repoOwner: repoOwnerList) {
            if(repoOwner == null || 
               repoOwner.indexOf("/") == -1 ||
               repoOwner.split("/").length != 2) {
                   System.out.println("repoOwner: " + repoOwner + " is null, doesn't have a slash or isn't in the right format, bailing");
                   return false;
               }
        }
        return true;
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

    public static Map<String, Integer> sortByValue(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        final Comparator<Map.Entry<String, Integer>> c = new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
        };
        Collections.sort(list, c);

        Map<String, Integer> result = new LinkedHashMap<>();
        for(Map.Entry<String, Integer> anEntry: list) {
            System.out.println("Adding k: " + anEntry.getKey() + " v: " + anEntry.getValue() + " to map");
            result.put(anEntry.getKey(), anEntry.getValue());
        }
        return result;
    }

    public static List<GithubIssue> sortByIssueDate(List<GithubIssue> list) {
        final Comparator<GithubIssue> c = new Comparator<GithubIssue>() {
			@Override
			public int compare(GithubIssue o1, GithubIssue o2) {
				return o1.getCreated_at().compareTo(o2.getCreated_at());
			}
        };
        Collections.sort(list, c);
        System.out.println("Here's the sorted list I'll return:");
        for(GithubIssue gi: list) {
            System.out.println("gi" + gi.getId() + " created_at: " + gi.getCreated_at());
        }
        return list;
    }

    public static void callRESTv3(final String[] repoOwnerList) {
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
                System.out.println("Here's what I have: owner: " + owner + " repo: " + repo);

                GHUser user = github.getUser(owner);
                GHRepository ghRepo = user.getRepository(repo);
                List<GHIssue> repoIssues = ghRepo.getIssues(GHIssueState.ALL, null);
                System.out.println("Here are my issues for repo: " + repo + ":");
                for(GHIssue issue: repoIssues) {
                    // use DateFormat to get simple date
                    String pattern = "yyyy-MM-dd";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                    String formattedDate = simpleDateFormat.format(issue.getCreatedAt());
                    System.out.println("Here's formattedDate: " + formattedDate);

                    GithubIssue newIssue = new GithubIssue(issue.getId(),
                                                           issue.getState().name(),
                                                           issue.getTitle(),
                                                           issue.getRepository().getFullName(), 
                                                           issue.getCreatedAt(),
                                                           formattedDate);
                    allIssues.add(newIssue);
                    System.out.println("Added issue: id: " + newIssue.getId() + 
                        " state: " + newIssue.getStatus() + " title: " + newIssue.getTitle() + " repo: " + newIssue.getRepository() +
                        " created_at: " + newIssue.getCreated_at() +
                        " pretty_created_at: " + newIssue.getPretty_created_at());
                  
                    System.out.println("formatted date: " + formattedDate);
                    // print out the formatted date
                    if(dayIssuesMap.get(formattedDate) == null) {
                        System.out.println("Starting a date at 1");
                        dayIssuesMap.put(formattedDate, 1);
                    } else {
                        System.out.println("Adding to existing date");
                        dayIssuesMap.put(formattedDate, dayIssuesMap.get(formattedDate)+1);
                    }
                    //dayIssuesMap.put(issue.getCreatedAt().getYear()
                    System.out.println("id: " + newIssue.getId() + 
                                       " state: " + newIssue.getStatus() + 
                                       " title: " + newIssue.getTitle() + 
                                       " repository: " + newIssue.getRepository() + 
                                       " created_at: " + newIssue.getCreated_at());
                    // could also build up the list of issues by day, repo... maybe a heap
                }
            }
            System.out.println("Here are my total issues across all repos: " + allIssues.size());
            StringBuilder allIssuesSB = new StringBuilder();
            allIssues = sortByIssueDate(allIssues);
            allIssuesSB.append("\"issues\": [\n");
            for(GithubIssue githubIssue: allIssues) {
                System.out.println("id: " + githubIssue.getId() + 
                                       " state: " + githubIssue.getStatus() + 
                                       " title: " + githubIssue.getTitle() + 
                                       " repository: " + githubIssue.getRepository() + 
                                       " created_at: " + githubIssue.getCreated_at());
                allIssuesSB.append("{" + githubIssue + "},\n");
            }
            String allIssuesFakeJSON = allIssuesSB.substring(0, allIssuesSB.length()-2) + "]\n";
            System.out.println("allIssuesFakeJSON: " + allIssuesFakeJSON);

            dayIssuesMap.put("2011-01-05", 3);
            dayIssuesMap.put("2012-02-14", 1);
            dayIssuesMap.put("2013-03-15", 2);
            System.out.println("Before:");
            for(String dayKey: dayIssuesMap.keySet()) {
                System.out.println("day: " + dayKey + " count: " + dayIssuesMap.get(dayKey));
            }

            dayIssuesMap = sortByValue(dayIssuesMap);
            System.out.println("After:");
            for(String dayKey: dayIssuesMap.keySet()) {
                System.out.println("day: " + dayKey + " count: " + dayIssuesMap.get(dayKey));
            }
            String maxDay = getMaxDay(dayIssuesMap);
            System.out.println("maxDay: " + maxDay + " issues: " + dayIssuesMap.get(maxDay));
            // find the top day, along with occurrences for each day, multiple passes right now, but at least get the right answer first
            
            // given top day, find issue counts for each repo by searching entire list again. slow/dumb but get answer first.
            // day, occurrences: { "owner/repo": count, etc }
            // for a specific day, go find all repos and add things up
            Map<String, RepoIssueCount> repoIssuesForDay = new HashMap<>();
            for(GithubIssue githubIssue: allIssues) {
                System.out.println("Checking issue: " + githubIssue.getId() + " maxDay: " + " pretty_created_at: " + githubIssue.getPretty_created_at());
                if(maxDay.equals(githubIssue.getPretty_created_at())) {
                    final String repo = githubIssue.getRepository();
                    // we found a day we care about, so let's add our repoissue
                    if(repoIssuesForDay.get(repo) == null) {
                        System.out.println("Creating new issue count");
                        RepoIssueCount ric = new RepoIssueCount(repo, 1);
                        repoIssuesForDay.put(repo, ric);
                    } else {
                        System.out.println("Adding to existing issue count");
                        RepoIssueCount ric = repoIssuesForDay.get(repo);
                        ric.setIssueCount(ric.getIssueCount()+1);
                        repoIssuesForDay.put(repo, ric);
                    }
                }
            }

            System.out.println("Here are the issues for our maxDay: ");
            for(RepoIssueCount ric: repoIssuesForDay.values()) {
                System.out.println("repo: " + ric.getRepo() + " count: " + ric.getIssueCount());
            }



            TopGithubIssue topIssue = new TopGithubIssue(maxDay, repoIssuesForDay.values());
            System.out.println("topIssue: " + topIssue);

            StringBuilder fakeResponse = new StringBuilder();
            fakeResponse.append("{\n" +
                                allIssuesFakeJSON + ",\n" +
                                topIssue + "\n" + 
                                "}\n");
            System.out.println("Here's my fake response: " + fakeResponse);

        } catch (IOException ioe) {
            System.out.println("Had an ioe: " + ioe);
            ioe.printStackTrace();
        }

        // get issues for each repo

    }

    // https://www.graphql-java.com/documentation/master/
    // https://stackoverflow.com/questions/42024158/how-to-access-github-graphql-api-using-java
    public static void callGraphQL() {
        String schema = "type Query{hello: String} schema{query: Query}";

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

        RuntimeWiring runtimeWiring = newRuntimeWiring()
                .type("Query", builder -> builder.dataFetcher("hello", new StaticDataFetcher("bananas")))
                .build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
        ExecutionResult executionResult = build.execute("{hello}");

        System.out.println(executionResult.getData().toString());
        // Prints: {hello=world}
    }

}
