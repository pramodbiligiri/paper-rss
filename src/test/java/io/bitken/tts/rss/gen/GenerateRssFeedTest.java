package io.bitken.tts.rss.gen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import io.bitken.tts.model.domain.CategoryInfo;
import io.bitken.tts.model.entity.PaperAudio;
import io.bitken.tts.model.entity.PaperCategory;
import io.bitken.tts.model.entity.PaperData;
import io.bitken.tts.model.entity.RssFeed;
import io.bitken.tts.model.entity.converter.BlobStorageHandler;
import io.bitken.tts.repo.PaperAudioRepo;
import io.bitken.tts.repo.PaperDataRepo;
import io.bitken.tts.repo.RssFeedRepo;
import io.bitken.tts.rss.main.RssMain;
import io.bitken.tts.rss.view.RssFeedView;
import io.bitken.tts.rss.view.RssItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Transactional
@SpringBootTest(classes = {RssMain.class})
public class GenerateRssFeedTest {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateRssFeedTest.class);

    private static final byte[] BYTES = {65, 66, 67, 68};

    public static final String NEW_PAPER_DEFAULT_TITLE = "My New Paper 1";

    @Autowired
    RssFeedRepo feedRepo;

    @Autowired
    GenerateRssFeed genFeed;

    @Autowired
    PaperDataRepo paperDataRepo;

    @Autowired
    PaperAudioRepo paperAudioRepo;

    @Autowired
    BlobStorageHandler storageHandler;

    @Value("${feed.max.items}")
    private int maxItemsInFeed;

    private XmlMapper xmlMapper;

    @BeforeEach
    public void beforeEach() {
        feedRepo.deleteAll();
        xmlMapper = XmlMapper.builder()
                .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
                .build();
    }

    @Test
    public void testFeedGen() throws Exception {
        CategoryInfo catInfo = CategoryInfo.AI;

        RssFeed feed = generateFeedOnEmptyDb(catInfo, maxItemsInFeed);
        RssFeedView rssFeedView = xmlMapper.readValue(feed.getContent(), RssFeedView.class);
        assertFeedNotEmpty(rssFeedView);

        addNewPaperToDb();
        addVeryOldPaperToDb();

        List<RssItem> items2 = generateFeedAndGetItems(catInfo, maxItemsInFeed);
        assertNewPaperFoundAndOldPaperNotFound(items2);
    }

    @Test
    public void testFeedGen2() throws Exception {
        CategoryInfo catInfo = CategoryInfo.AI;
        int itemCount = 3;

        RssFeed feed = generateFeedOnEmptyDb(catInfo, itemCount);
        assertFeedHasItemCount(feed, itemCount, xmlMapper);

        // Run generate again with no new data, and get back an empty feed
        Optional<RssFeed> rssFeedOpt2 = genFeed.generateFeed(catInfo, itemCount, xmlMapper);
        Assertions.assertEquals(Optional.empty(), rssFeedOpt2);

        addNewPapersToDb(3);

        // Run generate with higher item count, so that old papers and *all* the new papers
        // are returned in the feed
        int itemCount2 = 6;
        Optional<RssFeed> rssFeedOpt3 = genFeed.generateFeed(catInfo, itemCount2, xmlMapper);
        List<RssItem> items2 = assertNewItemCount(itemCount2, rssFeedOpt3);
        assertGeneratedFeedHasExpectedNoOfNewPapers(items2, 3);

        // Add 2 more new papers
        addNewPapersToDb(2);

        // Run generate with smaller item count (count value is set such that only papers inserted
        // via addNewPapersToDb() are returned)
        int itemCount3 = 5;
        Optional<RssFeed> rssFeedOpt4 = genFeed.generateFeed(catInfo, itemCount3, xmlMapper);
        List<RssItem> items4 = assertNewItemCount(itemCount3, rssFeedOpt4);
        assertGeneratedFeedHasExpectedNoOfNewPapers(items4, itemCount3);
    }

    private List<RssItem> assertNewItemCount(int itemCount2, Optional<RssFeed> rssFeedOpt3) throws JsonProcessingException {
        RssFeedView rssFeedView2 = xmlMapper.readValue(rssFeedOpt3.get().getContent(), RssFeedView.class);
        Assertions.assertNotNull(rssFeedView2);

        List<RssItem> items2 = rssFeedView2.getChannel().getRssItems();
        Assertions.assertEquals(itemCount2, items2.size());
        return items2;
    }

    private void assertGeneratedFeedHasExpectedNoOfNewPapers(List<RssItem> items, int expectedNewPaperCount) {
        int count = 0;
        for (RssItem item : items) {
            if (NEW_PAPER_DEFAULT_TITLE.equals(item.getTitle())) {
                count++;
            }
        }

        Assertions.assertEquals(expectedNewPaperCount, count);
    }

    private RssFeed generateFeedOnEmptyDb(CategoryInfo catInfo, int itemCount) throws JsonProcessingException {
        Optional<RssFeed> rssFeedOpt = genFeed.generateFeed(catInfo, itemCount, xmlMapper);
        Assertions.assertEquals(true, rssFeedOpt.isPresent());
        RssFeed feed = rssFeedOpt.get();
        feedRepo.save(feed);

        return feed;
    }

    private void assertFeedHasItemCount(RssFeed feed, int itemCount, XmlMapper xmlMapper) throws Exception {
        RssFeedView rssFeedView = xmlMapper.readValue(feed.getContent(), RssFeedView.class);
        Assertions.assertNotNull(rssFeedView);

        List<RssItem> items = rssFeedView.getChannel().getRssItems();
        Assertions.assertEquals(itemCount, items.size());
    }

    private void addNewPapersToDb(int paperCount) throws IOException {
        for (int i=0; i < paperCount; i++) {
            addNewPaperToDb();
        }
    }

    private void addNewPaperToDb() throws IOException {
        // First PaperData
        PaperData pd1 = new PaperData();
        pd1.setTitle(NEW_PAPER_DEFAULT_TITLE);
        pd1.setLink("http://paper-link-2.com");
        pd1.setArxivId("arxiv-id-1");
        pd1.setAuthors("author-1.1, author-1.2");
        pd1.setAbstractt("This is the abstract 1");
        pd1.setPubDate(new Timestamp(new Date().getTime()));
        pd1.addCategory(new PaperCategory().setCategory("cs.AI"));

        pd1 = paperDataRepo.save(pd1);

        PaperAudio pa1 = new PaperAudio();
        pa1.setPaper(pd1);
        pa1.setAudio(storageHandler.newFile(BYTES));
        pa1.setDuration(30);
        pa1 = paperAudioRepo.save(pa1);
        pd1.setAudio(pa1);
    }

    private void addVeryOldPaperToDb() throws IOException {
        // First PaperData
        PaperData pd1 = new PaperData();
        pd1.setTitle("My Very Old Paper 1");
        pd1.setLink("http://paper-link-2.com");
        pd1.setArxivId("arxiv-id-1");
        pd1.setAuthors("author-1.1, author-1.2");
        pd1.setAbstractt("This is the abstract 1");
        pd1.setPubDate(new Timestamp(Date.from(Instant.ofEpochSecond(3000)).getTime()));
        pd1.addCategory(new PaperCategory().setCategory("cs.AI"));

        pd1 = paperDataRepo.save(pd1);

        PaperAudio pa1 = new PaperAudio();
        pa1.setPaper(pd1);
        pa1.setAudio(storageHandler.newFile(BYTES));
        pa1.setDuration(30);
        pa1 = paperAudioRepo.save(pa1);
        pd1.setAudio(pa1);
    }

    private void assertFeedNotEmpty(RssFeedView rssFeedView) {
        Assertions.assertNotNull(rssFeedView);
        List<RssItem> items = rssFeedView.getChannel().getRssItems();
        Assertions.assertTrue(items.size() > 0);
    }

    private List<RssItem> generateFeedAndGetItems(CategoryInfo catInfo, int itemCount) throws JsonProcessingException {
        Optional<RssFeed> rssFeedOpt2 = genFeed.generateFeed(catInfo, itemCount, xmlMapper);
        RssFeedView rssFeedView2 = xmlMapper.readValue(rssFeedOpt2.get().getContent(), RssFeedView.class);

        Assertions.assertNotNull(rssFeedView2);

        return rssFeedView2.getChannel().getRssItems();
    }

    private void assertNewPaperFoundAndOldPaperNotFound(List<RssItem> items) {
        boolean newPaperFound = false;
        boolean oldPaperFound = false;

        for (RssItem item : items) {
            if (NEW_PAPER_DEFAULT_TITLE.equals(item.getTitle())) {
                newPaperFound = true;
            }

            if ("My Very Old Paper 1".equals(item.getTitle())) {
                oldPaperFound = true;
            }
        }

        Assertions.assertEquals(true, newPaperFound);
        Assertions.assertEquals(false, oldPaperFound);
    }

}
