package com.internship.tracker.controller;

import com.internship.tracker.dto.UserResponse;
import com.internship.tracker.security.CurrentUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    @GetMapping
    UserResponse profile(@AuthenticationPrincipal CurrentUser currentUser) {
        return UserResponse.from(currentUser.user());
    }
}
