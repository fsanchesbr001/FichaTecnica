package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarHistoricoItemPort;
import com.fabriciosanches.fichatecnica.dtos.GraficoPrecoItemDTO;
import com.fabriciosanches.fichatecnica.dtos.ItemDTO;
import com.fabriciosanches.fichatecnica.dtos.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.enums.ImagemPosicao;
import com.fabriciosanches.fichatecnica.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.enums.TipoRelatorio;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.GraficoService;
import com.fabriciosanches.fichatecnica.services.RelatorioService;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("ficha-tecnica")
@Tag(name = "Itens", description = "Cadastro, consulta, atualização, exclusão e relatórios de itens")
@SecurityRequirement(name = "bearerAuth")
public class ItemController {

    private static final Logger logger = LogManager.getLogger(ItemController.class);

    private final BuscarItemPort buscarItemPort;
    private final CriarItemPort criarItemPort;
    private final AtualizarItemPort atualizarItemPort;
    private final DeletarItemPort deletarItemPort;
    private final ListarHistoricoItemPort listarHistoricoItemPort;
    private final RelatorioService relatorioService;
    private final GraficoService graficoService;

    public ItemController(
            BuscarItemPort buscarItemPort,
            CriarItemPort criarItemPort,
            AtualizarItemPort atualizarItemPort,
            DeletarItemPort deletarItemPort,
            ListarHistoricoItemPort listarHistoricoItemPort,
            RelatorioService relatorioService,
            GraficoService graficoService) {
        this.buscarItemPort = buscarItemPort;
        this.criarItemPort = criarItemPort;
        this.atualizarItemPort = atualizarItemPort;
        this.deletarItemPort = deletarItemPort;
        this.listarHistoricoItemPort = listarHistoricoItemPort;
        this.relatorioService = relatorioService;
        this.graficoService = graficoService;
    }

    @GetMapping("/itens")
    @Operation(summary = "Lista itens", description = "Retorna todos os itens cadastrados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhum item encontrado")
    })
    public ResponseEntity<List<ItemDTO>> buscarLista() {
        try {
            List<ItemDTO> itens = buscarItemPort.listar().stream().map(this::toDto).toList();
            if (itens.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(itens);
        } catch (FichaTecnicaException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/itens/{id}")
    @Operation(summary = "Busca item por ID", description = "Retorna os dados de um item específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "Erro ao buscar item")
    })
    public ResponseEntity<ItemDTO> buscarPorId(@PathVariable Long id) {
        try {
            Item item = buscarItemPort.buscarPorId(id);
            return ResponseEntity.ok(toDto(item));
        } catch (FichaTecnicaException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/itens/{id}")
    @Transactional
    @Operation(summary = "Remove item", description = "Exclui um item existente pelo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item removido com sucesso"),
            @ApiResponse(responseCode = "422", description = "Existem históricos vinculados ao item"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    public ResponseEntity<Void> apagar(@PathVariable Long id) {
        try {
            deletarItemPort.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (FichaTecnicaException e) {
            return ResponseEntity.unprocessableEntity().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/itens/{id}")
    @Transactional
    @Operation(summary = "Atualiza item", description = "Altera os dados de um item existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    public ResponseEntity<ItemDTO> atualizarItem(@PathVariable Long id, @RequestBody ItemDTO itemDTO) {
        try {
            Item item = atualizarItemPort.atualizar(id, itemDTO.nome(), itemDTO.unidadeMedida(), itemDTO.valor());
            return ResponseEntity.ok(toDto(item));
        } catch (FichaTecnicaException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/itens")
    @Transactional
    @Operation(summary = "Cadastra item", description = "Cria um novo item na base de dados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para cadastro")
    })
    public ResponseEntity<ItemDTO> cadastrarItem(@RequestBody ItemDTO itemDTO) {
        try {
            Item item = criarItemPort.criar(itemDTO.nome(), itemDTO.unidadeMedida(), itemDTO.valor());
            return ResponseEntity.ok(toDto(item));
        } catch (FichaTecnicaException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/itens/gerar-pdf-lista")
    @Operation(summary = "Gera PDF da lista de itens", description = "Exporta a lista completa de itens em PDF.")
    public ResponseEntity<byte[]> gerarPdfLista() {
        logger.info("Início do método gerarPdfLista – ItemController");
        try {
            List<ItemDTO> lista = buscarItemPort.listar().stream().map(this::toDto).toList();
            if (lista.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            String jsonData = new Gson().toJson(lista);
            Map<String, String> colunas = new LinkedHashMap<>();
            colunas.put("nome", "Nome");
            colunas.put("unidadeMedida", "Unidade de Medida");
            colunas.put("valor", "Valor");

            RelatorioRequestDTO request = new RelatorioRequestDTO(
                    jsonData,
                    "",
                    "Lista de Itens",
                    colunas,
                    TipoRelatorio.LISTA,
                    OrientacaoRelatorio.RETRATO,
                    true
            );

            byte[] pdfBytes = relatorioService.gerarRelatorioPDF(request);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = "Lista-Itens-" + timestamp + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar PDF de lista de Itens", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/itens/gerar-pdf-detalhe/{id:[0-9]+}")
    @Operation(summary = "Gera PDF detalhado do item", description = "Exporta a ficha detalhada de um item específico em PDF.")
    public ResponseEntity<byte[]> gerarPdfDetalhe(@PathVariable Long id) {
        try {
            Item item = buscarItemPort.buscarPorId(id);
            String jsonData = new Gson().toJson(List.of(toDto(item)));

            Map<String, String> colunas = new LinkedHashMap<>();
            colunas.put("nome", "Nome");
            colunas.put("unidadeMedida", "Unidade de Medida");
            colunas.put("valor", "Valor");

            byte[] graficoPng = null;
            try {
                GraficoPrecoItemDTO graficoDTO = listarHistoricoItemPort.gerarGraficoPreco(id);
                if (graficoDTO != null && !graficoDTO.labels().isEmpty()) {
                    graficoPng = graficoService.gerarGraficoPNG(graficoDTO);
                }
            } catch (FichaTecnicaException ex) {
                logger.info("Sem histórico de preços para o item id={} – PDF será gerado sem gráfico", id);
            }

            RelatorioRequestDTO request;
            if (graficoPng != null) {
                request = new RelatorioRequestDTO(
                        jsonData,
                        "",
                        "Detalhe do Item",
                        colunas,
                        TipoRelatorio.DETALHE,
                        OrientacaoRelatorio.PAISAGEM,
                        false,
                        true,
                        graficoPng,
                        ImagemPosicao.FIM
                );
            } else {
                request = new RelatorioRequestDTO(
                        jsonData,
                        "",
                        "Detalhe do Item",
                        colunas,
                        TipoRelatorio.DETALHE,
                        OrientacaoRelatorio.PAISAGEM,
                        false
                );
            }

            byte[] pdfBytes = relatorioService.gerarRelatorioPDF(request);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = "Detalhe-Item-" + id + "-" + timestamp + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (FichaTecnicaException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ItemDTO toDto(Item item) {
        return new ItemDTO(item.getCodigo(), item.getNome(), item.getUnidadeMedida(), item.getValor());
    }
}

