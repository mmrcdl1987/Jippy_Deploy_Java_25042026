package com.jippy.foodandmart.service.impl;

import com.jippy.foodandmart.constants.AppConstants;
import com.jippy.foodandmart.dto.OutletTransferRequestDTO;
import com.jippy.foodandmart.dto.OutletTransferResponseDTO;
import com.jippy.foodandmart.entity.Merchant;
import com.jippy.foodandmart.entity.Outlet;
import com.jippy.foodandmart.entity.OutletTransferHistory;
import com.jippy.foodandmart.repository.MerchantRepository;
import com.jippy.foodandmart.repository.OutletRepository;
import com.jippy.foodandmart.repository.OutletTransferHistoryRepository;
import com.jippy.foodandmart.service.interfaces.IOutletTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for transferring an outlet from one merchant to another
 * and querying the transfer history.
 *
 * <p>Why a separate service: outlet transfer involves multiple business rules
 * (both merchants must be active, outlet must be active, no self-transfer) and
 * writes to two tables (outlet_transfer_history + outlets). Isolating this in
 * its own service makes those rules easy to find, test, and audit.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutletTransferServiceImpl implements IOutletTransferService {

    private final OutletRepository                outletRepository;
    private final MerchantRepository              merchantRepository;
    private final OutletTransferHistoryRepository transferHistoryRepository;

    /**
     * Transfers an outlet from its current merchant to a new merchant.
     *
     * <p>Why {@code @Transactional}: both the history insert and the outlet's
     * merchantId update must succeed or fail together. If the outlet update
     * fails after the history is written, we'd have an orphaned history record.</p>
     *
     * <p>Business rules enforced:
     * <ul>
     *   <li>Outlet must exist and be active (isActive = "Y").</li>
     *   <li>Target merchant must exist and be active.</li>
     *   <li>Cannot transfer to the same merchant (self-transfer check).</li>
     * </ul>
     * </p>
     *
     * @param request the transfer request with outletId, toMerchantId, reason, transferredBy
     * @return a {@link OutletTransferResponseDTO} with full transfer details
     * @throws IllegalArgumentException if outlet or merchant not found
     * @throws IllegalStateException    if outlet or target merchant is inactive, or self-transfer
     */
    @Override
    @Transactional
    public OutletTransferResponseDTO transferOutlet(OutletTransferRequestDTO request) {

        Outlet outlet = outletRepository.findById(request.getOutletId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Outlet ID " + request.getOutletId() + " does not exist"));

        // An inactive outlet cannot be transferred — it may have been decommissioned
        if (!AppConstants.FLAG_YES.equalsIgnoreCase(outlet.getIsActive()))
            throw new IllegalStateException("Cannot transfer an inactive outlet (outletId=" + outlet.getOutletId() + ")");

        Integer fromMerchantId = outlet.getMerchantId();

        Merchant toMerchant = merchantRepository.findById(request.getToMerchantId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Target merchant ID " + request.getToMerchantId() + " does not exist"));

        // Target merchant must be active to receive an outlet
        if (!AppConstants.FLAG_YES.equalsIgnoreCase(toMerchant.getIsActive()))
            throw new IllegalStateException("Target merchant is inactive (merchantId=" + toMerchant.getMerchantId() + ")");

        // Self-transfer guard — no point moving an outlet to its current owner
        if (fromMerchantId.equals(request.getToMerchantId()))
            throw new IllegalArgumentException("Outlet already belongs to merchant ID " + fromMerchantId);

        Merchant fromMerchant = merchantRepository.findById(fromMerchantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Current owner merchant ID " + fromMerchantId + " not found"));

        log.info("[TRANSFER] Transferring outletId={} from merchantId={} to merchantId={}",
                outlet.getOutletId(), fromMerchantId, request.getToMerchantId());

        // Persist the transfer history record first (audit trail)
        OutletTransferHistory history = new OutletTransferHistory();
        history.setOutletId(outlet.getOutletId());
        history.setFromMerchantId(fromMerchantId);
        history.setToMerchantId(request.getToMerchantId());
        history.setTransferReason(request.getTransferReason());
        // Status is COMPLETED because the actual ownership change happens in this same transaction
        history.setTransferStatus(AppConstants.TRANSFER_STATUS_COMPLETED);
        history.setTransferredAt(LocalDateTime.now());
        history.setTransferredBy(request.getTransferredBy());
        transferHistoryRepository.save(history);
        log.info("[TRANSFER] History saved: transferId={}", history.getTransferId());

        // Update the outlet to point to its new owner
        outlet.setMerchantId(request.getToMerchantId());
        outlet.setUpdatedAt(LocalDateTime.now());
        outlet.setUpdatedBy(request.getTransferredBy());
        outletRepository.save(outlet);

        // Build response DTO with resolved names
        OutletTransferResponseDTO response = new OutletTransferResponseDTO();
        response.setTransferId(history.getTransferId());
        response.setOutletId(outlet.getOutletId());
        response.setOutletName(outlet.getOutletName());
        response.setFromMerchantId(fromMerchantId);
        response.setFromMerchantName(fromMerchant.getMerchantName());
        response.setToMerchantId(toMerchant.getMerchantId());
        response.setToMerchantName(toMerchant.getMerchantName());
        response.setTransferReason(history.getTransferReason());
        response.setTransferStatus(history.getTransferStatus());
        response.setTransferredAt(history.getTransferredAt());
        return response;
    }

    /**
     * Returns the full transfer history for the specified outlet, newest first.
     *
     * <p>Why ordered descending: the most recent transfers are the most relevant
     * to the admin viewing the history panel, so they appear at the top.</p>
     *
     * @param outletId the outlet's primary key
     * @return list of transfer history records mapped to response DTOs
     * @throws IllegalArgumentException if the outlet does not exist
     */
    @Override
    public List<OutletTransferResponseDTO> getHistoryByOutlet(Integer outletId) {
        if (!outletRepository.existsById(outletId))
            throw new IllegalArgumentException("Outlet ID " + outletId + " does not exist");
        return transferHistoryRepository.findByOutletIdOrderByTransferredAtDesc(outletId)
                .stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    /**
     * Returns all inbound transfers for a merchant (outlets received by this merchant).
     *
     * @param merchantId the merchant's primary key
     * @return list of transfer records where this merchant was the recipient
     * @throws IllegalArgumentException if the merchant does not exist
     */
    @Override
    public List<OutletTransferResponseDTO> getInboundTransfers(Integer merchantId) {
        validateMerchant(merchantId);
        return transferHistoryRepository.findByToMerchantIdOrderByTransferredAtDesc(merchantId)
                .stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    /**
     * Returns all outbound transfers for a merchant (outlets sent away by this merchant).
     *
     * @param merchantId the merchant's primary key
     * @return list of transfer records where this merchant was the original owner
     * @throws IllegalArgumentException if the merchant does not exist
     */
    @Override
    public List<OutletTransferResponseDTO> getOutboundTransfers(Integer merchantId) {
        validateMerchant(merchantId);
        return transferHistoryRepository.findByFromMerchantIdOrderByTransferredAtDesc(merchantId)
                .stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    /**
     * Returns every transfer record in the system, newest first.
     *
     * <p>Why no filter: this is used by the super-admin "all transfers" view
     * which shows the entire platform's transfer history.</p>
     *
     * @return list of all transfer history records
     */
    @Override
    public List<OutletTransferResponseDTO> getAllTransfers() {
        return transferHistoryRepository.findAllByOrderByTransferredAtDesc()
                .stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    /**
     * Validates that a merchant with the given ID exists in the DB.
     *
     * <p>Why a shared helper: inbound and outbound transfer queries both need
     * to validate merchant existence. A helper avoids duplicating the check.</p>
     *
     * @param merchantId the merchant's primary key to validate
     * @throws IllegalArgumentException if the merchant does not exist
     */
    private void validateMerchant(Integer merchantId) {
        if (!merchantRepository.existsById(merchantId))
            throw new IllegalArgumentException("Merchant ID " + merchantId + " does not exist");
    }

    /**
     * Maps an {@link OutletTransferHistory} entity to a response DTO, resolving
     * outlet and merchant names from their respective repositories.
     *
     * <p>Why resolve names here: the history table stores only FK integers.
     * Resolving to display names makes the API response human-readable without
     * requiring the client to make extra requests.</p>
     *
     * <p>Deleted entities are handled gracefully — if an outlet or merchant was
     * deleted after the transfer was recorded, we use a placeholder string
     * "(deleted)" rather than throwing an error.</p>
     *
     * @param h the transfer history entity to convert
     * @return a fully populated {@link OutletTransferResponseDTO}
     */
    private OutletTransferResponseDTO toResponseDTO(OutletTransferHistory h) {
        String outletName = outletRepository.findById(h.getOutletId())
                .map(Outlet::getOutletName).orElse("(deleted)");
        String fromName = merchantRepository.findById(h.getFromMerchantId())
                .map(Merchant::getMerchantName).orElse("(deleted)");
        String toName = merchantRepository.findById(h.getToMerchantId())
                .map(Merchant::getMerchantName).orElse("(deleted)");

        OutletTransferResponseDTO dto = new OutletTransferResponseDTO();
        dto.setTransferId(h.getTransferId());
        dto.setOutletId(h.getOutletId());
        dto.setOutletName(outletName);
        dto.setFromMerchantId(h.getFromMerchantId());
        dto.setFromMerchantName(fromName);
        dto.setToMerchantId(h.getToMerchantId());
        dto.setToMerchantName(toName);
        dto.setTransferReason(h.getTransferReason());
        dto.setTransferStatus(h.getTransferStatus());
        dto.setTransferredAt(h.getTransferredAt());
        return dto;
    }
}
