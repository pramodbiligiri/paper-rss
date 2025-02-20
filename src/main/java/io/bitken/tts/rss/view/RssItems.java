package io.bitken.tts.rss.view;

import io.bitken.tts.model.entity.PaperData;
import io.bitken.tts.rss.gen.FeedSorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class RssItems {

    private static final Logger LOG = LoggerFactory.getLogger(RssItems.class);

    public static List<RssItem> from(List<PaperData> papers) {
        List<RssItem> rssItems = new ArrayList<>();

        for (PaperData paper : papers) {
            RssItem pi = RssItem.from(paper);
            rssItems.add(pi);
        }

        return rssItems;
    }

    public static List<RssItem> getMostRecentItems(List<RssItem> inputItems, int n) {
        if (n <= 0) {
            return new ArrayList<>();
        }

        List<RssItem> retval = new ArrayList<>();

        List<RssItem> items = new ArrayList<>(inputItems);
        items.sort(FeedSorter::sortByNewestItemFirst);

        for (int i = 0; i < n && i < items.size(); i++) {
            retval.add(items.get(i));
        }

        LOG.info("Getting " + retval.size() + " items from previous");

        return retval;
    }

    public static Optional<Date> getPubDateOfMostRecentItem(List<RssItem> items) {
        Date currentMaxDate = null;

        for (RssItem item : items) {
            Optional<Date> itemDateOpt = item.getPubDateParsed();
            if (itemDateOpt.isEmpty()) {
                continue;
            }

            Date itemDate = itemDateOpt.get();
            if (currentMaxDate == null) {
                currentMaxDate = itemDate;
                continue;
            }

            if (itemDate.after(currentMaxDate)) {
                currentMaxDate = itemDate;
            }
        }

        if (currentMaxDate == null) {
            return Optional.empty();
        }

        return Optional.of(currentMaxDate);
    }
}
