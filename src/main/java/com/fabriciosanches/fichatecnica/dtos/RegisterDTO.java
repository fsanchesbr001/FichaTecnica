package com.fabriciosanches.fichatecnica.dtos;

import com.fabriciosanches.fichatecnica.enums.UserRole;

public record RegisterDTO(String login, String senha, UserRole role) {
}
