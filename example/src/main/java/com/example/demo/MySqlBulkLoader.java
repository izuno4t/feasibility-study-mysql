package com.example.demo;

import com.mysql.cj.jdbc.JdbcStatement;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class MySqlBulkLoader {

    private static final DateTimeFormatter MYSQL_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;

    public MySqlBulkLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * SQLのvaluesを列挙して、1回のSQLクエリで全データをDBに投げる方式
     */
    public void insertByPlainSql() {

        var dataList = generateImportData(100000);
        var values = dataList.stream().map(it -> String.format(" ('%s', '%s', '%s', '%s')", it.getSerialNumber(), it.getDeviceId(), it.getMacAddress(), it.getUpdatedAt().format(MYSQL_DATETIME_FORMAT))).collect(Collectors.toList());

        var sql = String.format("INSERT INTO serial_number2 VALUES %s", String.join(",", values));
        // System.out.println(sql);

        jdbcTemplate.execute(sql);
    }

    /**
     * SQLのvaluesを列挙して、1回のSQLクエリで全データをDBに投げる方式
     */
    public void insertBulLoader() {
        var dataList = generateImportData(100000);
        var values = dataList.stream().map(it -> String.format("%s,%s,%s,%s)", it.getSerialNumber(), it.getDeviceId(), it.getMacAddress(), it.getUpdatedAt().format(MYSQL_DATETIME_FORMAT))).collect(Collectors.toList());

        var datasource = jdbcTemplate.getDataSource();
        if (datasource == null) {
            throw new IllegalStateException("DBのコネクションがないです。");
        }
        try (var conn = datasource.getConnection(); var is = new ReaderInputStream(new StringReader(String.join("\n", values)), StandardCharsets.UTF_8)) {
            var stmt = (JdbcStatement) conn.createStatement();
            stmt.setLocalInfileInputStream(is);
            stmt.execute("LOAD DATA LOCAL INFILE  '/dev/stdin'  INTO TABLE serial_number2 FIELDS TERMINATED BY ',' ENCLOSED BY '\\\"';");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private List<SerialNumberImportDto> generateImportData(int rows) {
        var now = LocalDateTime.now();
        return IntStream.rangeClosed(1, rows).mapToObj(
                it -> {
                    var dto = new SerialNumberImportDto();
                    dto.setSerialNumber(RandomStringUtils.randomAlphanumeric(14).toUpperCase());
                    dto.setDeviceId(RandomStringUtils.randomAlphanumeric(16).toUpperCase());
                    dto.setMacAddress(RandomStringUtils.randomAlphanumeric(12).toUpperCase());
                    dto.setUpdatedAt(now.plusSeconds(it));
                    return dto;
                }
        ).collect(Collectors.toList());
    }
}
