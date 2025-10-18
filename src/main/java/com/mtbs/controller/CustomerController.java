package com.mtbs.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mtbs.constant.PathConstant;
import com.mtbs.constant.SecurityConstant;
import com.mtbs.dto.BookingRequest;
import com.mtbs.dto.BookingResponse;
import com.mtbs.entity.Booking;
import com.mtbs.entity.Movie;
import com.mtbs.entity.Seat;
import com.mtbs.entity.Show;
import com.mtbs.repository.UserRepository;
import com.mtbs.service.BookingService;
import com.mtbs.service.MovieService;
import com.mtbs.service.ShowService;

import lombok.RequiredArgsConstructor;

@PreAuthorize(SecurityConstant.CUSTOMER_AUTHORIZED_ACCESS)
@RestController
@RequestMapping(PathConstant.CUSTOMER_PATH)
@RequiredArgsConstructor
public class CustomerController {

	private final BookingService bookingService;
	private final MovieService movieService;
	private final ShowService showService;
	private final UserRepository userRepository;

	// search movies
	@GetMapping
	public ResponseEntity<Page<Movie>> searchMovies(@RequestParam(required = false, defaultValue = "") String title,
			@PageableDefault(page = 0, size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {

		Page<Movie> movies = movieService.searchMovies(title, pageable);

		if (movies.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok(movies);
	}

	// shows for movie
	@GetMapping("/{movieId}")
	public ResponseEntity<List<Show>> getShowsForMovie(@PathVariable Long movieId) {
		if (movieId == null || movieId <= 0) {
			return ResponseEntity.badRequest().build(); // 400 Bad Request for invalid ID
		}

		List<Show> shows = showService.getShowsForMovie(movieId);

		if (shows.isEmpty()) {
			return ResponseEntity.noContent().build(); // 204 if no shows
		}

		return ResponseEntity.ok(shows);
	}

	// seats for a show
	@GetMapping("/{showId}/seats")
	public ResponseEntity<Page<Seat>> getSeatsForShow(@PathVariable Long showId,
			@PageableDefault(page = 0, size = 20, sort = "seatNumber", direction = Sort.Direction.ASC) Pageable pageable) {

		if (showId == null || showId <= 0) {
			return ResponseEntity.badRequest().build();
		}

		Page<Seat> seats = showService.getSeatsForShow(showId, pageable);

		if (seats.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok(seats);
	}

	// book seats
	@PostMapping("/book/{userId}")
	public ResponseEntity<?> book(@PathVariable Long userId, @RequestBody BookingRequest request) {

		if (userId == null || userId <= 0) {
			return ResponseEntity.badRequest().body("Invalid userId");
		}

		if (!userRepository.existsById(userId)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}

		if (request == null || request.getSeatNumbers() == null || request.getSeatNumbers().isEmpty()) {
			return ResponseEntity.badRequest().body("Seat numbers are required");
		}

		BookingResponse bookSeats = bookingService.bookSeats(userId, request);
		return ResponseEntity.ok().body(bookSeats);
	}

	// view bookings for user
	@GetMapping("/bookings/{userId}")
	public List<Booking> viewBookings(@PathVariable Long userId) {
		return bookingService.getBookingsByUser(userId);
	}
	
	
	
	
	
}
