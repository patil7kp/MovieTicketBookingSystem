package com.mtbs.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingResponse {
	private Long bookingId;
	private String movieTitle;
	private String showTime;
	private List<String> seats;
	private double totalAmount;
	private String message;
}
