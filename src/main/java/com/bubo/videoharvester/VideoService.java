package com.bubo.videoharvester;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoService {

    @Autowired
    private List<VideoDownloader> videoDownloaders;

    @Async
    public void forceCheckVideosAsync() {
        videoDownloaders.forEach(VideoDownloader::checkForNewVideo);
    }
}