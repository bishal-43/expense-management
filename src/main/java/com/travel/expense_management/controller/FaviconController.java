package com.travel.expense_management.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaviconController {

    @GetMapping("favicon.ico")
    public void returnNoFavicon() {
        // Return empty response to prevent NoResourceFoundException
    }
}
