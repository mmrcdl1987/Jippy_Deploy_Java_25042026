package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmOutletDetailsDto;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmOutletMapper;
import com.jippy.foodandmart.projections.FmOutletMenuProjection;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.service.IFmOutletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FmOutletServiceImpl implements IFmOutletService {

    private final FmOutletRepository outletRepository;

    @Override
    public FmOutletDetailsDto getOutletDetails(Integer outletId, String userType) {

        log.info("Fetching outlet details for outletId={}", outletId);

        List<FmOutletMenuProjection> rows = outletRepository.getOutletMenu(outletId);

        if (rows == null || rows.isEmpty()) {
            log.error("No data found for outletId={}", outletId);
            throw new ResourceNotFoundException("Outlet not found with id: " + outletId);
        }

        FmOutletDetailsDto outletDtoresponse = FmOutletMapper.mapToOutletDto(rows,userType);

        log.info("Successfully fetched outlet details for outletId={}", outletId);

        return outletDtoresponse;
    }
}