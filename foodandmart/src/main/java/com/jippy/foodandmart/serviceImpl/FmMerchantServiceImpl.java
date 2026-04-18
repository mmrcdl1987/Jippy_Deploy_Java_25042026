package com.jippy.foodandmart.serviceImpl;


import com.jippy.foodandmart.dto.FmMerchantDto;
import com.jippy.foodandmart.dto.FmMerchantWithBankDto;
import com.jippy.foodandmart.entity.FmMerchant;
import com.jippy.foodandmart.entity.FmMerchantBankDetails;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmMerchantMapper;
import com.jippy.foodandmart.projections.FmMerchantWithBankProjection;
import com.jippy.foodandmart.repository.FmMerchantBankDetailsRepository;
import com.jippy.foodandmart.repository.FmMerchantRepository;
import com.jippy.foodandmart.service.IFmMerchantService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FmMerchantServiceImpl implements IFmMerchantService {

    private static final Logger logger =
            LoggerFactory.getLogger(FmMerchantServiceImpl.class);

    @Autowired
    private FmMerchantRepository merchantRepository;

    @Autowired
    private FmMerchantBankDetailsRepository fmMerchantBankDetailsRepository;

    //    get only merchant
    public FmMerchantDto getMerchantProfile(int merchantId) {
        logger.info("Fetching merchant profile for merchantId: {}", merchantId);
        FmMerchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> {
                    logger.error("Merchant not found with id: {}", merchantId);
                    return new ResourceNotFoundException(
                            "Merchant not found with id: " + merchantId
                    );
                });

        logger.info("Merchant fetched successfully for merchantId: {}", merchantId);

        return FmMerchantMapper.mapToMerchantDto(merchant);
    }

    //   get -> merchant + bank (native query)
    @Override
    public FmMerchantWithBankDto getMerchantWithBank(Long merchantId) {
        logger.info("Fetching merchant with bank details for merchantId: {}", merchantId);
        FmMerchantWithBankProjection data =
                merchantRepository.getMerchantWithBank(merchantId);
        if (data == null) {
            logger.error("Merchant with bank details not found for merchantId: {}", merchantId);
            throw new ResourceNotFoundException("Merchant not found with :" + merchantId);
        }
        logger.info("Successfully fetched merchant + bank details for merchantId: {}", merchantId);
        return FmMerchantMapper.mapToMerchantWithBankDto(data);
    }

    //    update--> merchant + bank
    @Override
    @Transactional
    public FmMerchantWithBankDto updateMerchantProfile(FmMerchantWithBankDto dto) {

        logger.info("Updating merchant profile for merchantId: {}", dto.getMerchantId());
        logger.debug("Request DTO: {}", dto);
        // 1. Fetch Merchant
        FmMerchant merchant = merchantRepository.findById(dto.getMerchantId().intValue())
                .orElseThrow(() -> {
                    logger.error("Merchant not found with ID: {}", dto.getMerchantId());
                    return new ResourceNotFoundException(
                            "Merchant not found with ID :" + dto.getMerchantId()
                    );
                });

        // 2. Update Merchant fields by using lombok -getters and setters(data)s
        merchant.setMerchantName(dto.getMerchantName());
        merchant.setMerchantEmail(dto.getMerchantEmail());
        merchant.setMerchantPhone(dto.getMerchantPhone());
        merchant.setMerchantBusinessType(dto.getBusinessType());
        merchant.setStatus(dto.getStatus());

        merchantRepository.save(merchant);
        logger.info("Merchant updated successfully for merchantId: {}", dto.getMerchantId());

        // 3. Fetch Bank Details
        FmMerchantBankDetails bank = fmMerchantBankDetailsRepository
                .findByRecipientIdAndUserType(dto.getMerchantId(), "merchant")
                .orElseThrow(() -> {
                    logger.error("Bank details not found for merchantId: {}", dto.getMerchantId());
                    return new ResourceNotFoundException("Bank details not found");
                });
        // 4.Duplicate account check if already exists
        if (!bank.getAccountNumber().equals(dto.getAccountNumber()) &&
                fmMerchantBankDetailsRepository.existsByAccountNumber(dto.getAccountNumber())) {

            logger.error("Duplicate account number found: {}", dto.getAccountNumber());
            throw new DuplicateResourceException("Account number already exists");
        }
        // 5. Update Bank fields
        bank.setRecipientId(dto.getMerchantId()); // important mapping
        bank.setAccountNumber(dto.getAccountNumber());
        bank.setIfscCode(dto.getIfscCode());
        bank.setBankName(dto.getBankName());
        bank.setAccountHolderName(dto.getAccountHolderName());
        bank.setUserType("merchant"); // keep consistent

        fmMerchantBankDetailsRepository.save(bank);
        logger.info("Bank details updated successfully for merchantId: {}", dto.getMerchantId());

        // 6. Returning updated combined [merchant + Bank] data using mapper
        FmMerchantWithBankDto response =
                getMerchantWithBank(dto.getMerchantId());

        logger.info("Returning updated merchant + bank response for merchantId: {}", dto.getMerchantId());

        return response;
    }

}
