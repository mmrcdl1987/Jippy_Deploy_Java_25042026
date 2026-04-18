package com.jippy.foodandmart.service;


import com.jippy.foodandmart.dto.FmOutletDetailsDto;

public interface IFmOutletService {
    FmOutletDetailsDto getOutletDetails(Integer outletId, String userType);
}