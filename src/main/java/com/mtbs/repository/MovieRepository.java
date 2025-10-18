package com.mtbs.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mtbs.entity.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

	boolean existsByTitleAndReleaseDate(String title, LocalDate releaseDate);

	boolean existsByTitleAndReleaseDateAndIdNot(String title, LocalDate releaseDate, Long id);

	Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
