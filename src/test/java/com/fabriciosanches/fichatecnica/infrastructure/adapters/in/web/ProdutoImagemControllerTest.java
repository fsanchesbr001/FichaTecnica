package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.ArquivoUpload;
import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.usecase.ProdutoImagemUploadUseCase;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.ProdutoImagemStorageAdapter;
import com.fabriciosanches.fichatecnica.infrastructure.config.WebConfig;
import com.fabriciosanches.fichatecnica.core.ports.in.ConsultarUploadImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.IniciarUploadImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarJobsUploadImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RemoverImagemProdutoPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.UploadJobDTO;
import com.fabriciosanches.fichatecnica.core.domain.enums.UploadJobStatus;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

class ProdutoImagemControllerTest {

    private MockMvc mockMvc;
    private IniciarUploadImagemProdutoPort iniciarUploadImagemProdutoPort;
    private ConsultarUploadImagemProdutoPort consultarUploadImagemProdutoPort;
    private RemoverImagemProdutoPort removerImagemProdutoPort;
    private ListarJobsUploadImagemProdutoPort listarJobsUploadImagemProdutoPort;

    @BeforeEach
    void setUp() {
        iniciarUploadImagemProdutoPort = Mockito.mock(IniciarUploadImagemProdutoPort.class);
        consultarUploadImagemProdutoPort = Mockito.mock(ConsultarUploadImagemProdutoPort.class);
        removerImagemProdutoPort = Mockito.mock(RemoverImagemProdutoPort.class);
        listarJobsUploadImagemProdutoPort = Mockito.mock(ListarJobsUploadImagemProdutoPort.class);
        ProdutoImagemController controller = new ProdutoImagemController(
                iniciarUploadImagemProdutoPort,
                consultarUploadImagemProdutoPort,
                removerImagemProdutoPort,
                listarJobsUploadImagemProdutoPort);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void iniciarUpload_DeveRetornarAccepted() throws Exception {
        UploadJobDTO job = new UploadJobDTO("job-1", UploadJobStatus.PENDING, 4L, null, null);
        when(iniciarUploadImagemProdutoPort.iniciar(eq(4L), any(ArquivoUpload.class))).thenReturn(job);

        MockMultipartFile file = new MockMultipartFile("file", "imagem.png", "image/png", "abc".getBytes());

        mockMvc.perform(multipart("/ficha-tecnica/produtos/{id}/imagem/upload", 4L)
                        .file(file)
                        .contentType(MULTIPART_FORM_DATA))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("job-1"));
    }

    @Test
    void iniciarUpload_DeveRetornarBadRequestQuandoFalhar() throws Exception {
        when(iniciarUploadImagemProdutoPort.iniciar(eq(4L), any(ArquivoUpload.class))).thenThrow(new FichaTecnicaException("arquivo invalido"));

        MockMultipartFile file = new MockMultipartFile("file", "imagem.png", "image/png", "abc".getBytes());

        mockMvc.perform(multipart("/ficha-tecnica/produtos/{id}/imagem/upload", 4L)
                        .file(file)
                        .contentType(MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("arquivo invalido"));
    }

    @Test
    void iniciarUpload_DeveRetornarErroQuandoJobJaFalhou() throws Exception {
        UploadJobDTO job = new UploadJobDTO("job-erro", UploadJobStatus.ERROR, 4L, null,
                "Erro ao salvar imagem do produto.");
        when(iniciarUploadImagemProdutoPort.iniciar(eq(4L), any(ArquivoUpload.class))).thenReturn(job);

        mockMvc.perform(multipart("/ficha-tecnica/produtos/{id}/imagem/upload", 4L)
                        .file(new MockMultipartFile("file", "imagem.png", "image/png", "abc".getBytes())))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.jobId").value("job-erro"))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Erro ao salvar imagem do produto."));
    }

    @Test
    void userPodeEnviarPngEConsultarArquivoPelaUrlPublica(@TempDir Path diretorio) throws Exception {
        var anterior = SecurityContextHolder.getContext();
        var autenticacao = SecurityContextHolder.createEmptyContext();
        autenticacao.setAuthentication(UsernamePasswordAuthenticationToken.authenticated("teste", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        SecurityContextHolder.setContext(autenticacao);
        try (var context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("imagens",
                    Map.of("app.storage.image-upload-dir", diretorio.toString())));
            context.register(FluxoImagemConfig.class);
            context.refresh();
            var mvc = MockMvcBuilders.webAppContextSetup(context).build();
            var buffer = new ByteArrayOutputStream();
            ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "png", buffer);
            byte[] png = buffer.toByteArray();

            var resposta = mvc.perform(multipart("/ficha-tecnica/produtos/4/imagem/upload")
                            .file(new MockMultipartFile("file", "prato1.png", "image/png", png)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.status").value("DONE"))
                    .andExpect(jsonPath("$.imagemUrl", startsWith("produtos/4/")))
                    .andReturn();
            String caminho = new ObjectMapper().readTree(resposta.getResponse().getContentAsString())
                    .get("imagemUrl").asText();

            mvc.perform(get("/api/imagens/" + caminho))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("image/png"))
                    .andExpect(content().bytes(png));
            assertEquals(caminho, context.getBean(ProdutoRepositoryPort.class).buscarPorId(4L).orElseThrow().getImagem());
        } finally {
            SecurityContextHolder.setContext(anterior);
        }
    }

    @Test
    void consultarStatus_DeveRetornarConflictQuandoJobNaoPertencerAoProduto() throws Exception {
        UploadJobDTO job = new UploadJobDTO("job-1", UploadJobStatus.DONE, 99L, "url", null);
        when(consultarUploadImagemProdutoPort.consultar("job-1")).thenReturn(job);

        mockMvc.perform(get("/ficha-tecnica/produtos/{id}/imagem/status/{jobId}", 4L, "job-1"))
                .andExpect(status().isConflict());
    }

    @Test
    void consultarStatus_DeveRetornarNotFoundQuandoJobNaoExistir() throws Exception {
        when(consultarUploadImagemProdutoPort.consultar("job-404")).thenThrow(new FichaTecnicaException("nao encontrado"));

        mockMvc.perform(get("/ficha-tecnica/produtos/{id}/imagem/status/{jobId}", 4L, "job-404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("nao encontrado"));
    }

    @Test
    void removerImagem_DeveRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/ficha-tecnica/produtos/{id}/imagem", 4L))
                .andExpect(status().isNoContent());
    }

    @Test
    void removerImagem_DeveRetornarNotFoundQuandoProdutoNaoExistir() throws Exception {
        doThrow(new FichaTecnicaException("produto nao encontrado")).when(removerImagemProdutoPort).remover(4L);

        mockMvc.perform(delete("/ficha-tecnica/produtos/{id}/imagem", 4L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("produto nao encontrado"));
    }

    @Test
    void listarJobs_DeveRetornarListaDeJobs() throws Exception {
        when(listarJobsUploadImagemProdutoPort.listar())
                .thenReturn(List.of(new UploadJobDTO("job-1", UploadJobStatus.DONE, 4L, "url", null)));

        mockMvc.perform(get("/ficha-tecnica/produtos/imagem/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobId").value("job-1"));
    }

    @TestConfiguration
    @EnableWebMvc
    @EnableMethodSecurity
    @Import(WebConfig.class)
    static class FluxoImagemConfig {

        @Bean
        ProdutoRepositoryPort produtoImagemFluxoRepository() {
            var repository = Mockito.mock(ProdutoRepositoryPort.class);
            var produto = new Produto(4L, "Bolo", "Desc", null, BigDecimal.TEN, BigDecimal.ZERO, List.of());
            when(repository.buscarPorId(4L)).thenReturn(Optional.of(produto));
            when(repository.salvar(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            return repository;
        }

        @Bean
        ProdutoImagemController produtoImagemFluxoController(
                Environment environment,
                @Qualifier("produtoImagemFluxoRepository") ProdutoRepositoryPort repository) {
            var storage = new ProdutoImagemStorageAdapter();
            ReflectionTestUtils.setField(storage, "storagePath",
                    environment.getRequiredProperty("app.storage.image-upload-dir"));
            var upload = new ProdutoImagemUploadUseCase(repository, storage);
            return new ProdutoImagemController(upload, upload, upload, upload);
        }
    }
}


