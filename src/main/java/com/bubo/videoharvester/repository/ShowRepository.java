package com.bubo.videoharvester.repository;

import com.bubo.videoharvester.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findAllByIsEnabledTrue();
}
