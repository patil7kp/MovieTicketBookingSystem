package com.mtbs.dto;

import java.util.List;

import lombok.Data;

@Data
public class BookingRequest {
	private Long showId;
	private List<String> seatNumbers;
	private String promoCode;
}
