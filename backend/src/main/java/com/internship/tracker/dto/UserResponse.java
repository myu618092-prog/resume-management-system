package com.internship.tracker.dto;

import com.internship.tracker.entity.User;

public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String school,
        String major,
        String skills
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getSchool(),
                user.getMajor(),
                user.getSkills()
        );
    }
}
