package com.alienvault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//@TODO: make SOPs logs instead
public class GithubRunner {
    public static Map<String, Integer> sortMapKeysByValue(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        final Comparator<Map.Entry<String, Integer>> c = new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        };
        Collections.sort(list, c);

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> anEntry : list) {
            //System.out.println("Adding k: " + anEntry.getKey() + " v: " + anEntry.getValue() + " to map");
            result.put(anEntry.getKey(), anEntry.getValue());
        }
        return result;
    }

    public static boolean isValidRepoOwnerList(final String[] repoOwnerList) {
        if (repoOwnerList == null || repoOwnerList.length == 0) {
            return false;
        }
        for (String repoOwner : repoOwnerList) {
            // @TODO: could have more exhaustive, cunning error checks for things like:
            // 1 - of the two tokens that exist, both are non-empty, of adequate length, etc 
            if (repoOwner == null || numSlashes(repoOwner) != 1) {
                System.out.println("repoOwner: " + repoOwner
                        + " is null, doesn't have a slash or isn't in the right format, bailing");
                return false;
            }
        }
        return true;
    }

    public static long numSlashes(final String s){
        if(s == null) {
            return 0;
        }
        return s.chars().filter(ch -> ch == '/').count();
    }

    //@TODO: add unit tests
    public static List<GithubIssue> sortByIssueDate(List<GithubIssue> list) {
            final Comparator<GithubIssue> c = new Comparator<GithubIssue>() {
        		@Override
        		public int compare(GithubIssue o1, GithubIssue o2) {
        			return o1.getCreated_at().compareTo(o2.getCreated_at());
        		}
            };
            Collections.sort(list, c);
            // System.out.println("Here's the sorted list I'll return:");
            // for(GithubIssue gi: list) {
            //     System.out.println("gi" + gi.getId() + " created_at: " + gi.getCreated_at());
            // }
            return list;
    }
}