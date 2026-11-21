package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.enums.UploadJobStatus;
import com.fabriciosanches.fichatecnica.core.ports.in.ConsultarUploadImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.IniciarUploadImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarJobsUploadImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RemoverImagemProdutoPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.UploadJobDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(ProdutoImagemPermissoesTest.Config.class)
class ProdutoImagemPermissoesTest {

    @Autowired
    private ProdutoImagemController controller;

    @WithMockUser(roles = "USER")
    @ParameterizedTest
    @EnumSource(Operacao.class)
    void userPodeGerenciarImagemDoProduto(Operacao operacao) {
        assertThat(executar(operacao).getStatusCode().is2xxSuccessful()).isTrue();
    }

    @WithMockUser(roles = "ADMIN")
    @ParameterizedTest
    @EnumSource(Operacao.class)
    void adminContinuaPodendoGerenciarImagem(Operacao operacao) {
        assertThat(executar(operacao).getStatusCode().is2xxSuccessful()).isTrue();
    }

    @WithMockUser(roles = "SYSTEM")
    @ParameterizedTest
    @EnumSource(Operacao.class)
    void systemNaoGanhaAcessoAsImagens(Operacao operacao) {
        assertThatThrownBy(() -> executar(operacao)).isInstanceOf(AccessDeniedException.class);
    }

    @WithAnonymousUser
    @ParameterizedTest
    @EnumSource(Operacao.class)
    void imagemContinuaExigindoAutenticacao(Operacao operacao) {
        assertThatThrownBy(() -> executar(operacao)).isInstanceOf(AccessDeniedException.class);
    }

    @WithMockUser(roles = "USER")
    @Test
    void listaGlobalDeJobsContinuaRestrita() {
        assertThatThrownBy(() -> controller.listarJobs()).isInstanceOf(AccessDeniedException.class);
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void adminPodeListarJobs() {
        assertThat(controller.listarJobs().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private ResponseEntity<?> executar(Operacao operacao) {
        return switch (operacao) {
            case UPLOAD -> controller.iniciarUpload(1L,
                    new MockMultipartFile("file", "prato1.png", "image/png", new byte[]{1, 2, 3}));
            case STATUS -> controller.consultarStatus(1L, "job-1");
            case REMOCAO -> controller.removerImagem(1L);
        };
    }

    enum Operacao {
        UPLOAD, STATUS, REMOCAO
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class Config {

        @Bean
        ProdutoImagemController produtoImagemController() {
            var iniciar = mock(IniciarUploadImagemProdutoPort.class);
            var consultar = mock(ConsultarUploadImagemProdutoPort.class);
            var listar = mock(ListarJobsUploadImagemProdutoPort.class);
            var job = new UploadJobDTO("job-1", UploadJobStatus.DONE, 1L, "produtos/1/foto.png", null);
            when(iniciar.iniciar(eq(1L), any())).thenReturn(job);
            when(consultar.consultar("job-1")).thenReturn(job);
            when(listar.listar()).thenReturn(List.of(job));
            return new ProdutoImagemController(iniciar, consultar, mock(RemoverImagemProdutoPort.class), listar);
        }
    }
}
