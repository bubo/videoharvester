package com.bubo.videoharvester;

import com.bubo.videoharvester.entity.Video;
import com.bubo.videoharvester.entity.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VideoController {

    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);

    @Autowired
    private VideoRepository videoRepository;

    @GetMapping("/videos")
    public List<Video> getAll() {
        return videoRepository.findAll();
    }

    @GetMapping("/videos/{show}")
    public List<Video> getByShow(@PathVariable String show) {
        return videoRepository.findAllByShow(show);
    }

    @GetMapping("/videos/delete/{id}")
    public void delete(@PathVariable Long id) {
        videoRepository.deleteById(id);
    }


}
