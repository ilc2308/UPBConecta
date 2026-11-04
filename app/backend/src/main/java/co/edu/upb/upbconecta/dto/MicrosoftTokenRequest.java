package co.edu.upb.upbconecta.dto;

import jakarta.validation.constraints.NotBlank;

public record MicrosoftTokenRequest(@NotBlank String idToken) {}
