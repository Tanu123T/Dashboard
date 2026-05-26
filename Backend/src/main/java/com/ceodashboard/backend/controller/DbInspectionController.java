package com.ceodashboard.backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DbInspectionController {

    private final JdbcTemplate jdbcTemplate;

    public DbInspectionController(@Qualifier("hrmsJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/internal/db/tables")
    public ResponseEntity<List<Map<String, Object>>> listTables() {
        String sql = "SELECT table_name, table_schema, engine, table_rows FROM information_schema.tables WHERE table_schema = DATABASE() ORDER BY table_name";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/internal/db/table/{tableName}")
    public ResponseEntity<Map<String, Object>> getTableRows(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "200") int limit
    ) {
        Map<String, Object> result = new HashMap<>();

        // Validate table exists
        String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
        Integer exists = jdbcTemplate.queryForObject(checkSql, Integer.class, tableName);
        if (exists == null || exists == 0) {
            result.put("error", "Table not found: " + tableName);
            return ResponseEntity.badRequest().body(result);
        }

        if (limit < 1) limit = 1;
        if (limit > 2000) limit = 2000;
        if (offset < 0) offset = 0;

        // Count total rows (may be slow for very large tables)
        String countSql = String.format("SELECT COUNT(*) FROM `%s`", tableName);
        long total = jdbcTemplate.queryForObject(countSql, Long.class);

        // Fetch sample rows
        String dataSql = String.format("SELECT * FROM `%s` LIMIT ? OFFSET ?", tableName);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(dataSql, limit, offset);

        result.put("table", tableName);
        result.put("total", total);
        result.put("offset", offset);
        result.put("limit", limit);
        result.put("rows", rows);

        return ResponseEntity.ok(result);
    }
}
