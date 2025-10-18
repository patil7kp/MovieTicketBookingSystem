package com.mtbs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mtbs.entity.PromoCode;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {
	Optional<PromoCode> findByCodeAndActiveTrue(String code);

	Optional<PromoCode> findByCode(String code);
}
