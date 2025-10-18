package com.mtbs.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.mtbs.entity.Movie;
import com.mtbs.exception.MovieException;
import com.mtbs.repository.MovieRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovieService {
	
    private final MovieRepository movieRepository;

	public Movie addMovie(Movie movie) {

		if (movie == null) {
			throw new MovieException("Movie cannot be null", HttpStatus.BAD_REQUEST);
		}

		if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
			throw new MovieException("Movie title is required", HttpStatus.BAD_REQUEST);
		}

		if (movie.getGenre() == null || movie.getGenre().trim().isEmpty()) {
			throw new MovieException("Movie genre is required", HttpStatus.BAD_REQUEST);
		}

		if (movie.getDurationMinutes() <= 0) {
			throw new MovieException("Movie duration must be greater than 0", HttpStatus.BAD_REQUEST);
		}

		if (movie.getReleaseDate() == null) {
			throw new MovieException("Movie release date is required", HttpStatus.BAD_REQUEST);
		}

		boolean exists = movieRepository.existsByTitleAndReleaseDate(movie.getTitle(), movie.getReleaseDate());
		if (exists) {
			throw new MovieException("A movie with the same title and release date already exists",
					HttpStatus.CONFLICT);
		}

		if (movie.getShows() == null) {
			movie.setShows(new ArrayList<>());
		} else {
			movie.getShows().forEach(show -> show.setMovie(movie));
		}

		return movieRepository.save(movie);
	}


	public Page<Movie> getAllMovies(int page, int size, String sortBy, String sortDir) {
	    try {
	        Sort sort = sortDir.equalsIgnoreCase("desc")
	                ? Sort.by(sortBy).descending()
	                : Sort.by(sortBy).ascending();

	        Pageable pageable = PageRequest.of(page, size, sort);
	        Page<Movie> moviesPage = movieRepository.findAll(pageable);

	        if (moviesPage.isEmpty()) {
	            throw new MovieException("No movies found", HttpStatus.NOT_FOUND);
	        }

	        return moviesPage;
	    } catch (Exception ex) {
	        throw new MovieException("Failed to fetch movies: " + ex.getMessage(),
	                HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}


	public void deleteMovie(Long id) {

		if (id == null || id <= 0) {
			throw new MovieException("Invalid movie ID", HttpStatus.BAD_REQUEST);
		}

		Optional<Movie> movieOpt = movieRepository.findById(id);
		if (movieOpt.isEmpty()) {
			throw new MovieException("Movie with ID " + id + " not found", HttpStatus.NOT_FOUND);
		}

		try {
			movieRepository.deleteById(id);
		} catch (Exception ex) {
			throw new MovieException("Failed to delete movie: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	public Movie updateMovie(Long id, Movie updatedMovie) {

		if (id == null || id <= 0) {
			throw new MovieException("Invalid movie ID", HttpStatus.BAD_REQUEST);
		}

		if (updatedMovie == null) {
			throw new MovieException("Updated movie cannot be null", HttpStatus.BAD_REQUEST);
		}
		if (updatedMovie.getTitle() == null || updatedMovie.getTitle().trim().isEmpty()) {
			throw new MovieException("Movie title is required", HttpStatus.BAD_REQUEST);
		}
		if (updatedMovie.getGenre() == null || updatedMovie.getGenre().trim().isEmpty()) {
			throw new MovieException("Movie genre is required", HttpStatus.BAD_REQUEST);
		}
		if (updatedMovie.getDurationMinutes() <= 0) {
			throw new MovieException("Movie duration must be greater than 0", HttpStatus.BAD_REQUEST);
		}
		if (updatedMovie.getReleaseDate() == null) {
			throw new MovieException("Movie release date is required", HttpStatus.BAD_REQUEST);
		}

		Movie existingMovie = movieRepository.findById(id)
				.orElseThrow(() -> new MovieException("Movie with ID " + id + " not found", HttpStatus.NOT_FOUND));

		boolean exists = movieRepository.existsByTitleAndReleaseDateAndIdNot(updatedMovie.getTitle(),
				updatedMovie.getReleaseDate(), id);
		if (exists) {
			throw new MovieException("Another movie with the same title and release date already exists",
					HttpStatus.CONFLICT);
		}

		existingMovie.setTitle(updatedMovie.getTitle());
		existingMovie.setGenre(updatedMovie.getGenre());
		existingMovie.setDurationMinutes(updatedMovie.getDurationMinutes());
		existingMovie.setReleaseDate(updatedMovie.getReleaseDate());

		if (updatedMovie.getShows() != null) {
			updatedMovie.getShows().forEach(show -> show.setMovie(existingMovie));
			existingMovie.setShows(updatedMovie.getShows());
		}

		try {
			return movieRepository.save(existingMovie);
		} catch (Exception ex) {
			throw new MovieException("Failed to update movie: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
//	---------------------------------------------
	
	public Page<Movie> searchMovies(String title, Pageable pageable) {
        if (title == null || title.trim().isEmpty()) {
            // Return all movies with pagination
            return movieRepository.findAll(pageable);
        }

        try {
            return movieRepository.findByTitleContainingIgnoreCase(title.trim(), pageable);
        } catch (Exception e) {
            // Log error, return empty page instead of throwing
            System.err.println("Error while searching movies: " + e.getMessage());
            return Page.empty(pageable);
        }
    }
	
	
	
	
	
	

}
