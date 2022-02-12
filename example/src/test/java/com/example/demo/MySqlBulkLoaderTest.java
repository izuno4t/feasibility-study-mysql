package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class MySqlBulkLoaderTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    MySqlBulkLoader mySqlBulkLoader;


    @Test
    void testInsertByPlainSql() {
        mySqlBulkLoader = new MySqlBulkLoader(jdbcTemplate);
        mySqlBulkLoader.insertByPlainSql();
    }

    @Test
    void testInsertBulLoader() {
        mySqlBulkLoader = new MySqlBulkLoader(jdbcTemplate);
        mySqlBulkLoader.insertBulLoader();
    }

}
