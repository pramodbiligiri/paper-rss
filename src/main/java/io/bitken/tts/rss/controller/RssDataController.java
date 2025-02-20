package io.bitken.tts.rss.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import io.bitken.tts.model.domain.CategoryInfo;
import io.bitken.tts.model.entity.RssFeed;
import io.bitken.tts.repo.RssFeedRepo;
import io.bitken.tts.rss.view.RssFeedView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
public class RssDataController {

	private static final Logger LOG = LoggerFactory.getLogger(RssDataController.class);

	@Autowired
	RssFeedRepo feedRepo;

	@Value("${feed.channel.image.baseurl}")
	private String imgBaseUrl;

	@GetMapping(path = "/rss/{cat}", produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> papers(HttpServletRequest req, @PathVariable String cat)
			throws JsonProcessingException {
		if (cat == null || cat.isBlank()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
		}

		Optional<CategoryInfo> catInfoOpt = CategoryInfo.fromDbTag(cat);
		if (catInfoOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
		}

		CategoryInfo catInfo = catInfoOpt.get();

		List<RssFeed> latestFeed = feedRepo.findLatestFeed(catInfo.getDbTag());
		if (latestFeed.isEmpty()) {
			XmlMapper mapper = XmlMapper.builder()
					.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
					.build();

			RssFeedView feedView = RssFeedView.createForCategory(catInfo, imgBaseUrl);

			return ResponseEntity.status(HttpStatus.OK).body(mapper.writeValueAsString(feedView));
		}

		return ResponseEntity.status(HttpStatus.OK).body(latestFeed.get(0).getContent());
	}

}
