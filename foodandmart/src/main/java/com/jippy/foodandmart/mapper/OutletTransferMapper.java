package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.OutletTransferRequestDTO;
import com.jippy.foodandmart.dto.OutletTransferResponseDTO;
import com.jippy.foodandmart.entity.OutletTransferHistory;

import java.time.LocalDateTime;

/**
 * Static utility class for converting between {@link OutletTransferRequestDTO} /
 * {@link OutletTransferResponseDTO} and the {@link OutletTransferHistory} entity.
 *
 * <p>Why a separate mapper: the transfer flow involves resolving merchant names
 * from integer FKs. By accepting those resolved values as parameters this
 * mapper remains free of repository dependencies and is easy to unit-test.</p>
 */
public final class OutletTransferMapper {

    /**
     * Private constructor — static utility class, must not be instantiated.
     */
    private OutletTransferMapper() {}

    /**
     * Converts an {@link OutletTransferRequestDTO} into a new
     * {@link OutletTransferHistory} entity.
     *
     * <p>Why fromMerchantId is a separate parameter: the DTO only carries the
     * target merchant ID. The service derives the current owner merchant ID
     * from the Outlet entity and passes it here, keeping this mapper agnostic
     * of business-logic lookups.</p>
     *
     * <p>transferStatus is always set to "COMPLETED" here because a transfer
     * record is only written after the ownership reassignment has succeeded.
     * Failed transfers do not produce a history record.</p>
     *
     * @param dto            the inbound transfer request
     * @param fromMerchantId the current owner merchant ID (resolved by service)
     * @return a transient {@link OutletTransferHistory} entity ready to persist
     */
    public static OutletTransferHistory toEntity(OutletTransferRequestDTO dto, Integer fromMerchantId) {
        OutletTransferHistory entity = new OutletTransferHistory();
        entity.setOutletId(dto.getOutletId());
        // fromMerchantId is the current owner — derived from the Outlet entity in the service
        entity.setFromMerchantId(fromMerchantId);
        entity.setToMerchantId(dto.getToMerchantId());
        entity.setTransferReason(dto.getTransferReason());
        // Only COMPLETED transfers are persisted; failed attempts are thrown as exceptions
        entity.setTransferStatus("COMPLETED");
        entity.setTransferredAt(LocalDateTime.now());
        entity.setTransferredBy(dto.getTransferredBy());
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    /**
     * Converts an {@link OutletTransferHistory} entity (plus resolved display names)
     * into an {@link OutletTransferResponseDTO}.
     *
     * <p>Why names are parameters: the entity stores only integer FKs. The
     * service resolves outletName, fromMerchantName, and toMerchantName via
     * repository lookups and passes them in so this mapper stays free of
     * repository dependencies.</p>
     *
     * @param entity           the persisted transfer history record
     * @param outletName       human-readable outlet name (resolved by service)
     * @param fromMerchantName display name of the previous owner merchant
     * @param toMerchantName   display name of the new owner merchant
     * @return a fully populated {@link OutletTransferResponseDTO}
     */
    public static OutletTransferResponseDTO toDTO(OutletTransferHistory entity,
                                                  String outletName,
                                                  String fromMerchantName,
                                                  String toMerchantName) {
        OutletTransferResponseDTO dto = new OutletTransferResponseDTO();
        dto.setTransferId(entity.getTransferId());
        dto.setOutletId(entity.getOutletId());
        // Names are passed in since the entity only stores integer FKs
        dto.setOutletName(outletName);
        dto.setFromMerchantId(entity.getFromMerchantId());
        dto.setFromMerchantName(fromMerchantName);
        dto.setToMerchantId(entity.getToMerchantId());
        dto.setToMerchantName(toMerchantName);
        dto.setTransferReason(entity.getTransferReason());
        dto.setTransferStatus(entity.getTransferStatus());
        dto.setTransferredAt(entity.getTransferredAt());
        return dto;
    }
}
