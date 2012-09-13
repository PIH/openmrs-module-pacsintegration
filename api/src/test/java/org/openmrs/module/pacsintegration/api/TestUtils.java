package org.openmrs.module.pacsintegration.api;

import org.junit.Assert;

import java.util.regex.Pattern;

public class TestUtils {

    /**
     * Ignores white space. Ignores capitalization.
     */
    public static void assertFuzzyEquals(String expected, String actual) {
        if (expected == null && actual == null)
            return;
        if (expected == null || actual == null)
            Assert.fail(expected + " does not match " + actual);
        String test1 = stripWhitespaceAndConvertToLowerCase(expected);
        String test2 = stripWhitespaceAndConvertToLowerCase(actual);
        if (!test1.equals(test2)) {
            Assert.fail(expected + " does not match " + actual);
        }
    }


    /**
     * Tests whether the substring is contained in the actual string. Allows for inclusion of
     * regular expressions in the substring.
     */
    public static void assertContains(String substring, String actual) {
        if (substring == null) {
            return;
        }
        if (actual == null) {
            Assert.fail(substring + " is not contained in " + actual);
        }

        if (!actual.contains(substring)) {
            Assert.fail(substring + " is not contained in " + actual);
        }
    }

    /**
     * Tests whether the substring is contained in the actual string. Allows for inclusion of
     * regular expressions in the substring. Ignores white space. Ignores capitalization.
     */
    public static void assertFuzzyContains(String substring, String actual) {
        if (substring == null) {
            return;
        }
        if (actual == null) {
            Assert.fail(substring + " is not contained in " + actual);
        }

        if (!Pattern.compile(stripWhitespaceAndConvertToLowerCase(substring), Pattern.DOTALL).matcher(stripWhitespaceAndConvertToLowerCase(actual)).find()) {
            Assert.fail(substring + " is not contained in " + actual);
        }
    }

    /**
     * Tests whether the substring is NOT contained in the actual string. Allows for inclusion of
     * regular expressions in the substring. Ignores white space.
     */
    public static void assertFuzzyDoesNotContain(String substring, String actual) {
        if (substring == null) {
            return;
        }
        if (actual == null) {
            return;
        }

        if (Pattern.compile(stripWhitespaceAndConvertToLowerCase(substring), Pattern.DOTALL).matcher(stripWhitespaceAndConvertToLowerCase(actual)).find()) {
            Assert.fail(substring + " found in  " + actual);
        }
    }


    private static String stripWhitespaceAndConvertToLowerCase(String string) {
        string = string.toLowerCase();
        string = string.replaceAll("\\s", "");
        return string;
    }

}
