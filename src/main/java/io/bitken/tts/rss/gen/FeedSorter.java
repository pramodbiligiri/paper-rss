package io.bitken.tts.rss.gen;

import io.bitken.tts.rss.view.RssItem;
import java.util.Date;
import java.util.Optional;

public class FeedSorter {

    public static int sortByNewestItemFirst(RssItem x, RssItem y) {
        Optional<Date> acTimeOpt1 = x.getPubDateParsed();
        Optional<Date> acTimeOpt2 = y.getPubDateParsed();

        if (acTimeOpt1.isEmpty() && acTimeOpt2.isEmpty()) {
            return 0;
        }

        if (acTimeOpt1.isEmpty()) {
            return -1;
        }

        if (acTimeOpt2.isEmpty()) {
            return 1;
        }

        Date acTime1 = acTimeOpt1.get();
        Date acTime2 = acTimeOpt2.get();

        if (acTime1.after(acTime2)) {
            return 1;
        }

        if (acTime2.after(acTime1)) {
            return -1;
        }

        return 0;
    }

}
