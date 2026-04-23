package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.service.interfaces.IUpdateMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

/**
 * Update Menu API
 *
 * GET   /api/update-menu/outlets              → list all outlets (reuse MenuService outlets)
 * GET   /api/update-menu/{outletId}           → get full categorised menu for an outlet
 * POST  /api/update-menu/{outletId}/upload    → upload .xlsx or .csv to upsert menu products
 */
@RestController
@RequestMapping("/api/update-menu")
@RequiredArgsConstructor
@Slf4j
public class UpdateMenuController {

    private final IUpdateMenuService updateMenuService;

    /**
     * GET /api/update-menu/{outletId}
     * Returns the full categorised menu (categories → products → variants).
     */
    @GetMapping("/{outletId}")
    public ResponseEntity<ApiResponse<List<OutletCategoryDTO>>> getMenu(
            @PathVariable Integer outletId) {
        log.info("GET /api/update-menu/{}", outletId);
        try {
            List<OutletCategoryDTO> menu = updateMenuService.getMenuByOutlet(outletId);
            return ResponseEntity.ok(ApiResponse.success("Menu fetched", menu));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/update-menu/{outletId}/upload
     * Accepts .xlsx or .csv with columns:
     *   category | productName | description | price | isVeg | variants
     * variants format: "Small:49,Medium:99,Large:149"
     */
    @PostMapping(value = "/{outletId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UpdateMenuResultDTO>> uploadMenu(
            @PathVariable Integer outletId,
            @RequestParam("file") MultipartFile file) {

        log.info("POST /api/update-menu/{}/upload - file={}, size={}", outletId,
                file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Uploaded file is empty"));
        }

        String fn = (file.getOriginalFilename() != null ? file.getOriginalFilename() : "").toLowerCase();
        List<Map<String, String>> rows;

        try {
            if (fn.endsWith(".xlsx") || fn.endsWith(".xls")) {
                rows = parseExcel(file.getInputStream());
            } else if (fn.endsWith(".csv")) {
                rows = parseCsv(file.getInputStream());
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Only .xlsx or .csv files are supported"));
            }
        } catch (Exception e) {
            log.error("File parse error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to parse file: " + e.getMessage()));
        }

        if (rows.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No data rows found in file"));
        }

        try {
            UpdateMenuResultDTO result = updateMenuService.uploadMenu(rows, outletId);

            String message = String.format(
                    "Menu updated: %d products created, %d updated, %d failed",
                    result.getProductsCreated(), result.getProductsUpdated(), result.getFailureCount());

            HttpStatus status = result.getFailureCount() == 0 ? HttpStatus.OK
                    : (result.getProductsCreated() + result.getProductsUpdated() == 0
                    ? HttpStatus.BAD_REQUEST : HttpStatus.MULTI_STATUS);

            return ResponseEntity.status(status).body(ApiResponse.success(message, result));

        } catch (IllegalArgumentException e) {
            log.warn("Menu upload validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Menu upload error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal error: " + e.getMessage()));
        }
    }

    // ─── Parsers ──────────────────────────────────────────────────────────────

    private List<Map<String, String>> parseExcel(InputStream is) throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return rows;

            // Build header map
            Map<Integer, String> headers = new LinkedHashMap<>();
            for (int c = 0; c <= headerRow.getLastCellNum(); c++) {
                Cell cell = headerRow.getCell(c);
                if (cell != null) {
                    String raw = (cell.getCellType() == CellType.STRING)
                            ? cell.getStringCellValue()
                            : cell.toString();
                    String h = raw.trim().toLowerCase().replaceAll("[\\s_]+", "");
                    headers.put(c, h);
                }
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                // Skip if first meaningful column is blank
                String firstVal = cellStr(row.getCell(0));
                if (firstVal.isBlank()) continue;

                Map<String, String> map = new LinkedHashMap<>();
                for (Map.Entry<Integer, String> e : headers.entrySet()) {
                    map.put(e.getValue(), cellStr(row.getCell(e.getKey())));
                }
                rows.add(map);
            }
        }
        return rows;
    }

    private List<Map<String, String>> parseCsv(InputStream is) throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();
        try (Scanner sc = new Scanner(is)) {
            if (!sc.hasNextLine()) return rows;
            String[] headers = sc.nextLine().split(",", -1);
            String[] normHeaders = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                normHeaders[i] = headers[i].trim().toLowerCase().replaceAll("[\\s_]+", "");
            }
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                // Handle comma inside quoted fields
                String[] cells = splitCsvLine(line);
                if (cells.length == 0 || cells[0].isBlank()) continue;
                Map<String, String> map = new LinkedHashMap<>();
                for (int i = 0; i < normHeaders.length; i++) {
                    map.put(normHeaders[i], i < cells.length ? cells[i].trim() : "");
                }
                rows.add(map);
            }
        }
        return rows;
    }

    /** Splits a CSV line respecting double-quoted fields. */
    private String[] splitCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuote = false;
        StringBuilder sb = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '"') { inQuote = !inQuote; }
            else if (c == ',' && !inQuote) { result.add(sb.toString().trim()); sb.setLength(0); }
            else sb.append(c);
        }
        result.add(sb.toString().trim());
        return result.toArray(new String[0]);
    }

    private String cellStr(Cell c) {
        if (c == null) return "";
        return switch (c.getCellType()) {
            case NUMERIC -> {
                double d = c.getNumericCellValue();
                yield d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(c.getBooleanCellValue());
            default      -> c.toString().trim();
        };
    }
}
