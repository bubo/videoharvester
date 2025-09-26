package com.bubo.videoharvester.controllers;

import com.bubo.videoharvester.entity.Show;
import com.bubo.videoharvester.repository.ShowRepository;
import com.bubo.videoharvester.service.VideoDownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/shows")
public class ShowController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShowController.class);

    private final ShowRepository showRepository;

    private final VideoDownloadService videoDownloadService;


    public ShowController(ShowRepository showRepository, VideoDownloadService videoDownloadService) {

        this.showRepository = showRepository;
        this.videoDownloadService = videoDownloadService;
    }

    @GetMapping
    public String getAll(Model model) {

        model.addAttribute("shows", showRepository.findAll());
        return "shows";
    }

    @ModelAttribute("providers")
    public List<String> getProviders() {

        return videoDownloadService.getProviders();
    }

    @PostMapping("/delete/{showId}")
    public String deleteShow(@PathVariable("showId") Long showId) {

        showRepository.deleteById(showId);
        return "redirect:/shows";
    }

    @GetMapping({"/create", "/edit/{id}"})
    public String showForm(@PathVariable(required = false) Long id, Model model) {

        Show show = (id != null)
                    ? showRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid show ID: " + id))
                    : new Show();

        model.addAttribute("show", show);
        return "create-edit-show";
    }

    @PostMapping({"/create", "/edit/{id}"})
    public String saveShow(@PathVariable(required = false) Long id, @ModelAttribute Show show) {

        if (id != null) {
            Show existingShow = showRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid show ID: " + id));
            existingShow.setTitle(show.getTitle());
            existingShow.setIsEnabled(show.getIsEnabled());
            existingShow.setUrl(show.getUrl());
            existingShow.setPath(show.getPath());
            existingShow.setProvider(show.getProvider());
            existingShow.setDownloadOnlyEpisodes(show.getDownloadOnlyEpisodes());
            showRepository.save(existingShow);
            LOGGER.info("Updated show: {}", show);
        } else {
            showRepository.save(show);
            LOGGER.info("Created show: {}", show);
        }
        return "redirect:/shows";
    }
}
