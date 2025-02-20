package io.bitken.tts.rss.view;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RssItemTest {

    @Test
    public void testAbstractFmt1() {
        StringBuilder res = RssItem.getAbstract(" \n  Some text\n\n \n");
        Assertions.assertEquals("Some text", res.toString());
    }

    @Test
    public void testTitleFmt1() {
        StringBuilder res = RssItem.formatTitle(" This is some      title  \n\n\n");
        Assertions.assertEquals("This is some title", res.toString());

        StringBuilder res2 = RssItem.formatTitle("Title unchanged");
        Assertions.assertEquals("Title unchanged", res2.toString());

    }
}
