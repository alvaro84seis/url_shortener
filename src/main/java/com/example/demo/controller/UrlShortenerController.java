package com.example.demo.controller;


import com.example.demo.service.UrlShortenerService;
import com.example.demo.dto.UrlRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/url")
public class UrlShortenerController {

    @Autowired
    private UrlShortenerService urlShortenerService;

    @PostMapping
    public ResponseEntity<String> shortenUrl(@RequestBody UrlRequest urlRequest) {
        String shortUrl = urlShortenerService.shortenUrl(urlRequest);
        return ResponseEntity.ok(shortUrl);
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirectToUrl(@PathVariable String shortUrl, HttpServletResponse response) throws IOException {
        try {
            String originalUrl = urlShortenerService.getOriginalUrl(shortUrl);
            response.sendRedirect(originalUrl);
            return ResponseEntity.status(HttpStatus.FOUND).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{shortUrl}")
    public ResponseEntity<Void> updateUrl(@PathVariable String shortUrl, @RequestBody UrlRequest urlRequest) {
        urlShortenerService.updateUrl(shortUrl, urlRequest.getOriginalUrl());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{shortUrl}")
    public ResponseEntity<Void> disableUrl(@PathVariable String shortUrl) {
        urlShortenerService.disableUrl(shortUrl);
        return ResponseEntity.ok().build();
    }
}