package com.mtbs.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mtbs.entity.Seat;

import jakarta.persistence.LockModeType;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM Seat s WHERE s.show.id = :showId AND s.seatNumber IN :seatNumbers")
	List<Seat> findByShowIdAndSeatNumberInForUpdate(@Param("showId") Long showId,
			@Param("seatNumbers") List<String> seatNumbers);

	List<Seat> findByShowIdAndSeatNumberIn(Long showId, List<String> list);

	Page<Seat> findByShowId(Long showId, Pageable pageable);
}
