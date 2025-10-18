package com.mtbs.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mtbs.constant.PathConstant;
import com.mtbs.constant.SecurityConstant;
import com.mtbs.dto.AddShowRequest;
import com.mtbs.entity.Booking;
import com.mtbs.entity.Movie;
import com.mtbs.entity.Show;
import com.mtbs.exception.MovieException;
import com.mtbs.service.BookingService;
import com.mtbs.service.MovieService;
import com.mtbs.service.ShowService;

import lombok.RequiredArgsConstructor;

@PreAuthorize(SecurityConstant.ADMIN_AUTHORIZED_ACCESS)
@RestController
@RequestMapping(PathConstant.ADMIN_PATH)
@RequiredArgsConstructor
public class AdminController {

	private final MovieService movieService;
	private final ShowService showService;
	private final BookingService bookingService;

	@PostMapping("/movies")
	public ResponseEntity<?> addMovie(@RequestBody Movie movie) {
		try {
			Movie savedMovie = movieService.addMovie(movie);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedMovie);
		} catch (MovieException ex) {
			return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage()));
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Failed to add movie: " + ex.getMessage()));
		}
	}

	@GetMapping("/allMovies")
	public ResponseEntity<Page<Movie>> getAllMovies(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDir) {

		Page<Movie> movies = movieService.getAllMovies(page, size, sortBy, sortDir);
		return ResponseEntity.ok(movies);
	}

	@PutMapping("/uMov/{id}")
	public ResponseEntity<?> updateMovie(@PathVariable Long id, @RequestBody Movie updated) {
		try {
			Movie movie = movieService.updateMovie(id, updated);
			return ResponseEntity.status(HttpStatus.OK).body(movie);
		} catch (MovieException ex) {
			return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage()));
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Failed to update movie: " + ex.getMessage()));
		}
	}

	@DeleteMapping("/dMov/{id}")
	public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
		try {
			movieService.deleteMovie(id);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of("message", "Movie deleted successfully"));
		} catch (MovieException ex) {
			return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage()));
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Failed to delete movie: " + ex.getMessage()));
		}
	}

	@PostMapping("/shows")
	public Show addShow(@RequestBody AddShowRequest request) {
	    return showService.addShow(request);
	}


	@GetMapping("/all-shows")
	public ResponseEntity<?> getAllShows() {
		List<Show> shows = showService.getAllShows();

		if (shows.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No shows available at the moment.");
		}

		return ResponseEntity.ok(shows);
	}

	@GetMapping("/bookings")
	public ResponseEntity<?> getAllBookings(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDir) {

		if (page < 0 || size <= 0) {
			return ResponseEntity.badRequest().body("Page index must be >= 0 and size must be > 0.");
		}

		try {
			Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

			Pageable pageable = PageRequest.of(page, size, sort);
			Page<Booking> bookings = bookingService.getAllBookings(pageable);

			if (bookings.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No bookings found.");
			}

			return ResponseEntity.ok(bookings);

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body("Invalid sort property: " + e.getMessage());
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An unexpected error occurred while fetching bookings.");
		}
	}
	
	
	
	
	
	
	

}
