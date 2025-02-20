package io.bitken.tts.rss.main;

import io.bitken.tts.rss.gen.FeedGenerateChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableJpaRepositories("io.bitken.tts.repo")
@EntityScan(basePackages = {"io.bitken.tts.model.entity"})
@ComponentScan(
	basePackages = {"io.bitken.tts"},
	excludeFilters = {
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "io.bitken.tts.rss.main.*"),
	}
)
public class RssMain {

	private static final Logger LOG = LoggerFactory.getLogger(RssMain.class);

	@Value("${feed.gen.enable}")
	private boolean isFeedGenEnabled;

	@Value("${feed.check.interval.minutes}")
	private int interval;

	@Value("${feed.check.initial.delay.minutes}")
	private int initialDelay;

	@Autowired
	FeedGenerateChecker feedGenerateChecker;

	private ScheduledFuture<?> fut;

	public static void main(String[] args) {
		int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8085"));

		SpringApplication app = new SpringApplication(RssMain.class);
		app.setDefaultProperties(Collections.singletonMap("server.port", port));

		app.run(args);
	}

	@PostConstruct
	public void postConstruct() {
		if (!isFeedGenEnabled) {
			LOG.warn("Not generating RSS feed because feed gen is not enabled");
			return;
		}

		ScheduledExecutorService scheduledExec = Executors.newScheduledThreadPool(1);
		LOG.info("Scheduling RSS feed check at interval of " + interval + " " +
				"minutes, with initial delay: " + initialDelay + " minutes");

		fut = scheduledExec.scheduleWithFixedDelay(feedGenerateChecker, initialDelay, interval, TimeUnit.MINUTES);
	}

	@PreDestroy
	public void preDestroy() {
		if (fut == null) {
			return;
		}

		if (!fut.isDone() && !fut.isCancelled()) {
			fut.cancel(false);
		}
	}


}
