package com.expenseshare.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

@RestController
public class OAuth2LoginController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <title>OAuth2 Login</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; margin: 50px; text-align: center; }" +
                "        h1 { color: #333; }" +
                "        a { display: inline-block; margin-top: 20px; padding: 15px 30px; " +
                "            background-color: #4285f4; color: white; text-decoration: none; " +
                "            border-radius: 5px; font-size: 16px; }" +
                "        a:hover { background-color: #357ae8; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <h1>Welcome to OAuth2 Demo</h1>" +
                "    <p>Click below to login with your Google account</p>" +
                "    <a href='/oauth2/authorization/google'>Login with Google</a>" +
                "</body>" +
                "</html>";
    }

    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public String login() {
        return home();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Application is running successfully!");
    }
}
