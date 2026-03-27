package com.taskmanager.dto.response.projects;

public record ProjectResponseParticipantDTO(
        Long userId,
        String firstName,
        String lastName,
        String email,
        String role
) {}
