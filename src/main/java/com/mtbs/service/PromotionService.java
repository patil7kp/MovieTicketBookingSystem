package com.mtbs.service;

import com.mtbs.entity.PromoCode;
import com.mtbs.entity.User;
import com.mtbs.exception.InvalidPromoCodeException;
import com.mtbs.exception.PromoNotEligibleException;
import com.mtbs.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private final PromoCodeRepository promoCodeRepository;

    public PromoCode validatePromoCode(String code) {
        Optional<PromoCode> op = promoCodeRepository.findByCode(code);
        PromoCode promo = op.orElseThrow(() -> new InvalidPromoCodeException("Invalid promo code"));
        if (!promo.isActive() || promo.getExpiryDate() == null || promo.getExpiryDate().isBefore(LocalDate.now())) {
            throw new InvalidPromoCodeException("Promo code expired or inactive");
        }
        return promo;
    }

    public void ensureUserEligibleForPromo(User user) {
        boolean eligible = user.getTotalBookings() > 5 || user.getTotalSpent() > 1500.0;
        if (!eligible) {
            throw new PromoNotEligibleException("User is not eligible for promotions (need >5 bookings OR spent >1500)");
        }
    }
}
