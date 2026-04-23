package com.jippy.foodandmart.service.interfaces;

import com.jippy.foodandmart.dto.BulkUploadResultDTO;
import com.jippy.foodandmart.dto.MerchantRequestDTO;
import com.jippy.foodandmart.entity.Merchant;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IMerchantService {

    List<Merchant> getAllMerchants();

    Merchant getMerchantById(Integer id);

    long countMerchants();

    Merchant createMerchant(MerchantRequestDTO dto);

    BulkUploadResultDTO bulkUpload(MultipartFile file);
}
