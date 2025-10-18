package com.mtbs.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mtbs.dto.AddShowRequest;
import com.mtbs.entity.Movie;
import com.mtbs.entity.Seat;
import com.mtbs.entity.Show;
import com.mtbs.exception.InvalidShowRequestException;
import com.mtbs.exception.MovieNotFoundException;
import com.mtbs.exception.ShowAlreadyExistsException;
import com.mtbs.repository.MovieRepository;
import com.mtbs.repository.SeatRepository;
import com.mtbs.repository.ShowRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShowService {

	private final ShowRepository showRepository;
	private final SeatRepository seatRepository;
	private final MovieRepository movieRepository;

	public Show addShow(AddShowRequest request) {
		Movie movie = movieRepository.findById(request.getMovieId())
				.orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + request.getMovieId()));

		// Validate show time
		if (request.getShowTime() == null || request.getShowTime().isBefore(LocalDateTime.now())) {
			throw new InvalidShowRequestException("Show time must be in the future");
		}

		// Validate screen name
		if (request.getScreenName() == null || request.getScreenName().isBlank()) {
			throw new InvalidShowRequestException("Screen name is required");
		}

		// Validate seats
		if (request.getTotalSeats() <= 0) {
			throw new InvalidShowRequestException("Total seats must be greater than 0");
		}

		// Prevent duplicate show
		boolean overlap = showRepository.findAll().stream()
				.anyMatch(s -> s.getScreenName().equalsIgnoreCase(request.getScreenName())
						&& s.getShowTime().equals(request.getShowTime()));
		if (overlap) {
			throw new ShowAlreadyExistsException(
					"A show already exists in screen '" + request.getScreenName() + "' at " + request.getShowTime());
		}

		Show show = Show.builder().movie(movie).showTime(request.getShowTime()).screenName(request.getScreenName())
				.totalSeats(request.getTotalSeats()).availableSeats(request.getTotalSeats()).build();

		// Create seats
		List<Seat> seats = createSeats(show, request.getTotalSeats());
		show.setSeats(seats);

		return showRepository.save(show);
	}

	private List<Seat> createSeats(Show show, int totalSeats) {
		List<Seat> seats = new ArrayList<>();
		for (int i = 1; i <= totalSeats; i++) {
			Seat seat = Seat.builder().seatNumber("S" + i).status("AVAILABLE").show(show).build();
			seats.add(seat);
		}
		return seats;
	}

	public List<Show> getAllShows() {
		List<Show> shows = showRepository.findAll();

		if (shows == null || shows.isEmpty()) {
			return Collections.emptyList();
		}

		shows.sort(Comparator.comparing(Show::getShowTime));

		return shows;
	}

	public List<Show> getShowsForMovie(Long movieId) {
		if (!movieRepository.existsById(movieId)) {
			throw new EntityNotFoundException("Movie with id " + movieId + " not found");
		}

		try {
			return showRepository.findByMovieId(movieId);
		} catch (Exception e) {
			throw new RuntimeException("Internal server error while fetching shows");
		}
	}

	public Page<Seat> getSeatsForShow(Long showId, Pageable pageable) {
		if (!showRepository.existsById(showId)) {
			throw new EntityNotFoundException("Show with id " + showId + " not found");
		}

		try {
			return seatRepository.findByShowId(showId, pageable);
		} catch (Exception e) {
			System.err.println("Error fetching seats for show " + showId + ": " + e.getMessage());
			return Page.empty(pageable);
		}
	}

}
