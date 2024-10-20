package com.example.demo.service;


import com.example.demo.model.UrlMapping;
import com.example.demo.repository.UrlRepository;

import jakarta.transaction.Transactional;

import com.example.demo.dto.UrlRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UrlShortenerService {

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private final String BASE_URL = "https://ample-happiness-production.up.railway.app/api/url/";

    public String shortenUrl(UrlRequest urlRequest) {
        String uniqueID = generateShortUrl();
        String shortUrl = BASE_URL + uniqueID;

        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(urlRequest.getOriginalUrl());
        urlMapping.setShortUrl(uniqueID);
        urlRepository.save(urlMapping);

        redisTemplate.opsForValue().set(uniqueID, urlRequest.getOriginalUrl());

        return shortUrl;
    }

    public String getOriginalUrl(String shortUrl) {
        String cachedUrl = redisTemplate.opsForValue().get(shortUrl);
        if (cachedUrl != null) {
            return cachedUrl;
        }

        UrlMapping urlMapping = urlRepository.findByShortUrl(shortUrl);
        if (urlMapping == null) {
            throw new RuntimeException("URL no encontrada");
        }
        redisTemplate.opsForValue().set(shortUrl, urlMapping.getOriginalUrl());

        return urlMapping.getOriginalUrl();
    }

    private String generateShortUrl() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Transactional
    public void updateUrl(String shortUrl, String newOriginalUrl) {
        UrlMapping url = urlRepository.findByShortUrl(shortUrl);
        if (url != null) {
            url.setOriginalUrl(newOriginalUrl);
            urlRepository.save(url);

            redisTemplate.opsForValue().set(shortUrl, newOriginalUrl);
        }
    }

    @Transactional
    public void disableUrl(String shortUrl) {
        UrlMapping urlMapping = urlRepository.findByShortUrl(shortUrl);
        if (urlMapping != null) {
            urlRepository.delete(urlMapping);
            redisTemplate.opsForValue().getOperations().delete(shortUrl);
        } else {
            throw new RuntimeException("URL no encontrada");
        }
    }
}
