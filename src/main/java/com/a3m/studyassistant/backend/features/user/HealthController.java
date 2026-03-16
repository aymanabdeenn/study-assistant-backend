package com.a3m.studyassistant.backend.features.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

@RestController
@RequestMapping("/api/public")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/db-check")
    public String checkDb() {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("Connection is working!!!");
            return "Connected to Supabase Postgres: " + conn.getMetaData().getDatabaseProductVersion();
        } catch (Exception e) {
            return "Database Connection Failed: " + e.getMessage();
        }
    }
}
