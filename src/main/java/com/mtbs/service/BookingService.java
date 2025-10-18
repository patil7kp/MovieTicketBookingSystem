package com.mtbs.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mtbs.dto.BookingRequest;
import com.mtbs.dto.BookingResponse;
import com.mtbs.entity.Booking;
import com.mtbs.entity.PromoCode;
import com.mtbs.entity.Seat;
import com.mtbs.entity.Show;
import com.mtbs.entity.User;
import com.mtbs.exception.SeatUnavailableException;
import com.mtbs.repository.BookingRepository;
import com.mtbs.repository.SeatRepository;
import com.mtbs.repository.ShowRepository;
import com.mtbs.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final PromotionService promotionService;

    /**
     * Book exact seats for a user — concurrency-safe using PESSIMISTIC_WRITE on Seat rows.
     */
    @Transactional
    public BookingResponse bookSeats(Long userId, BookingRequest request) {
        // fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // fetch show
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found"));

        List<String> seatNumbers = request.getSeatNumbers();
        if (seatNumbers == null || seatNumbers.isEmpty()) {
            throw new SeatUnavailableException("No seats selected");
        }

        // LOCK the seats selected to prevent other tx from reading them as available
        List<Seat> seats = seatRepository.findByShowIdAndSeatNumberInForUpdate(show.getId(), seatNumbers);

        // ensure fetched seats count matches requested
        if (seats.size() != seatNumbers.size()) {
            throw new SeatUnavailableException("One or more selected seats do not exist");
        }

        // verify seat statuses
        List<Seat> unavailable = seats.stream()
                .filter(s -> !"AVAILABLE".equalsIgnoreCase(s.getStatus()))
                .collect(Collectors.toList());
        if (!unavailable.isEmpty()) {
            String names = unavailable.stream().map(Seat::getSeatNumber).collect(Collectors.joining(","));
            throw new SeatUnavailableException("Seats not available: " + names);
        }

        // compute base total
        double perSeatPrice = show.getMovie() == null ? 0.0 : estimateTicketPrice(show); // you may have a price field on Show; adapt if so
        // if Show has ticket price, use that; otherwise fallback
        // For this code assume price per seat obtained from show (if you add a ticketPrice field) :
        // double perSeatPrice = show.getTicketPrice();
        // For now compute with sample logic: assume show has attribute ticketPrice (update Show entity if needed).
        // We'll check if show has getTicketPrice method via reflection; otherwise default 200.0
        try {
            perSeatPrice = (double) Show.class.getDeclaredMethod("getTicketPrice").invoke(show);
        } catch (Exception ignored) {
            perSeatPrice = 200.0; // default fallback
        }

        int seatsCount = seats.size();
        double total = seatsCount * perSeatPrice;

        // apply promo if provided
        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            // validate promo exists & active & expiry
            PromoCode promo = promotionService.validatePromoCode(request.getPromoCode());

            // check eligibility
            promotionService.ensureUserEligibleForPromo(user);

            if ("FREE_SEAT".equalsIgnoreCase(promo.getType())) {
                // Make one seat free - subtract one seat price
                total -= perSeatPrice;
            } else if ("FLAT_250".equalsIgnoreCase(promo.getType())) {
                total -= 250.0;
            }
            if (total < 0) total = 0.0;
            // NOTE: If you want to mark promo as used, you'd add a usage table or deactivate; not implemented here.
        }

        // mark seats as BOOKED
        seats.forEach(s -> s.setStatus("BOOKED"));
        seatRepository.saveAll(seats);

        // update show.availableSeats atomically
        int newAvailable = show.getAvailableSeats() - seatsCount;
        if (newAvailable < 0) {
            // This is a safety check (shouldn't happen because we checked seat availability)
            throw new SeatUnavailableException("Not enough available seats");
        }
        show.setAvailableSeats(newAvailable);
        showRepository.save(show);

        // create booking
        Booking booking = Booking.builder()
                .user(user)
                .show(show)
                .seats(new ArrayList<>(seats))
                .totalAmount(total)
                .bookingTime(LocalDateTime.now())
                .status("CONFIRMED")
                .build();
        booking = bookingRepository.save(booking);

        // update user stats (totalBookings and totalSpent)
        user.setTotalBookings(user.getTotalBookings() + 1);
        user.setTotalSpent(user.getTotalSpent() + total);
        userRepository.save(user);

        return BookingResponse.builder()
                .bookingId(booking.getId())
                .movieTitle(show.getMovie().getTitle())
                .showTime(show.getShowTime().toString())
                .seats(seatNumbers)
                .totalAmount(total)
                .message("Booking successful")
                .build();
    }

    private double estimateTicketPrice(Show show) {
        return 200.0;
    }

	public List<Booking> getBookingsByUser(Long userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		boolean userExists = userRepository.existsById(userId);
		if (!userExists) {
			throw new NoSuchElementException("User with ID " + userId + " does not exist");
		}

		List<Booking> bookings = bookingRepository.findByUserId(userId);

		return bookings != null ? bookings : Collections.emptyList();
	}
	
	public Page<Booking> getAllBookings(Pageable pageable) {
        try {
            if (pageable == null) {
                pageable = Pageable.ofSize(10);
            }

            Page<Booking> bookings = bookingRepository.findAll(pageable);

            return bookings != null ? bookings : Page.empty();

        } catch (DataAccessException ex) {
            throw new RuntimeException("Database error while fetching bookings.", ex);
        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error while retrieving bookings.", ex);
        }
    }

}
