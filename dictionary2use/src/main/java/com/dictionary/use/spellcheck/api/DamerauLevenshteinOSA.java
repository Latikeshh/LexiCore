package com.dictionary.use.spellcheck.api;

/**
 * Optimal String Alignment (OSA) Damerau-Levenshtein distance algorithm.
 */
public class DamerauLevenshteinOSA implements StringDistance {

    private final CharComparator charComparator;

    public DamerauLevenshteinOSA() {
        this.charComparator = new DefaultCharComparator();
    }

    public DamerauLevenshteinOSA(CharComparator charComparator) {
        this.charComparator = charComparator != null ? charComparator : new DefaultCharComparator();
    }

    @Override
    public int distanceWithEarlyStop(String baseString, String string2, int maxDistance) {
        if (baseString == null) return string2 == null ? 0 : string2.length();
        if (string2 == null || string2.isEmpty()) return baseString.length();
        if (maxDistance == 0) return baseString.equals(string2) ? 0 : -1;

        int[] baseChar1Costs = new int[baseString.length()];
        int[] basePrevChar1Costs = new int[baseString.length()];

        String string1;
        if (baseString.length() > string2.length()) {
            string1 = string2;
            string2 = baseString;
        } else {
            string1 = baseString;
        }
        int str1Len = string1.length();
        int str2Len = string2.length();

        while ((str1Len > 0) && (string1.charAt(str1Len - 1) == string2.charAt(str2Len - 1))) {
            str1Len--;
            str2Len--;
        }

        int start = 0;
        if ((string1.charAt(0) == string2.charAt(0)) || (str1Len == 0)) {
            while ((start < str1Len) && (string1.charAt(start) == string2.charAt(start))) start++;

            str1Len -= start;
            str2Len -= start;

            if (str1Len == 0) {
                return str2Len;
            }

            string2 = string2.substring(start, start + str2Len);
        }

        int lenDiff = str2Len - str1Len;

        if ((maxDistance < 0) || (maxDistance > str2Len)) {
            maxDistance = str2Len;
        } else if (lenDiff > maxDistance) {
            return -1;
        }

        if (str2Len > baseChar1Costs.length) {
            baseChar1Costs = new int[str2Len];
            basePrevChar1Costs = new int[str2Len];
        } else {
            for (int i = 0; i < str2Len; i++) {
                basePrevChar1Costs[i] = 0;
            }
        }

        for (int j = 0; j < str2Len; j++) {
            if (j < maxDistance) {
                baseChar1Costs[j] = j + 1;
            } else {
                baseChar1Costs[j] = maxDistance + 1;
            }
        }

        int jStartOffset = maxDistance - (str2Len - str1Len);
        boolean haveMax = maxDistance < str2Len;
        int jStart = 0;
        int jEnd = maxDistance;
        char str1Char = string1.charAt(0);
        int current = 0;

        for (int i = 0; i < str1Len; i++) {
            char prevStr1Char = str1Char;
            str1Char = string1.charAt(start + i);
            char str2Char = string2.charAt(0);
            int left = i;
            current = left + 1;
            int nextTransCost = 0;

            jStart += (i > jStartOffset) ? 1 : 0;
            jEnd += (jEnd < str2Len) ? 1 : 0;

            for (int j = jStart; j < jEnd; j++) {
                int above = current;
                int thisTransCost = nextTransCost;
                nextTransCost = basePrevChar1Costs[j];
                basePrevChar1Costs[j] = current = left;
                left = baseChar1Costs[j];
                char prevStr2Char = str2Char;
                str2Char = string2.charAt(j);

                if (charComparator.areDistinct(str1Char, str2Char)) {
                    if (left < current) current = left;
                    if (above < current) current = above;
                    current++;
                    if ((i != 0) && (j != 0)
                            && charComparator.areEqual(str1Char, prevStr2Char)
                            && charComparator.areEqual(prevStr1Char, str2Char)) {
                        thisTransCost++;
                        if (thisTransCost < current) current = thisTransCost;
                    }
                }
                baseChar1Costs[j] = current;
            }
            if (haveMax && (baseChar1Costs[i + lenDiff] > maxDistance)) return -1;
        }

        return (current <= maxDistance) ? current : -1;
    }
}
