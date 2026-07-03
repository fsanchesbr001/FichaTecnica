package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.controllers.ProdutoImagemController;
import com.fabriciosanches.fichatecnica.dtos.UploadJobDTO;
import com.fabriciosanches.fichatecnica.enums.UploadJobStatus;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

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

class ProdutoImagemControllerTest {

    private MockMvc mockMvc;
    private ProdutoImagemUploadService uploadService;

    @BeforeEach
    void setUp() {
        uploadService = Mockito.mock(ProdutoImagemUploadService.class);
        ProdutoImagemController controller = new ProdutoImagemController(uploadService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void iniciarUpload_DeveRetornarAccepted() throws Exception {
        UploadJobDTO job = new UploadJobDTO("job-1", UploadJobStatus.PENDING, 4L, null, null);
        when(uploadService.iniciarUpload(eq(4L), any())).thenReturn(job);

        MockMultipartFile file = new MockMultipartFile("file", "imagem.png", "image/png", "abc".getBytes());

        mockMvc.perform(multipart("/ficha-tecnica/produtos/{id}/imagem/upload", 4L)
                        .file(file)
                        .contentType(MULTIPART_FORM_DATA))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("job-1"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void iniciarUpload_DeveRetornarBadRequestQuandoRegraNegocioFalhar() throws Exception {
        when(uploadService.iniciarUpload(eq(4L), any())).thenThrow(new FichaTecnicaException("arquivo invalido"));

        MockMultipartFile file = new MockMultipartFile("file", "imagem.png", "image/png", "abc".getBytes());

        mockMvc.perform(multipart("/ficha-tecnica/produtos/{id}/imagem/upload", 4L)
                        .file(file)
                        .contentType(MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("arquivo invalido"));
    }

    @Test
    void consultarStatus_DeveRetornarConflictQuandoJobNaoPertencerAoProduto() throws Exception {
        UploadJobDTO job = new UploadJobDTO("job-1", UploadJobStatus.DONE, 99L, "url", null);
        when(uploadService.consultarStatus("job-1")).thenReturn(job);

        mockMvc.perform(get("/ficha-tecnica/produtos/{id}/imagem/status/{jobId}", 4L, "job-1"))
                .andExpect(status().isConflict());
    }

    @Test
    void consultarStatus_DeveRetornarNotFoundQuandoJobNaoExistir() throws Exception {
        when(uploadService.consultarStatus("job-404")).thenThrow(new FichaTecnicaException("nao encontrado"));

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
        doThrow(new FichaTecnicaException("produto nao encontrado")).when(uploadService).removerImagem(4L);

        mockMvc.perform(delete("/ficha-tecnica/produtos/{id}/imagem", 4L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("produto nao encontrado"));
    }

    @Test
    void listarJobs_DeveRetornarListaDeJobs() throws Exception {
        when(uploadService.listarJobsAtivos())
                .thenReturn(List.of(new UploadJobDTO("job-1", UploadJobStatus.DONE, 4L, "url", null)));

        mockMvc.perform(get("/ficha-tecnica/produtos/imagem/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobId").value("job-1"));
    }
}

