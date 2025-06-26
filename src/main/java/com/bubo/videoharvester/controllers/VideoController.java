package com.bubo.videoharvester.controllers;

import com.bubo.videoharvester.VideoService;
import com.bubo.videoharvester.entity.Video;
import com.bubo.videoharvester.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Controller
public class VideoController {

    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoService videoService;

    @GetMapping("/videos")
    public String getAll(@RequestParam(required = false) String show, Model model) {
        List<Video> videos;
        if (show != null && !show.isEmpty()) {
            videos = videoRepository.findAllByShow(show);
        } else {
            videos = videoRepository.findAll();
        }
        for (Video video : videos) {
            if (video.getDownloadTimestamp().getYear() == 169087565) {
                video.setDownloadTimestamp(LocalDateTime.of(1970, 1, 1, 0, 0));
                videoRepository.save(video);
            }
        }

        videos.sort(Comparator.comparing(Video::getDownloadTimestamp).reversed());

        model.addAttribute("videos", videos);
        model.addAttribute("shows", videoRepository.findDistinctShowValues());
        return "videos";
    }

    @GetMapping("/videos/{show}")
    public List<Video> getByShow(@PathVariable String show) {
        return videoRepository.findAllByShow(show);
    }

    @PostMapping("/videos/delete/{id}")
    public String deleteItem(@PathVariable("id") Long id) {
        videoRepository.deleteById(id);
        return "redirect:/videos";
    }

    @PostMapping("/videos/force-check")
    @ResponseBody
    public void forceCheck() {
        videoService.forceCheckVideosAsync();
    }
}
