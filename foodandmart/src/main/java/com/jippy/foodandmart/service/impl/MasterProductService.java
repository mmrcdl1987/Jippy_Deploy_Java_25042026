package com.jippy.foodandmart.service.impl;

import com.jippy.foodandmart.dto.CompareFileResponse;
import com.jippy.foodandmart.dto.MasterProductRequest;
import com.jippy.foodandmart.entity.Category;
import com.jippy.foodandmart.entity.MasterProduct;
import com.jippy.foodandmart.exception.FileProcessingException;
import com.jippy.foodandmart.exception.MasterProductNotFoundException;
import com.jippy.foodandmart.mapper.MasterProductMapper;
import com.jippy.foodandmart.mapper.ProductMapper;
import com.jippy.foodandmart.repository.CategoryRepository;
import com.jippy.foodandmart.repository.MasterProductRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MasterProductService {

    private final MasterProductRepository masterProductRepository;
    private final FileConverterService fileConverterService;
    private final CategoryRepository categoryRepository;

    // ── CREATE ────────────────────────────────────────────────────────────────

    public MasterProduct save(MasterProductRequest req) {
        MasterProductMapper.validate(req);
        MasterProduct entity = MasterProductMapper.toEntity(req);
        MasterProduct saved = masterProductRepository.save(entity);
        log.info("[MASTER] Saved id={} name={}", saved.getMasterProductId(), saved.getMasterProductName());
        return saved;
    }

    public List<MasterProduct> saveAll(List<MasterProductRequest> requests) {
        if (requests == null || requests.isEmpty())
            throw new IllegalArgumentException("Product list cannot be null or empty.");

        List<MasterProduct> toInsert = new ArrayList<>();
        for (MasterProductRequest req : requests) {
            if (req.getMasterProductName() == null || req.getMasterProductName().isBlank()) {
                log.warn("[MASTER] Skipping entry with blank name");
                continue;
            }
            if (req.getCategoryId() == null || req.getCategoryName() == null || req.getCategoryName().isBlank()) {
                log.warn("[MASTER] Skipping '{}' — missing categoryId or categoryName", req.getMasterProductName());
                continue;
            }
            if (!masterProductRepository.existsByMasterProductNameIgnoreCase(req.getMasterProductName())) {
                toInsert.add(MasterProductMapper.toEntity(req));
            } else {
                log.debug("[MASTER] Skipping duplicate: {}", req.getMasterProductName());
            }
        }
        List<MasterProduct> saved = masterProductRepository.saveAll(toInsert);
        log.info("[MASTER] Bulk insert: {}/{}", saved.size(), requests.size());
        return saved;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MasterProduct> getAll() {
        return masterProductRepository.findAllByOrderByMasterProductIdAsc();
    }

    @Transactional(readOnly = true)
    public MasterProduct getById(Integer id) {
        return masterProductRepository.findById(id)
                .orElseThrow(() -> new MasterProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<MasterProduct> filter(String type) {
        String normalised = MasterProductMapper.validateType(type);
        return masterProductRepository.filterByType(normalised);
    }

    @Transactional(readOnly = true)
    public List<MasterProduct> search(String keyword) {
        String kw = MasterProductMapper.validateSearchKeyword(keyword);
        return masterProductRepository.searchByName(kw);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public MasterProduct update(Integer id, MasterProductRequest req) {
        MasterProductMapper.validate(req);
        MasterProduct existing = masterProductRepository.findById(id)
                .orElseThrow(() -> new MasterProductNotFoundException(id));
        MasterProductMapper.updateEntity(existing, req);
        MasterProduct saved = masterProductRepository.save(existing);
        log.info("[MASTER] Updated id={}", saved.getMasterProductId());
        return saved;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public void delete(Integer id) {
        if (!masterProductRepository.existsById(id))
            throw new MasterProductNotFoundException(id);
        masterProductRepository.deleteById(id);
        log.info("[MASTER] Deleted id={}", id);
    }

    // ── PHOTO UPLOAD ──────────────────────────────────────────────────────────

    public String updatePhoto(Integer id, MultipartFile photo) {
        if (photo == null || photo.isEmpty())
            throw new IllegalArgumentException("Photo file cannot be empty.");
        MasterProductMapper.validatePhoto(photo.getContentType(), photo.getSize());
        try {
            byte[] bytes = photo.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String uri = "data:" + photo.getContentType() + ";base64," + base64;
            MasterProduct mp = masterProductRepository.findById(id)
                    .orElseThrow(() -> new MasterProductNotFoundException(id));
            mp.setPhoto(uri);
            masterProductRepository.save(mp);
            log.info("[MASTER] Photo saved id={}", id);
            return uri;
        } catch (MasterProductNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new FileProcessingException("Failed to store photo: " + e.getMessage(), e);
        }
    }

    // ── FILE COMPARE ──────────────────────────────────────────────────────────

    public CompareFileResponse compareFileWithDB(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File is null or empty.");
        if (file.getSize() > 10 * 1024 * 1024L)
            throw new IllegalArgumentException("File exceeds 10 MB size limit.");

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            throw new FileProcessingException("Failed to read file bytes: " + e.getMessage(), e);
        }

        InputStream csvStream = fileConverterService.convertToCsvFromBytes(fileBytes, file.getOriginalFilename());
        List<MasterProduct> parsed = parseCsv(csvStream);

        if (parsed.isEmpty())
            return new CompareFileResponse(List.of(), List.of(), 0, 0, 0, 0);

        List<MasterProduct> db = masterProductRepository.findAllByOrderByMasterProductIdAsc();
        Map<String, MasterProduct> dbLookup = new HashMap<>();
        for (MasterProduct d : db) {
            if (d.getMasterProductName() != null)
                dbLookup.put(norm(d.getMasterProductName()), d);
        }

        List<CompareFileResponse.CompareItem> dupes = new ArrayList<>();
        List<CompareFileResponse.CompareItem> newOnes = new ArrayList<>();
        int skipped = 0;

        for (MasterProduct fp : parsed) {
            if (fp.getMasterProductName() == null || fp.getMasterProductName().isBlank()) {
                skipped++;
                continue;
            }
            MasterProduct dbMatch = dbLookup.get(norm(fp.getMasterProductName()));
            if (dbMatch != null) {
                dupes.add(toCompareItem(dbMatch.getMasterProductId(), dbMatch, fp.getCsvMerchantPrice(), fp.getCsvTiming(), fp.getCsvDayOfWeek()));
            } else {
                newOnes.add(toCompareItem(null, fp, fp.getCsvMerchantPrice(), fp.getCsvTiming(), fp.getCsvDayOfWeek()));
            }
        }

        log.info("[MASTER] Compare: dup={} new={} skipped={}", dupes.size(), newOnes.size(), skipped);
        return new CompareFileResponse(dupes, newOnes, parsed.size(), dupes.size(), newOnes.size(), skipped);
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    private CompareFileResponse.CompareItem toCompareItem(Integer id, MasterProduct mp, Double csvPrice, String csvTiming, String csvDayOfWeek) {
        log.info("[MASTER] toCompareItem: product='{}' id={} csvPrice={} csvTiming={} csvDayOfWeek={}", mp.getMasterProductName(), id, csvPrice, csvTiming, csvDayOfWeek);
        return new CompareFileResponse.CompareItem(
                id,
                mp.getMasterProductName(),
                mp.getVeg(),
                mp.getNonVeg(),
                mp.getCategoryId(),
                mp.getCategoryName(),
                mp.getSubCategoryId(),
                mp.getSubCategoryName(),
                mp.getDescription(),
                mp.getShortDescription(),
                mp.getPhoto(),
                mp.getPhotos(),
                mp.getThumbnail(),
                mp.getFoodType(),
                mp.getCuisineType(),
                mp.getHasOptions(),
                mp.getOptionsEnabled(),
                mp.getOptions(),
                mp.getCalories(),
                mp.getProtein(),
                mp.getFats(),
                mp.getCarbs(),
                mp.getGrams(),
                mp.getPublish(),
                csvPrice,     // merchant_price from uploaded CSV file
                csvTiming,    // availability timing from uploaded CSV file
                csvDayOfWeek  // day-of-week name from uploaded CSV file
        );
    }

    private List<MasterProduct> parseCsv(InputStream stream) {
        List<MasterProduct> list = new ArrayList<>();
        // Clear any prices/timings from a previous compare run — each upload is independent
        ProductMapper.priceMapper.clear();
        ProductMapper.timingMapper.clear();
        ProductMapper.dayOfWeekMapper.clear();
        try (CSVReader r = new CSVReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String[] row;
            boolean header = true;
            int nameIdx = -1, descIdx = -1, shortDescIdx = -1,
                    vegIdx = -1, nonVegIdx = -1,
                    categoryIdIdx = -1, categoryNameIdx = -1,
                    photoIdx = -1, photosIdx = -1, thumbnailIdx = -1,
                    foodTypeIdx = -1, cuisineTypeIdx = -1,
                    hasOptionsIdx = -1, optionsEnabledIdx = -1,
                    optionsIdx = -1, publishIdx = -1,
                    caloriesIdx = -1, proteinsIdx = -1, fatsIdx = -1,
                    carbsIdx = -1, gramsIdx = -1,priceIdx=-1, timingIdx=-1, dayOfWeekIdx=-1;


            while ((row = r.readNext()) != null) {
                if (header) {
                    header = false;
                    // Log ALL column headers so we can diagnose unrecognized column names
                    log.info("[MASTER] CSV headers detected: {}", Arrays.toString(row));
                    for (int i = 0; i < row.length; i++) {
                        String h = norm(row[i]);
                        if ("masterproductname".equals(h) || "name".equals(h)) nameIdx = i;
                        if ("description".equals(h)) descIdx = i;
                        if ("short_description".equals(h) || "shortdescription".equals(h)) shortDescIdx = i;
                        if ("veg".equals(h)) vegIdx = i;
                        if ("nonveg".equals(h) || "non_veg".equals(h)) nonVegIdx = i;
                        if ("categoryid".equals(h) || "category_id".equals(h)) categoryIdIdx = i;
                        if ("categoryname".equals(h) || "category_name".equals(h)
                                || "category".equals(h) || "categorytitle".equals(h)
                                || "category_title".equals(h)) categoryNameIdx = i;
                        if ("photo".equals(h)) photoIdx = i;
                        if ("photos".equals(h)) photosIdx = i;
                        if ("thumbnail".equals(h)) thumbnailIdx = i;
                        if ("food_type".equals(h) || "foodtype".equals(h)) foodTypeIdx = i;
                        if ("cuisine_type".equals(h) || "cuisinetype".equals(h)) cuisineTypeIdx = i;
                        if ("has_options".equals(h) || "hasoptions".equals(h)) hasOptionsIdx = i;
                        if ("options_enabled".equals(h) || "optionsenabled".equals(h)) optionsEnabledIdx = i;
                        if ("options".equals(h)) optionsIdx = i;
                        if ("publish".equals(h)) publishIdx = i;
                        if ("calories".equals(h)) caloriesIdx = i;
                        if ("proteins".equals(h) || "protein".equals(h)) proteinsIdx = i;
                        if ("fats".equals(h)) fatsIdx = i;
                        if ("carbs".equals(h)) carbsIdx = i;
                        if ("grams".equals(h)) gramsIdx = i;
                        // Fuzzy price column detection: match any header that contains
                        // "price"/"pric", "mrp", or contains "merchant"/"merchat" —
                        // covers common user typos such as "merchat_pricce".
                        if (h.contains("price") || h.contains("pric") || h.equals("mrp")
                                || h.contains("merchant") || h.contains("merchat")) {
                            priceIdx = i;
                            log.info("[MASTER] Price column detected at index {}: '{}'", i, h);
                        }
                        // Timing column: match "timing","time","avail","avelabule" — covers typos.
                        if (h.contains("timing") || h.contains("time") || h.contains("avail")
                                || h.contains("avelabule")) {
                            timingIdx = i;
                            log.info("[MASTER] Timing column detected at index {}: '{}'", i, h);
                        }
                        // Day-of-week column: match "dayofaweek","daysofaweek","daysofweek","weekday"
                        if (h.contains("dayofaweek") || h.contains("daysofaweek")
                                || h.contains("daysofweek") || h.contains("weekday")
                                || h.equals("day") || h.equals("days")) {
                            dayOfWeekIdx = i;
                            log.info("[MASTER] DayOfWeek column detected at index {}: '{}'", i, h);
                        }
                    }
                    continue;
                }
                MasterProduct mp = new MasterProduct();
                mp.setMasterProductName(safeGet(row, nameIdx));
                mp.setDescription(safeGet(row, descIdx));
                mp.setShortDescription(safeGet(row, shortDescIdx));
                mp.setPhoto(safeGet(row, photoIdx));
                mp.setPhotos(safeGetRaw(row, photosIdx));   // keep JSON as-is
                mp.setThumbnail(safeGet(row, thumbnailIdx));
                mp.setFoodType(safeGet(row, foodTypeIdx));
                mp.setCuisineType(safeGet(row, cuisineTypeIdx));
                mp.setOptions(safeGetRaw(row, optionsIdx)); // keep JSON as-is

                String v = norm(safeGet(row, vegIdx));
                String nv = norm(safeGet(row, nonVegIdx));
                mp.setVeg("1".equals(v) || "true".equals(v) ? 1 : 0);
                mp.setNonVeg("1".equals(nv) || "true".equals(nv) ? 1 : 0);

                mp.setHasOptions(parseIntSafe(safeGet(row, hasOptionsIdx)));
                mp.setOptionsEnabled(parseIntSafe(safeGet(row, optionsEnabledIdx)));
                mp.setPublish(parseIntSafe(safeGet(row, publishIdx), 1));
                mp.setCalories(parseIntSafe(safeGet(row, caloriesIdx)));
                mp.setProtein(parseIntSafe(safeGet(row, proteinsIdx)));
                mp.setFats(parseIntSafe(safeGet(row, fatsIdx)));
                mp.setCarbs(parseIntSafe(safeGet(row, carbsIdx)));
                mp.setGrams(parseIntSafe(safeGet(row, gramsIdx)));

                String catIdStr = safeGet(row, categoryIdIdx);
                String catName = safeGet(row, categoryNameIdx);
                mp.setCategoryName(catName);

                String rawPrice = safeGet(row, priceIdx);
                double csvPrice = 0.0;
                if (rawPrice != null && !rawPrice.isBlank()) {
                    // Strip currency symbols, commas, spaces (e.g. "₹1,234" → "1234")
                    String cleanPrice = rawPrice.replaceAll("[^0-9.]", "").trim();
                    try {
                        if (!cleanPrice.isBlank()) csvPrice = Double.parseDouble(cleanPrice);
                    } catch (NumberFormatException ignored) {
                        log.warn("[MASTER] Could not parse price rawPrice='{}' cleanPrice='{}' for product='{}'",
                                rawPrice, cleanPrice, mp.getMasterProductName());
                    }
                }
                log.info("[MASTER] Product='{}' priceIdx={} rawPrice='{}' csvPrice={}", mp.getMasterProductName(), priceIdx, rawPrice, csvPrice);
                mp.setCsvMerchantPrice(csvPrice);
                ProductMapper.priceMapper.put(safeGet(row, nameIdx), csvPrice);

                // Parse timing column (e.g. "7:00-22:00")
                String csvTiming = safeGet(row, timingIdx);
                if (csvTiming != null && csvTiming.isBlank()) csvTiming = null;
                mp.setCsvTiming(csvTiming);
                ProductMapper.timingMapper.put(safeGet(row, nameIdx), csvTiming);

                // Parse day-of-week column
                String csvDayOfWeek = safeGet(row, dayOfWeekIdx);
                if (csvDayOfWeek != null && csvDayOfWeek.isBlank()) csvDayOfWeek = null;
                mp.setCsvDayOfWeek(csvDayOfWeek);
                ProductMapper.dayOfWeekMapper.put(safeGet(row, nameIdx), csvDayOfWeek);
                log.info("[MASTER] Product='{}' dayOfWeekIdx={} csvDayOfWeek='{}'", mp.getMasterProductName(), dayOfWeekIdx, csvDayOfWeek);
                log.info("[MASTER] Product='{}' timingIdx={} csvTiming='{}'", mp.getMasterProductName(), timingIdx, csvTiming);

                if (catIdStr != null && !catIdStr.isBlank()) {
                    try {
                        mp.setCategoryId(Integer.parseInt(catIdStr.trim()));
                    } catch (NumberFormatException ignored) {
                        mp.setCategoryId(resolveOrCreateCategoryId(catName));
                    }
                } else if (catName != null && !catName.isBlank()) {
                    mp.setCategoryId(resolveOrCreateCategoryId(catName));
                }
                list.add(mp);
            }
        } catch (Exception e) {
            throw new FileProcessingException("CSV parse error: " + e.getMessage(), e);
        }
        return list;
    }

    private int parseIntSafe(String val) {
        return parseIntSafe(val, 0);
    }

    private int parseIntSafe(String val, int defaultVal) {
        if (val == null || val.isBlank()) return defaultVal;
        try {
            return (int) Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private Integer resolveOrCreateCategoryId(String catName) {
        if (catName == null || catName.isBlank()) return null;
        Category cat = categoryRepository.findByCategoryNameIgnoreCase(catName.trim())
                .orElseGet(() -> {
                    Category newCat = new Category();
                    newCat.setCategoryName(catName.trim());
                    return categoryRepository.save(newCat);
                });
        return cat.getCategoryId();
    }

    /**
     * Strips quotes and whitespace — for non-JSON fields.
     */
    private String safeGet(String[] row, int idx) {
        if (idx < 0 || idx >= row.length) return null;
        return row[idx] == null ? null : row[idx].replace("\"", "").replace("\r", "").trim();
    }

    /**
     * Preserves raw value (including JSON brackets) — for photo arrays and options.
     */
    private String safeGetRaw(String[] row, int idx) {
        if (idx < 0 || idx >= row.length) return null;
        String val = row[idx];
        if (val == null) return null;
        val = val.replace("\r", "").trim();
        return val.isEmpty() ? null : val;
    }

    private String norm(String v) {
        if (v == null) return "";
        return v.replace("\"", "").replace("\r", "").replace("\n", "")
                .trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
