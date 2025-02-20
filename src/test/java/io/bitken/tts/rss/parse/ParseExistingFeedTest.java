package io.bitken.tts.rss.parse;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import io.bitken.tts.model.domain.CategoryInfo;
import io.bitken.tts.model.entity.RssFeed;
import io.bitken.tts.repo.RssFeedRepo;
import io.bitken.tts.rss.gen.GenerateRssFeed;
import io.bitken.tts.rss.main.RssMain;
import io.bitken.tts.rss.view.RssFeedView;
import io.bitken.tts.rss.view.RssItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
@SpringBootTest(classes = {RssMain.class})
public class ParseExistingFeedTest {

    private static final Logger LOG = LoggerFactory.getLogger(ParseExistingFeedTest.class);

    @Autowired
    RssFeedRepo feedRepo;

    @Autowired
    GenerateRssFeed genFeed;

    @Value("${feed.max.items}")
    private int maxItemsInFeed;

    @Test
    public void testFeedParse() throws Exception {
        CategoryInfo catInfo = CategoryInfo.AI;

        XmlMapper xmlMapper = XmlMapper.builder()
                .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
                .build();

        Optional<RssFeed> rssFeedOpt = genFeed.generateFeed(catInfo, maxItemsInFeed, xmlMapper);

        Assertions.assertEquals(true, rssFeedOpt.isPresent());

        RssFeed feed = rssFeedOpt.get();

        RssFeedView rssFeedView = xmlMapper.readValue(feed.getContent(), RssFeedView.class);
        Assertions.assertNotNull(rssFeedView);

        List<RssItem> items = rssFeedView.getChannel().getRssItems();
        Assertions.assertTrue(items.size() > 0);

    }

}
