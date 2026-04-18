package com.jippy.foodandmart.repository;


import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.projections.FmOutletMenuProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FmOutletRepository extends JpaRepository<FmOutlet, Integer> {

    @Query(value = """
               SELECT
                           -- Outlet basic details (from outlets table)
                           o.outlet_id,          -- jippy_fm.outlets
                           o.outlet_name,        -- jippy_fm.outlets
                           o.outlet_phone,       -- jippy_fm.outlets
            
                           --- online pricing details (from product_online_pricing table)
                           --product_id from product_online_pricing table 
                           pop.product_id AS pop_id,          -- jippy_fm.product_online_pricing
                           pop.online_price,        -- jippy_fm.product_online_pricing
            
                           -- Category details (from categories table)
                           c.category_id,        -- jippy_fm.categories
                           c.category_name,      -- jippy_fm.categories
            
                           -- Product details (from products table)
                           p.product_id,         -- jippy_fm.products
                           p.product_name,       -- jippy_fm.products
                           p.description,        -- jippy_fm.products
                           p.merchant_price,     -- jippy_fm.products
                           p.is_veg,             -- jippy_fm.products
                           p.has_product_variants, -- jippy_fm.products
            
                           -- Outlet day-wise availability (from outlet_days table)
                           od.is_open,           -- jippy_fm.outlet_days
                           od.opening_time,      -- jippy_fm.outlet_days
                           od.closing_time,      -- jippy_fm.outlet_days
            
                           -- Outlet day name (from days_of_week table via outlet_days)
                           d1.day_name AS outlet_day,   -- jippy_fm.days_of_week
            
                           -- Product available timings (from product_available_timings table)
                           pat.start_time,       -- jippy_fm.product_available_timings
                           pat.end_time,         -- jippy_fm.product_available_timings
            
                           -- Product day name (from days_of_week table via product_available_timings)
                           d2.day_name AS product_day   -- jippy_fm.days_of_week
            
            
                       -- Start from outlet (main table: jippy_fm.outlets)
                       FROM jippy_fm.outlets o
            
            
                       -- Join outlet_categories (maps outlet to categories)
                       JOIN jippy_fm.outlet_categories oc
                           ON o.outlet_id = oc.outlet_id
            
            
                       -- Join categories (get category details)
                       JOIN jippy_fm.categories c
                           ON oc.category_id = c.category_id
            
            
                         -- Join products (get products under each outlet_category)
                       JOIN jippy_fm.products p
                           ON oc.outlet_category_id = p.outlet_category_id
                       --JOIN jippy_fm.product_online_pricing pop
                            --ON p.product_id = pop.product_id
            
            
                -- for online pricing details 
                --(get online price for each product by product_id and outlet_category_id)
                            LEFT JOIN jippy_fm.product_online_pricing pop
                               ON p.product_id = pop.product_id
                               AND p.outlet_category_id = pop.outlet_category_id
            
            
                       -- Left join outlet_days (get outlet timings per day)
                       LEFT JOIN jippy_fm.outlet_days od
                           ON o.outlet_id = od.outlet_id
            
            
                       -- Join days_of_week for outlet days (convert day_id to name)
                       LEFT JOIN jippy_fm.days_of_week d1
                           ON od.day_of_week_id = d1.day_id
            
            
                       -- Left join product_available_timings (get product timing per day)
                       -- Condition ensures product timing matches outlet day
                       LEFT JOIN jippy_fm.product_available_timings pat
                           ON p.product_id = pat.product_id
                           AND od.day_of_week_id = pat.day_of_week_id
            
            
                       -- Join days_of_week for product days (convert day_id to name)
                       LEFT JOIN jippy_fm.days_of_week d2
                           ON pat.day_of_week_id = d2.day_id
            
            
                       -- Filter by outlet_id (input parameter from API)
            
                        WHERE o.is_approved = true AND o.outlet_id = :outletId  --for Api response @query
                       --WHERE o.is_approved = true AND o.outlet_id = 1  --for postgres SQL testing used 
            
            
                       -- Order results to simplify grouping in service layer
                       ORDER BY
                           c.category_id,
                           p.product_id,
                           od.day_of_week_id,
                           pat.start_time;
            """, nativeQuery = true)
    List<FmOutletMenuProjection> getOutletMenu(@Param("outletId") Integer outletId);
}