package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.infrastructure.constants.Constants;
import com.fabriciosanches.fichatecnica.core.domain.Seguranca;
import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarUsuarioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarUsuarioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarUsuarioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ExcluirUsuarioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerenciarBloqueioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.PrimeiroAcessoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.SegurancaRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UsuarioRepositoryPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.AtualizarUsuarioRequestDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.BloqueiosRequestDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.BloqueiosResponseDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.EnviarEmailPrimeiroAcessoRequestDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.RegisterDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.UsuarioListagemDTO;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.infrastructure.util.Utilidades;
import jakarta.mail.MessagingException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class UsuarioUseCase implements CriarUsuarioPort, BuscarUsuarioPort, AtualizarUsuarioPort,
        ExcluirUsuarioPort, PrimeiroAcessoPort, GerenciarBloqueioPort {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final SegurancaRepositoryPort segurancaRepositoryPort;
    private final SegurancaUseCase segurancaUseCase;

    public UsuarioUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                          SegurancaRepositoryPort segurancaRepositoryPort,
                          SegurancaUseCase segurancaUseCase) {
        this.usuarioRepositoryPort = Objects.requireNonNull(usuarioRepositoryPort, "UsuarioRepositoryPort não pode ser nulo");
        this.segurancaRepositoryPort = Objects.requireNonNull(segurancaRepositoryPort, "SegurancaRepositoryPort não pode ser nulo");
        this.segurancaUseCase = Objects.requireNonNull(segurancaUseCase, "SegurancaUseCase não pode ser nulo");
    }

    @Override
    public void registrarUsuario(RegisterDTO registerDTO) throws MessagingException {
        if (usuarioRepositoryPort.buscarPorLogin(registerDTO.login()).isPresent()) {
            throw new FichaTecnicaException("Usuário já existe com o login: " + registerDTO.login());
        }

        String senhaAleatoria = Utilidades.gerarSenhaAleatoria();
        String senhaGerada = Utilidades.encriptaSenha(senhaAleatoria);

        Usuario usuario = new Usuario(registerDTO.login(), senhaGerada, registerDTO.role(), registerDTO.nome());
        usuarioRepositoryPort.salvar(usuario);

        Seguranca seguranca = new Seguranca();
        seguranca.setEmail(registerDTO.login());
        seguranca.setDataCriacao(LocalDateTime.now());
        seguranca.setCpf(registerDTO.cpf());
        seguranca.setPrimeiro_acesso(Boolean.TRUE);
        seguranca.setBloqueado_admin(Boolean.FALSE);
        seguranca.setBloqueado_tentativas(Boolean.FALSE);
        seguranca.setBloqueado_expiracao(Boolean.FALSE);
        seguranca.setTentativas(5);
        seguranca.setDataExpiracaoSenha(LocalDateTime.now().plusHours(2));
        seguranca.setDataExpiracaoToken(null);
        seguranca.setTokenSeguranca(null);
        segurancaRepositoryPort.salvar(seguranca);

        EnviarEmailPrimeiroAcessoRequestDTO emailDTO =
                new EnviarEmailPrimeiroAcessoRequestDTO(registerDTO.login(), registerDTO.nome(), senhaAleatoria);
        segurancaUseCase.enviarEmailPrimeiroAcesso(emailDTO);
    }

    @Override
    public void excluirUsuario(String email) {
        Seguranca seguranca = buscarSegurancaOuFalhar(email);
        segurancaRepositoryPort.deletar(seguranca);

        Usuario usuario = usuarioRepositoryPort.buscarPorLogin(email)
                .orElseThrow(() -> new FichaTecnicaException("Usuário não encontrado"));
        usuarioRepositoryPort.deletar(usuario);
    }

    @Override
    public void primeiroAcesso(String email) throws MessagingException {
        Seguranca seguranca = buscarSegurancaOuFalhar(email);
        Usuario usuario = usuarioRepositoryPort.buscarPorLogin(email)
                .orElseThrow(() -> new FichaTecnicaException("Usuário não encontrado"));

        String nome = usuario.getNome();
        String cpfSemPontuacao = seguranca.getCpf() != null ? seguranca.getCpf().replaceAll("\\D", "") : null;

        if (cpfSemPontuacao == null || cpfSemPontuacao.isBlank()) {
            throw new FichaTecnicaException("CPF inválido para o usuário: " + email);
        }

        RegisterDTO registerDTO = new RegisterDTO(email, null, usuario.getRole(), nome, cpfSemPontuacao);
        excluirUsuario(email);
        registrarUsuario(registerDTO);
    }

    @Override
    public UsuarioListagemDTO atualizarUsuario(String email, AtualizarUsuarioRequestDTO dados) {
        Seguranca seguranca = segurancaRepositoryPort.buscarPorEmail(email)
                .orElseThrow(() -> new FichaTecnicaException("Usuário não encontrado: " + email));

        Usuario usuario = usuarioRepositoryPort.buscarPorLogin(email)
                .orElseThrow(() -> new FichaTecnicaException("Usuário não encontrado na tabela usuarios: " + email));

        if (dados.bloqueado_admin() != null) {
            seguranca.setBloqueado_admin(dados.bloqueado_admin());
        }
        if (dados.bloqueado_tentativas() != null) {
            seguranca.setBloqueado_tentativas(dados.bloqueado_tentativas());
        }
        if (dados.bloqueado_expiracao() != null) {
            seguranca.setBloqueado_expiracao(dados.bloqueado_expiracao());
        }
        if (dados.primeiro_acesso() != null) {
            seguranca.setPrimeiro_acesso(dados.primeiro_acesso());
        }
        segurancaRepositoryPort.salvar(seguranca);

        if (dados.nome() != null && !dados.nome().isBlank()) {
            usuario.setNome(dados.nome());
        }
        if (dados.role() != null) {
            usuario.setRole(dados.role());
        }
        usuarioRepositoryPort.salvar(usuario);

        return new UsuarioListagemDTO(seguranca, usuario);
    }

    @Override
    public UsuarioListagemDTO buscarUsuarioPorEmail(String email) {
        Seguranca seguranca = segurancaRepositoryPort.buscarPorEmail(email).orElse(null);
        if (seguranca == null) {
            return null;
        }

        Usuario usuario = usuarioRepositoryPort.buscarPorLogin(email).orElse(null);
        return new UsuarioListagemDTO(seguranca, usuario);
    }

    @Override
    public List<UsuarioListagemDTO> listarTodosUsuarios() {
        List<Seguranca> dadosSeguranca = segurancaRepositoryPort.buscarTodos();
        if (dadosSeguranca.isEmpty()) {
            return List.of();
        }

        return dadosSeguranca.stream()
                .map(seguranca -> new UsuarioListagemDTO(seguranca,
                        usuarioRepositoryPort.buscarPorLogin(seguranca.getEmail()).orElse(null)))
                .toList();
    }

    @Override
    public BloqueiosResponseDTO bloquear(BloqueiosRequestDTO bloqueiosRequestDTO) {
        Seguranca seguranca = segurancaRepositoryPort.buscarPorEmail(bloqueiosRequestDTO.email()).orElse(null);
        if (seguranca == null) {
            return new BloqueiosResponseDTO(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS);
        }

        if (seguranca.getBloqueado_admin()) {
            return new BloqueiosResponseDTO(Constants.MSG_BLOQUEIO_ADM_JA_SETADO);
        }

        seguranca.setBloqueado_admin(Boolean.TRUE);
        segurancaRepositoryPort.salvar(seguranca);
        return new BloqueiosResponseDTO(Constants.MSG_BLOQUEIO_ADM_SETADO);
    }

    @Override
    public BloqueiosResponseDTO desbloquear(BloqueiosRequestDTO bloqueiosRequestDTO) {
        Seguranca seguranca = segurancaRepositoryPort.buscarPorEmail(bloqueiosRequestDTO.email()).orElse(null);
        if (seguranca == null) {
            return new BloqueiosResponseDTO(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS);
        }

        if (!seguranca.getBloqueado_admin()) {
            return new BloqueiosResponseDTO(Constants.MSG_BLOQUEIO_ADM_DESATIVADO);
        }

        seguranca.setBloqueado_admin(Boolean.FALSE);
        segurancaRepositoryPort.salvar(seguranca);
        return new BloqueiosResponseDTO(Constants.MSG_BLOQUEIO_ADM_DESATIVADO);
    }

    public void expirarSenha(String email) {
        Seguranca seguranca = buscarSegurancaOuFalhar(email);
        seguranca.setDataExpiracaoSenha(LocalDateTime.now().minusDays(1));
        seguranca.setBloqueado_expiracao(Boolean.TRUE);
        segurancaRepositoryPort.salvar(seguranca);
    }

    private Seguranca buscarSegurancaOuFalhar(String email) {
        return segurancaRepositoryPort.buscarPorEmail(email)
                .orElseThrow(() -> new FichaTecnicaException(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS));
    }
}


