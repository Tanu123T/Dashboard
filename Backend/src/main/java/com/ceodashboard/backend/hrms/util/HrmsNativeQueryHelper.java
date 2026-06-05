package com.ceodashboard.backend.hrms.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.hibernate.Session;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "hrms.db", name = "enabled", havingValue = "true", matchIfMissing = false)
public class HrmsNativeQueryHelper {

    @PersistenceContext(unitName = "hrms")
    private EntityManager entityManager;

    public <T> T queryForObject(String sql, Class<T> type, Object... args) {
        try {
            jakarta.persistence.Query query = entityManager.createNativeQuery(sql);
            bindParameters(query, args);
            Object result = query.getSingleResult();
            return convertValue(result, type);
        } catch (NoResultException ex) {
            return null;
        }
    }

    public List<Map<String, Object>> queryForList(String sql, Object... args) {
        return doWithConnection(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                bindParameters(ps, args);
                try (ResultSet rs = ps.executeQuery()) {
                    return resultSetToList(rs);
                }
            }
        });
    }

    public Map<String, Object> queryForMap(String sql, Object... args) {
        List<Map<String, Object>> rows = queryForList(sql, args);
        return rows.isEmpty() ? new LinkedHashMap<>() : rows.get(0);
    }

    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... args) {
        return doWithConnection(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                bindParameters(ps, args);
                try (ResultSet rs = ps.executeQuery()) {
                    List<T> result = new ArrayList<>();
                    int rowNum = 0;
                    while (rs.next()) {
                        result.add(mapper.mapRow(rs, rowNum++));
                    }
                    return result;
                }
            }
        });
    }

    private <T> T doWithConnection(ConnectionCallback<T> callback) {
        Session session = entityManager.unwrap(Session.class);
        return session.doReturningWork(callback::doInConnection);
    }

    private void bindParameters(PreparedStatement ps, Object... args) throws SQLException {
        if (args == null) {
            return;
        }
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

    private void bindParameters(jakarta.persistence.Query query, Object... args) {
        if (args == null) {
            return;
        }
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i + 1, args[i]);
        }
    }

    private List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnLabel = metaData.getColumnLabel(i);
                if (columnLabel == null || columnLabel.isBlank()) {
                    columnLabel = metaData.getColumnName(i);
                }
                row.put(columnLabel, rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    @SuppressWarnings("unchecked")
    private <T> T convertValue(Object value, Class<T> type) {
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        if (Number.class.isAssignableFrom(type) && value instanceof Number) {
            Number number = (Number) value;
            if (type == Integer.class) {
                return (T) Integer.valueOf(number.intValue());
            }
            if (type == Long.class) {
                return (T) Long.valueOf(number.longValue());
            }
            if (type == Double.class) {
                return (T) Double.valueOf(number.doubleValue());
            }
            if (type == Float.class) {
                return (T) Float.valueOf(number.floatValue());
            }
        }
        if (type == String.class) {
            return (T) String.valueOf(value);
        }
        if (type == Boolean.class && value instanceof Boolean) {
            return (T) value;
        }
        throw new IllegalArgumentException("Unsupported conversion from " + value.getClass() + " to " + type);
    }

    @FunctionalInterface
    private interface ConnectionCallback<T> {
        T doInConnection(Connection connection) throws SQLException;
    }
}
