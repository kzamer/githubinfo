import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.alienvault.GithubRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class GithubRunnerTest {
  @Test public void sortMapKeysByValueTest() {
    Map<String, Integer> mapToSort = new HashMap<>();
    mapToSort.put("kzamer/repoA", 5);
    mapToSort.put("kzamer/repoB", 10);
    mapToSort.put("kzamer/repoC", 15);
    mapToSort.put("kzamer/repoD", 20);

    List<Integer> sorted = new ArrayList<>();
    sorted.add(5);
    sorted.add(10);
    sorted.add(15);
    sorted.add(20);

    Map<String, Integer> resultMap = GithubRunner.sortMapKeysByValue(mapToSort);

    int sortedIndex = 0;
    for(Integer resultElement: resultMap.values()) {
      assertEquals(resultElement, sorted.get(sortedIndex));
      ++sortedIndex;
    }
  }

  @Test public void isValidRepoOwnerListTest() {
      final String[] repoOwnerList = {"kzamer/fun", "kzamer/meeting-feedback", "kzamer/minesweeper"};
      assertTrue("The repo owner list contains valid elements", GithubRunner.isValidRepoOwnerList(repoOwnerList));
  }

  @Test public void isValidRepoOwnerListMissingSlashTest() {
    final String[] repoOwnerList = {"badformat", "kzamer/meeting-feedback", "kzamer/minesweeper"};
    assertFalse("We need a slash in every repoOwner element", GithubRunner.isValidRepoOwnerList(repoOwnerList));
  }

  @Test public void isValidRepoOwnerListExtraSlashTest() {
    final String[] repoOwnerList = {"user/toomanyslashes/", "kzamer/minesweeper"};
    assertFalse("Only one slash allowed", GithubRunner.isValidRepoOwnerList(repoOwnerList));
  }

  @Test public void numSlashesTest() {
    String zeroSlashes = "kzamer";
    String oneSlash    = "kzamer/minesweeper";
    String twoSlashes  = "user/toomanyslashes/";
    
    assertEquals(0, GithubRunner.numSlashes(null));
    assertEquals(0, GithubRunner.numSlashes(zeroSlashes));
    assertEquals(1, GithubRunner.numSlashes(oneSlash));
    assertEquals(2, GithubRunner.numSlashes(twoSlashes));
  }

}