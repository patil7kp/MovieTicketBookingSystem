package com.mtbs.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AddShowRequest {
	private Long movieId;
	private LocalDateTime showTime;
	private String screenName;
	private int totalSeats;
}
