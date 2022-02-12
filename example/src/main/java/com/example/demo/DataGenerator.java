package com.example.demo;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

public class DataGenerator {

    public static void main(String[] args) throws Exception {

        try (var bos = new BufferedOutputStream(new FileOutputStream("data.csv"))) {
            var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            var newLine = System.getProperty("line.separator");
            var now = LocalDateTime.now();

            IntStream.rangeClosed(1, 100000000).forEach(it -> {
                        if (it % 1000 == 0) {
                            System.out.print("*");
                        }
                        if (it % (1000 * 100) == 0) {
                            System.out.print(it + newLine);
                        }
                        var serialNumber = RandomStringUtils.randomAlphanumeric(14).toUpperCase();
                        var deviceId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
                        var macAddress = RandomStringUtils.randomAlphanumeric(12).toUpperCase();
                        try {
                            IOUtils.write(String.format("%s,%s,%s,%s%s", serialNumber, deviceId, macAddress, now.plusSeconds(it).format(formatter), newLine), bos, StandardCharsets.UTF_8);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );
        }
    }

}
