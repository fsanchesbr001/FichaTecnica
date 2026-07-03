package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.Usuario;
import com.fabriciosanches.fichatecnica.enums.UserRole;
import com.fabriciosanches.fichatecnica.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private AutenticacaoService service;

    @Test
    void loadUserByUsername_DeveRetornarUsuarioQuandoExistir() {
        Usuario usuario = new Usuario(1L, "admin@email.com", "senha", UserRole.ADMIN, "Admin");
        when(repository.findByLogin("admin@email.com")).thenReturn(usuario);

        UserDetails resultado = service.loadUserByUsername("admin@email.com");

        assertSame(usuario, resultado);
        verify(repository).findByLogin("admin@email.com");
    }

    @Test
    void loadUserByUsername_DeveRetornarNullQuandoNaoEncontrar() {
        when(repository.findByLogin("nao-existe@email.com")).thenReturn(null);

        UserDetails resultado = service.loadUserByUsername("nao-existe@email.com");

        assertNull(resultado);
        verify(repository).findByLogin("nao-existe@email.com");
    }
}

