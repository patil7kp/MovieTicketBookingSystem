package com.mtbs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mtbs.entity.Show;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
	List<Show> findByMovieId(Long movieId);

}
