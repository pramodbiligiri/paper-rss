package io.bitken.tts.rss.gen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import io.bitken.tts.model.domain.CategoryInfo;
import io.bitken.tts.model.entity.RssFeed;
import io.bitken.tts.repo.RssFeedRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class FeedGenerateChecker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(FeedGenerateChecker.class);

    @Autowired
    RssFeedRepo feedRepo;

    @Autowired
    GenerateRssFeed genFeed;

    @Value("${feed.update.interval.minutes}")
    private int updateIntervalMinutes;

    @Value("${feed.max.items}")
    private int maxItemsInFeed;


    @Async
    @Transactional
    public void run() {
        LOG.info("Starting to update feeds if needed");

        XmlMapper xmlMapper = XmlMapper.builder()
                .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
                .build();

        for (CategoryInfo cat : CategoryInfo.values()) {
            LOG.info("Category: " + cat.getDbTag());
            List<RssFeed> latestFeed = feedRepo.findLatestFeed(cat.getDbTag());

            if (latestFeed.isEmpty()) {
                LOG.info("No past feed found in database for " + cat.getDbTag());
                generateFeed(cat, xmlMapper);
                continue;
            }

            RssFeed latestFeed1 = latestFeed.get(0);
            if (!hasExceededRefreshInterval(latestFeed1)) {
                LOG.info("Refresh internval not exceeded. Skipping generation of RSS feed for category: " + cat.getDbTag());
                continue;
            }

            generateFeed(cat, xmlMapper);

        }
    }

    private void generateFeed(CategoryInfo cat, XmlMapper xmlMapper) {
        try {
            LOG.info("Generating RSS feed for category: " + cat.getDbTag());

            Optional<RssFeed> feedEntity = genFeed.generateFeed(cat, maxItemsInFeed, xmlMapper);

            if (feedEntity.isPresent()) {
                feedRepo.save(feedEntity.get());
            }
            
        } catch (JsonProcessingException e) {
            LOG.error("Exception generating feed", e);
        }
    }

    private boolean hasExceededRefreshInterval(RssFeed latestFeed) {
        Duration updateInterval = Duration.of(updateIntervalMinutes, ChronoUnit.MINUTES);
        long updateIntervalMillis = updateInterval.getSeconds() * 1000;

        LOG.info("Checking if feed update interval of " + (updateIntervalMillis/1000) + " seconds has been exceeded");

        Date previousCreateTime = getPreviousCreateTime(latestFeed);

        Date currentTime = new Date();
        LOG.info("Last create time: " + previousCreateTime);
        LOG.info("Current time: " + currentTime);
        boolean hasExceeded = previousCreateTime.getTime() <= (currentTime.getTime() - updateIntervalMillis);

        LOG.info("hasExceeded: " + hasExceeded);

        return hasExceeded;
    }

    private Date getPreviousCreateTime(RssFeed latestFeed) {
        Timestamp lastTime = latestFeed.getCreateTime();
        lastTime.setNanos(0);

        return new Date(lastTime.getTime());
    }
}
