package com.alperez.geekbooks.crowler.utils;

public class XmlTagExtractor {


    private final char htmlChars[];

    public XmlTagExtractor(String html) {
        this.htmlChars = html.toCharArray();
    }

    @Nullable
    public String getTag(String tagName, final int startIndex) {
        final char[] startTag = ("<"+tagName).toCharArray();
        final char[] endTag = String.format("</%s>", tagName).toCharArray();

        final int endIndex = htmlChars.length - endTag.length;

        int actualStart = -1;
        int actualEnd = -1;
        int nTagsOpen = 0;

        for (int index = startIndex; index < endIndex; index++) {
            if (actualStart < 0) {
                if (eq(htmlChars, index, startTag)) {
                    actualStart = index;
                    nTagsOpen ++;
                }
            } else if (eq(htmlChars, index, startTag)) {
                nTagsOpen ++;
            } else if (eq(htmlChars, index, endTag)) {
                nTagsOpen --;
                if (nTagsOpen == 0) {
                    actualEnd = index + endTag.length;
                    break;
                }
            }
        }

        return ((actualStart >= 0) && (nTagsOpen == 0)) ? new String(htmlChars, actualStart, (actualEnd - actualStart)) : null;
    }



    private boolean eq(char[] src, int srcIndex, char[] cmp) {
        int n = cmp.length;
        for (int i=0; i<n; i++) {
            if (src[srcIndex ++] != cmp[i]) return false;
        }
        return true;
    }
}
