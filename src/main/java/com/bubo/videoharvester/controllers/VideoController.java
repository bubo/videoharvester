package com.bubo.videoharvester.controllers;

import com.bubo.videoharvester.dto.VideoDTO;
import com.bubo.videoharvester.entity.Show;
import com.bubo.videoharvester.entity.Video;
import com.bubo.videoharvester.repository.ShowRepository;
import com.bubo.videoharvester.repository.VideoRepository;
import com.bubo.videoharvester.service.VideoDownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Controller
public class VideoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoController.class);

    private final VideoRepository videoRepository;

    private final VideoDownloadService videoDownloadService;

    private final ShowRepository showRepository;

    public VideoController(VideoRepository videoRepository, VideoDownloadService videoDownloadService,
                           ShowRepository showRepository) {

        this.videoRepository = videoRepository;
        this.videoDownloadService = videoDownloadService;
        this.showRepository = showRepository;
    }

    @GetMapping("/videos")
    public String getAll(@RequestParam(required = false) Long showId, Model model) {

        List<Video> videos;
        if (showId != null) {
            LOGGER.info("Getting videos for show ID: {}", showId);
            videos = videoRepository.findByShowId(showId);
            Show show = showRepository.findById(showId).orElseThrow();
            model.addAttribute("show", show);
        } else {
            LOGGER.info("Getting all videos");
            videos = videoRepository.findAll();
        }

        videos.sort(Comparator.comparing(Video::getId).reversed());

        model.addAttribute("videos", videos);
        return "videos";
    }

    @GetMapping("/videos/{id}")
    public ResponseEntity<VideoDTO> getVideo(@PathVariable("id") Long id) {

        return ResponseEntity.ok(VideoDTO.fromEntity(videoRepository.findById(id).orElseThrow()));
    }

    @PostMapping("/videos/delete/{id}")
    public String deleteItem(@PathVariable("id") Long id) {

        LOGGER.info("Deleting video with ID: {}", id);

        Long showId = videoDownloadService.deleteVideoFile(id);
        videoRepository.deleteById(id);
        return "redirect:/videos?showId=" + showId;
    }

    @PostMapping("/videos/deleteFile/{id}")
    public String deleteFile(@PathVariable("id") Long id) {

        LOGGER.info("Deleting file for video with ID: {}", id);


        Long showId = videoDownloadService.deleteVideoFile(id);
        return "redirect:/videos?showId=" + showId;
    }

    @PostMapping("/videos/force-check")
    @ResponseBody
    public void forceCheck() {

        LOGGER.info("Forcing video check");

        videoDownloadService.forceProcessVideosAsync();
    }
}
