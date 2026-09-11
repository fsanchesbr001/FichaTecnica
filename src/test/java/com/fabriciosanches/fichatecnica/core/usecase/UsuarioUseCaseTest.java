package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Seguranca;
import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.out.SegurancaRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UsuarioRepositoryPort;
import com.fabriciosanches.fichatecnica.dtos.AtualizarUsuarioRequestDTO;
import com.fabriciosanches.fichatecnica.dtos.UsuarioListagemDTO;
import com.fabriciosanches.fichatecnica.enums.UserRole;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioUseCaseTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;
    @Mock
    private SegurancaRepositoryPort segurancaRepositoryPort;
    @Mock
    private SegurancaUseCase segurancaUseCase;

    @InjectMocks
    private UsuarioUseCase useCase;

    private Seguranca seguranca;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        seguranca = new Seguranca();
        seguranca.setCodigo(1L);
        seguranca.setEmail("user@email.com");
        seguranca.setCpf("52998224725");
        seguranca.setTentativas(5);
        seguranca.setBloqueado_admin(false);
        seguranca.setBloqueado_tentativas(false);
        seguranca.setBloqueado_expiracao(false);
        seguranca.setPrimeiro_acesso(false);
        seguranca.setDataCriacao(LocalDateTime.now().minusDays(1));
        seguranca.setDataExpiracaoSenha(LocalDateTime.now().plusDays(30));

        usuario = new Usuario(1L, "user@email.com", "senha", UserRole.ADMIN, "Usuário Teste");
    }

    @Test
    void buscarUsuarioPorEmail_DeveRetornarDto() {
        when(segurancaRepositoryPort.buscarPorEmail("user@email.com")).thenReturn(Optional.of(seguranca));
        when(usuarioRepositoryPort.buscarPorLogin("user@email.com")).thenReturn(Optional.of(usuario));

        UsuarioListagemDTO dto = useCase.buscarUsuarioPorEmail("user@email.com");

        assertEquals("Usuário Teste", dto.nome());
        assertEquals("ADMIN", dto.role());
    }

    @Test
    void listarTodosUsuarios_DeveRetornarListaVazia() {
        when(segurancaRepositoryPort.buscarTodos()).thenReturn(List.of());

        assertTrue(useCase.listarTodosUsuarios().isEmpty());
    }

    @Test
    void atualizarUsuario_DeveAtualizarNomeERole() {
        when(segurancaRepositoryPort.buscarPorEmail("user@email.com")).thenReturn(Optional.of(seguranca));
        when(usuarioRepositoryPort.buscarPorLogin("user@email.com")).thenReturn(Optional.of(usuario));

        UsuarioListagemDTO dto = useCase.atualizarUsuario("user@email.com",
                new AtualizarUsuarioRequestDTO(true, false, false, false, "Nome Atualizado", UserRole.SYSTEM));

        assertEquals("Nome Atualizado", dto.nome());
        assertEquals("SYSTEM", dto.role());
    }

    @Test
    void excluirUsuario_DeveFalharSemSeguranca() {
        when(segurancaRepositoryPort.buscarPorEmail("user@email.com")).thenReturn(Optional.empty());

        assertThrows(FichaTecnicaException.class, () -> useCase.excluirUsuario("user@email.com"));
    }
}

