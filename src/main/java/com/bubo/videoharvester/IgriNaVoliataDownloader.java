package com.bubo.videoharvester;

import com.bubo.videoharvester.repository.VideoRepository;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class IgriNaVoliataDownloader extends VideoDownloader {

    private static final Logger logger = LoggerFactory.getLogger(IgriNaVoliataDownloader.class);

    @Value("${videoharvester.video.check.delay.igri:120000}")
    protected long fixedDelay;

    @Value("${videoharvester.video.location.igri}")
    private String path;

    protected IgriNaVoliataDownloader(@Autowired HomeAssistantNotifier homeAssistantNotifier,
                                      @Autowired VideoRepository videoRepository) {

        super(homeAssistantNotifier, videoRepository);
    }

    @Scheduled(cron = "${videoharvester.igri.cron}")
    @Override
    public void checkForNewVideo() {

        super.checkForNewVideo();
    }

    @Override
    protected Logger getLogger() {

        return logger;
    }

    @Override
    protected String getShowName() {

        return "Igrite";
    }

    @Override
    protected String getPath() {

        return path;
    }

    @Override
    protected String getUrl() {

        return "https://play.nova.bg/tvshow/igri-na-volyata-balgariya/46";
    }

    @Override
    protected String getCssQuery() {

        return "div[data-sentry-element=ShowWrapper]";
    }

    @Override
    protected String extractTitle(Element videoElement) {

        String title =
                Optional.ofNullable(videoElement.selectFirst("p[class^=cards-shared__VideoTitle]")).map(Element::text)
                        .orElse("");
        String subtitle = Optional.ofNullable(videoElement.selectFirst("p[class^=cards-shared__VideoSubTitle]"))
                .map(Element::text).orElse("");

        String[] parts = title.split("\\(");
        String formattedTitle = parts[0].trim();
        String date = parts.length > 1
                      ? parts[1].replace(")", "").trim()
                      : "";

        return formattedTitle + " " + subtitle + " (" + date + ")";
    }
}
