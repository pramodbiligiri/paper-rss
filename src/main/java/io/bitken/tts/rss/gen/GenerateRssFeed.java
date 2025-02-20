package io.bitken.tts.rss.gen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.bitken.tts.model.domain.CategoryInfo;
import io.bitken.tts.model.entity.PaperData;
import io.bitken.tts.model.entity.RssFeed;
import io.bitken.tts.repo.PaperDataRepo;
import io.bitken.tts.repo.RssFeedRepo;
import io.bitken.tts.rss.view.RssChannel;
import io.bitken.tts.rss.view.RssFeedView;
import io.bitken.tts.rss.view.RssItem;
import io.bitken.tts.rss.view.RssItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.*;

@Component
public class GenerateRssFeed {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateRssFeed.class);

    @Autowired
    PaperDataRepo pdRepo;

    @Autowired
    RssFeedRepo rssRepo;

    @Value("${feed.channel.image.baseurl}")
    private String imgBaseUrl;

    public Optional<RssFeed> generateFeed(CategoryInfo cat, int maxItemsInFeed, XmlMapper xmlMapper)
            throws JsonProcessingException {

        List<RssFeed> mostRecentFeeds = rssRepo.findLatestFeed(cat.getDbTag());

        List<String> cats = Arrays.asList(cat.getArxivCats());
        List<RssItem> rssItems;

        if (mostRecentFeeds.isEmpty()) {
            LOG.info("Previous feed was empty");
            rssItems = fetchOnlyNewItems(cats, maxItemsInFeed);
        } else {
            LOG.info("May include items from previous feed");
            rssItems = includeItemsFromPreviousFeedIfNeeded(xmlMapper, mostRecentFeeds.get(0), cats, maxItemsInFeed);
        }

        if (rssItems.isEmpty()) {
            LOG.info("Feed items list is empty, so not creating a new feed.");
            return Optional.empty();
        }

        LOG.info("Creating new feed with " + rssItems.size() + " items");
        return Optional.of(createFeedEntity(cat, xmlMapper, rssItems));
    }

    private List<RssItem> fetchOnlyNewItems(List<String> cats, int maxItems) {
        List<PaperData> newPapers = getPapersNewerThanPubDateForCategory(cats, maxItems);
        if (newPapers.isEmpty()) {
            LOG.info("No new papers in category");
            return new ArrayList<>();
        }

        return RssItems.from(newPapers);
    }

    private List<RssItem> includeItemsFromPreviousFeedIfNeeded(XmlMapper mapper, RssFeed mostRecentFeed,
                   List<String> cats, int maxItemsInFeed) throws JsonProcessingException {
        List<RssItem> prevItems = getPrevItems(mapper, mostRecentFeed);

        Optional<Date> latestPubDateInPrevFeedOpt = RssItems.getPubDateOfMostRecentItem(prevItems);

        if (latestPubDateInPrevFeedOpt.isEmpty()) {
            LOG.info("Ignoring previous feed because could not obtain any item pubdate from it");

            return fetchOnlyNewItems(cats, maxItemsInFeed);
        }

        Date latestPubDateInPrevFeed = latestPubDateInPrevFeedOpt.get();
        LOG.info("Fetching papers with pubdate > " + latestPubDateInPrevFeed);

        List<PaperData> newPapers = getPapersNewerThanPubDateForCategory(cats, latestPubDateInPrevFeed);
        if (newPapers.isEmpty()) {
            LOG.info("No newer papers compared to newest in previous feed. So returning empty list.");
            return new ArrayList<>();
        }

        LOG.info("No. of new papers: " + newPapers.size());
        if (newPapers.size() >= maxItemsInFeed) {
            LOG.info("Found sufficient no. of new papers(" + newPapers.size() + "), so no items from previous.");

            // DB gives them by newest item first, so choose the older ones at the end of the list.
            return RssItems.from(newPapers.subList(newPapers.size() - maxItemsInFeed, newPapers.size()));
        }

        return includeSomePrevItems(prevItems, newPapers, maxItemsInFeed);
    }

    private List<RssItem> includeSomePrevItems(List<RssItem> prevItems, List<PaperData> newPapers, int maxItemsInFeed) {
        List<RssItem> someOldItems = RssItems.getMostRecentItems(prevItems, maxItemsInFeed - newPapers.size());
        List<RssItem> rssItems = new ArrayList<>(someOldItems);

        List<RssItem> newItems = RssItems.from(newPapers);
        rssItems.addAll(newItems);

        return rssItems;
    }

    private RssFeed createFeedEntity(CategoryInfo cat, XmlMapper mapper, List<RssItem> rssItems) throws JsonProcessingException {
        RssFeedView feedView = RssFeedView.createForCategory(cat, imgBaseUrl);

        RssChannel channel = feedView.getChannel();
        channel.addRssItems(rssItems);

        RssFeed feedEntity = new RssFeed();
        feedEntity.setContent(mapper.writeValueAsString(feedView));
        feedEntity.setCreateTime(new Timestamp(new Date().getTime()));
        feedEntity.setSelector(cat.getDbTag());

        return feedEntity;
    }

    private List<RssItem> getPrevItems(XmlMapper mapper, RssFeed mostRecentFeed) throws JsonProcessingException {
        RssFeedView rssFeedView = mapper.readValue(mostRecentFeed.getContent(), RssFeedView.class);
        return rssFeedView.getChannel().getRssItems();
    }

    private List<PaperData> getPapersNewerThanPubDateForCategory(List<String> validCats, int maxItems) {
        return pdRepo.findLatestPapersWithAudioInCategories(validCats, maxItems, 0);
    }

    private List<PaperData> getPapersNewerThanPubDateForCategory(List<String> validCats, Date pubDate) {
        return pdRepo.findPapersNewerThanPubDateInCategory(validCats, pubDate.toInstant().atOffset(ZoneOffset.UTC));
    }


}
