package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.controllers.UsuarioController;
import com.fabriciosanches.fichatecnica.dtos.AtualizarUsuarioRequestDTO;
import com.fabriciosanches.fichatecnica.dtos.UsuarioListagemDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UsuarioControllerTest {

    private MockMvc mockMvc;
    private SegurancaService segurancaService;

    @BeforeEach
    void setUp() {
        segurancaService = Mockito.mock(SegurancaService.class);
        UsuarioController controller = new UsuarioController(segurancaService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void listarRoles_DeveRetornarRolesComLabels() throws Exception {
        mockMvc.perform(get("/ficha-tecnica/usuarios/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0].value").exists())
                .andExpect(jsonPath("$.roles[0].label").exists())
                .andExpect(jsonPath("$.roles[0].labelKey").exists());
    }

    @Test
    void listarTodosUsuarios_DeveRetornarNoContentQuandoListaVazia() throws Exception {
        when(segurancaService.findAllComDadosUsuario()).thenReturn(List.of());

        mockMvc.perform(get("/ficha-tecnica/usuarios/listar-todos-usuarios"))
                .andExpect(status().isNoContent());
    }

    @Test
    void listarTodosUsuarios_DeveRetornarOkQuandoHouverDados() throws Exception {
        when(segurancaService.findAllComDadosUsuario()).thenReturn(List.of(
                new UsuarioListagemDTO("user@email.com", "52998224725", null, 5,
                        false, false, false, false, null, null, null,
                        "Usuário Teste", "ADMIN")
        ));

        mockMvc.perform(get("/ficha-tecnica/usuarios/listar-todos-usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("user@email.com"));
    }

    @Test
    void buscarUsuarioPorEmail_DeveRetornarNotFoundQuandoNaoEncontrar() throws Exception {
        when(segurancaService.findByEmailComDadosUsuario("user@email.com")).thenReturn(null);

        mockMvc.perform(get("/ficha-tecnica/usuarios/buscar-usuario/{email}", "user@email.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarUsuarioPorEmail_DeveRetornarOkQuandoEncontrar() throws Exception {
        when(segurancaService.findByEmailComDadosUsuario("user@email.com"))
                .thenReturn(new UsuarioListagemDTO("user@email.com", "52998224725", null, 5,
                        false, false, false, false, null, null, null,
                        "Usuário Teste", "ADMIN"));

        mockMvc.perform(get("/ficha-tecnica/usuarios/buscar-usuario/{email}", "user@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Usuário Teste"));
    }

    @Test
    void atualizarUsuario_DeveRetornarOkQuandoAtualizar() throws Exception {
        when(segurancaService.atualizarUsuario(eq("user@email.com"), any(AtualizarUsuarioRequestDTO.class)))
                .thenReturn(new UsuarioListagemDTO("user@email.com", "52998224725", null, 5,
                        false, false, false, false, null, null, null,
                        "Nome Atualizado", "SYSTEM"));

        mockMvc.perform(put("/ficha-tecnica/usuarios/atualizar-usuario/{email}", "user@email.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bloqueado_admin": false,
                                  "bloqueado_tentativas": false,
                                  "bloqueado_expiracao": false,
                                  "primeiro_acesso": false,
                                  "nome": "Nome Atualizado",
                                  "role": "SYSTEM"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"));
    }

    @Test
    void atualizarUsuario_DeveRetornarBadRequestQuandoFalhar() throws Exception {
        when(segurancaService.atualizarUsuario(eq("user@email.com"), any(AtualizarUsuarioRequestDTO.class)))
                .thenThrow(new FichaTecnicaException("erro"));

        mockMvc.perform(put("/ficha-tecnica/usuarios/atualizar-usuario/{email}", "user@email.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Nome Atualizado",
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}

