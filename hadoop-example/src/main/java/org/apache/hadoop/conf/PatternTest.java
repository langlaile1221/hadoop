package org.apache.hadoop.conf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternTest {

  public static void main(String[] args) {

    // Get the regex to be checked
    String regex = "Geeks";

    // Create a pattern from regex
    Pattern pattern = Pattern.compile(regex);

    // Get the String to be matched
    String stringToBeMatched = "GeeksForGeeks";

    // Create a matcher for the input String
    Matcher matcher = pattern.matcher(stringToBeMatched);

    // Reset the Matcher using reset() method
    matcher = matcher.reset();

    // Get the current matcher state
    // java.util.regex.Matcher[pattern=Geeks region=0, 13 lastmatch=]
    System.out.println(matcher.toMatchResult());

    System.out.println("+++++++++++++++++++++++++");

  }

  public void testReset() {
    // Get the regex to be checked
    String regex = "GFG";

    // Create a pattern from regex
    Pattern pattern = Pattern.compile(regex);

    // Get the String to be matched
    String stringToBeMatched
        = "GFGFGFGFGFGFGFGFGFG";

    // Create a matcher for the input String
    Matcher matcher
        = pattern.matcher(stringToBeMatched);

    // Reset the Matcher using reset() method
    matcher = matcher.reset();

    // Get the current matcher state
    // java.util.regex.Matcher[pattern=GFG region=0, 19 lastmatch=]
    System.out.println(matcher.toMatchResult());
  }
}
