package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.infrastructure.constants.Constants;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarUsuarioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarUsuarioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarUsuarioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ExcluirUsuarioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerenciarBloqueioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.PrimeiroAcessoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ControleAcessoPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.AtualizarUsuarioRequestDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.BloqueiosRequestDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.BloqueiosResponseDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.RegisterDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.RoleOptionDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.UserRolesDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.UsuarioListagemDTO;
import com.fabriciosanches.fichatecnica.core.domain.enums.UserRole;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("ficha-tecnica/usuarios")
@Tag(name = "Usuários", description = "Administração de usuários, permissões e bloqueios")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private static final Logger logger = LogManager.getLogger(UsuarioController.class);

    private final CriarUsuarioPort criarUsuarioPort;
    private final ExcluirUsuarioPort excluirUsuarioPort;
    private final PrimeiroAcessoPort primeiroAcessoPort;
    private final GerenciarBloqueioPort gerenciarBloqueioPort;
    private final AtualizarUsuarioPort atualizarUsuarioPort;
    private final BuscarUsuarioPort buscarUsuarioPort;
    private final ControleAcessoPort controleAcessoPort;

    public UsuarioController(CriarUsuarioPort criarUsuarioPort,
                             ExcluirUsuarioPort excluirUsuarioPort,
                             PrimeiroAcessoPort primeiroAcessoPort,
                             GerenciarBloqueioPort gerenciarBloqueioPort,
                             AtualizarUsuarioPort atualizarUsuarioPort,
                             BuscarUsuarioPort buscarUsuarioPort,
                             ControleAcessoPort controleAcessoPort) {
        this.criarUsuarioPort = criarUsuarioPort;
        this.excluirUsuarioPort = excluirUsuarioPort;
        this.primeiroAcessoPort = primeiroAcessoPort;
        this.gerenciarBloqueioPort = gerenciarBloqueioPort;
        this.atualizarUsuarioPort = atualizarUsuarioPort;
        this.buscarUsuarioPort = buscarUsuarioPort;
        this.controleAcessoPort = controleAcessoPort;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/roles")
    @Operation(summary = "Lista roles disponíveis", description = "Retorna as roles do sistema para uso em cadastros e filtros.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles retornadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao listar roles")
    })
    public ResponseEntity<UserRolesDTO> listarRoles() {
        try {
            Map<UserRole, String> defaultLabels = Map.of(
                    UserRole.ADMIN, "Administrador",
                    UserRole.USER, "Usuário",
                    UserRole.SYSTEM, "Sistema"
            );

            List<RoleOptionDTO> options = Stream.of(UserRole.values())
                    .map(r -> new RoleOptionDTO(r.name(), defaultLabels.getOrDefault(r, r.name()), "role." + r.name()))
                    .sorted((a, b) -> a.label().compareToIgnoreCase(b.label()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new UserRolesDTO(options));
        } catch (Exception e) {
            logger.error("Erro ao listar roles", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/registrar-usuario")
    @Transactional
    @Operation(summary = "Registra usuário", description = "Cria um novo usuário no sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para registro")
    })
    public ResponseEntity<?> registrarUsuario(@RequestBody RegisterDTO dados) {
        try {
            criarUsuarioPort.registrarUsuario(dados);
            return ResponseEntity.ok().build();
        } catch (FichaTecnicaException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/excluir-usuario")
    @Transactional
    @Operation(summary = "Exclui usuário", description = "Remove um usuário do sistema pelo e-mail informado.")
    public ResponseEntity<?> excluirUsuario(@RequestBody BloqueiosRequestDTO dados) {
        try {
            excluirUsuarioPort.excluirUsuario(dados.email());
            return ResponseEntity.ok().build();
        } catch (FichaTecnicaException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/primeiro-acesso/{email}")
    @Transactional
    @Operation(summary = "Executa primeiro acesso", description = "Dispara o fluxo de primeiro acesso para o e-mail informado.")
    public ResponseEntity<?> primeiroAcesso(@PathVariable String email) {
        try {
            primeiroAcessoPort.primeiroAcesso(email);
            return ResponseEntity.ok().build();
        } catch (FichaTecnicaException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/resetar-senha")
    @Transactional
    @Operation(summary = "Reseta senha", description = "Expira a senha do usuário para forçar a redefinição no próximo login.")
    public ResponseEntity<?> resetarSenhaUsuario(@RequestBody BloqueiosRequestDTO dados) {
        try {
            controleAcessoPort.expirarSenha(dados.email());
            return ResponseEntity.ok().build();
        } catch (FichaTecnicaException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bloqueio-administrativo")
    @Transactional
    @Operation(summary = "Bloqueia usuário", description = "Executa o bloqueio administrativo de um usuário.")
    public ResponseEntity<BloqueiosResponseDTO> bloqueioAdministrativo(@RequestBody BloqueiosRequestDTO bloqueiosRequestDTO) {
        try {
            return ResponseEntity.ok(gerenciarBloqueioPort.bloquear(bloqueiosRequestDTO));
        } catch (FichaTecnicaException e) {
            return ResponseEntity.badRequest().body(new BloqueiosResponseDTO(Constants.MSG_ERRO_BLOQUEIO));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/desbloqueio-administrativo")
    @Transactional
    @Operation(summary = "Desbloqueia usuário", description = "Remove o bloqueio administrativo de um usuário.")
    public ResponseEntity<BloqueiosResponseDTO> desbloqueioAdministrativo(@RequestBody BloqueiosRequestDTO bloqueiosRequestDTO) {
        try {
            return ResponseEntity.ok(gerenciarBloqueioPort.desbloquear(bloqueiosRequestDTO));
        } catch (FichaTecnicaException e) {
            return ResponseEntity.badRequest().body(new BloqueiosResponseDTO(Constants.MSG_ERRO_BLOQUEIO));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/atualizar-usuario/{email}")
    @Transactional
    @Operation(summary = "Atualiza usuário", description = "Altera os dados de um usuário identificado pelo e-mail.")
    public ResponseEntity<UsuarioListagemDTO> atualizarUsuario(@PathVariable String email,
                                                               @RequestBody AtualizarUsuarioRequestDTO dados) {
        try {
            return ResponseEntity.ok(atualizarUsuarioPort.atualizarUsuario(email, dados));
        } catch (FichaTecnicaException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/listar-todos-usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista usuários", description = "Retorna todos os usuários cadastrados no sistema.")
    public ResponseEntity<List<UsuarioListagemDTO>> listarTodosUsuarios() {
        try {
            List<UsuarioListagemDTO> response = buscarUsuarioPort.listarTodosUsuarios();
            if (response.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(response);
        } catch (FichaTecnicaException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    @GetMapping("/buscar-usuario/{email}")
    @Operation(summary = "Busca usuário por e-mail", description = "Retorna os dados de um usuário específico a partir do e-mail.")
    public ResponseEntity<UsuarioListagemDTO> buscarUsuarioPorEmail(@PathVariable String email) {
        try {
            UsuarioListagemDTO usuarioListagemDTO = buscarUsuarioPort.buscarUsuarioPorEmail(email);
            if (usuarioListagemDTO == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(usuarioListagemDTO);
        } catch (FichaTecnicaException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}


