package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.infrastructure.constants.Constants;
import com.fabriciosanches.fichatecnica.core.domain.Seguranca;
import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.in.ControleAcessoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RecuperacaoSenhaPort;
import com.fabriciosanches.fichatecnica.core.ports.out.EnviarEmailPort;
import com.fabriciosanches.fichatecnica.core.ports.out.SegurancaRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UsuarioRepositoryPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.EnviarEmailPrimeiroAcessoRequestDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.EnviarEmailSegurancaResponseDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.SegurancaDTO;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.infrastructure.util.Utilidades;
import jakarta.mail.MessagingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;

public class SegurancaUseCase implements RecuperacaoSenhaPort, ControleAcessoPort {

    private static final Logger logger = LogManager.getLogger(SegurancaUseCase.class);
    private final SegurancaRepositoryPort segurancaRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final EnviarEmailPort enviarEmailPort;

    public SegurancaUseCase(SegurancaRepositoryPort segurancaRepositoryPort,
                            UsuarioRepositoryPort usuarioRepositoryPort,
                            EnviarEmailPort enviarEmailPort) {
        this.segurancaRepositoryPort = Objects.requireNonNull(segurancaRepositoryPort, "SegurancaRepositoryPort nÃ£o pode ser nulo");
        this.usuarioRepositoryPort = Objects.requireNonNull(usuarioRepositoryPort, "UsuarioRepositoryPort nÃ£o pode ser nulo");
        this.enviarEmailPort = Objects.requireNonNull(enviarEmailPort, "EnviarEmailPort nÃ£o pode ser nulo");
    }

    @Override
    public EnviarEmailSegurancaResponseDTO enviarEmailSeguranca(String email) throws MessagingException {
        String cpf = segurancaRepositoryPort.buscarCpfPorEmail(email);
        if (cpf == null) {
            throw new FichaTecnicaException("Email nÃ£o encontrado");
        }

        if (!Utilidades.validarCPF(cpf)) {
            throw new FichaTecnicaException("CPF invÃ¡lido");
        }

        Seguranca seguranca = buscarSegurancaPorEmailOuFalhar(email);
        String token = gerarTokenSeguranca();
        seguranca.setTokenSeguranca(token);
        seguranca.setDataExpiracaoToken(LocalDateTime.now().plusHours(24));
        segurancaRepositoryPort.salvar(seguranca);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataExpiracaoTokenFormatada = seguranca.getDataExpiracaoToken().format(formatter);

        SegurancaDTO segurancaDTO = new SegurancaDTO(seguranca);
        String corpo = "Token de seguranÃ§a: " + token + "\n"
                + "Validade: " + dataExpiracaoTokenFormatada + "\n"
                + "Se vocÃª nÃ£o solicitou, ignore este email.";
        enviarEmailPort.enviar(segurancaDTO.email(), Constants.SUBJECT_EMAIL_RECUPERACAO_SENHA, corpo);

        return new EnviarEmailSegurancaResponseDTO(segurancaDTO, dataExpiracaoTokenFormatada);
    }

    @Override
    public void enviarEmailPrimeiroAcesso(EnviarEmailPrimeiroAcessoRequestDTO dados) throws MessagingException {
        String corpo = "OlÃ¡, " + dados.nomeUsuario() + "!\n"
                + "Sua senha temporÃ¡ria Ã©: " + dados.senhaAleatoria() + "\n"
                + "Altere a senha no primeiro acesso.";
        enviarEmailPort.enviar(dados.email(), Constants.SUBJECT_EMAIL_PRIMEIRO_ACESSO, corpo);
    }

    @Override
    public void trocarSenhaSeguranca(String email, String cpf, String tokenSeguranca, String senha, String confirmacaoSenha) {
        Seguranca seguranca = buscarSegurancaPorEmailOuFalhar(email);

        if (!seguranca.getCpf().equals(cpf)) {
            throw new FichaTecnicaException("CPF nÃ£o corresponde ao email");
        }
        if (!validarTokenSeguranca(seguranca, tokenSeguranca)) {
            throw new FichaTecnicaException("Token de seguranÃ§a invÃ¡lido ou expirado");
        }
        if (!validarSenha(senha, confirmacaoSenha)) {
            throw new FichaTecnicaException("Senhas nÃ£o coincidem ou sÃ£o invÃ¡lidas");
        }

        seguranca.setTokenSeguranca(null);
        seguranca.setDataExpiracaoToken(null);
        seguranca.setDataExpiracaoSenha(LocalDateTime.now().plusDays(90));
        seguranca.setPrimeiro_acesso(Boolean.FALSE);
        seguranca.setBloqueado_admin(Boolean.FALSE);
        seguranca.setBloqueado_tentativas(Boolean.FALSE);
        seguranca.setBloqueado_expiracao(Boolean.FALSE);
        seguranca.setTentativas(5);
        segurancaRepositoryPort.salvar(seguranca);

        String senhaNormal = Utilidades.decodeFromBase64(senha);
        String senhaCriptografada = Utilidades.encriptaSenha(senhaNormal);

        Usuario usuario = usuarioRepositoryPort.buscarPorLogin(email)
                .orElseThrow(() -> new FichaTecnicaException("UsuÃ¡rio nÃ£o encontrado"));
        usuario.setSenha(senhaCriptografada);
        usuarioRepositoryPort.salvar(usuario);
    }

    @Override
    public void validarAcesso(String email) {
        Seguranca seguranca = buscarSegurancaPorEmailOuFalhar(email);
        validarSenhaExpirada(seguranca.getEmail());

        if (seguranca.getBloqueado_admin()) {
            throw new FichaTecnicaException(Constants.MSG_ERRO_BLOQUEIO_ADM);
        } else if (seguranca.getPrimeiro_acesso()) {
            throw new FichaTecnicaException(Constants.MSG_ERRO_BLOQUEIO_PRIMEIRO_ACESSO);
        } else if (seguranca.getBloqueado_expiracao()) {
            throw new FichaTecnicaException(Constants.MSG_ERRO_BLOQUEIO_EXPIRACAO);
        } else if (seguranca.getBloqueado_tentativas()) {
            throw new FichaTecnicaException(Constants.MSG_ERRO_BLOQUEIO_TENTATIVAS);
        }
    }

    @Override
    public void errouSenha(String email) {
        Seguranca seguranca = buscarSegurancaPorEmailOuFalhar(email);
        seguranca.setTentativas(seguranca.getTentativas() - 1);

        if (seguranca.getTentativas() <= 0) {
            seguranca.setBloqueado_tentativas(Boolean.TRUE);
            seguranca.setBloqueado_admin(Boolean.FALSE);
            seguranca.setBloqueado_expiracao(Boolean.FALSE);
            seguranca.setPrimeiro_acesso(Boolean.FALSE);
            segurancaRepositoryPort.salvar(seguranca);
            throw new FichaTecnicaException("UsuÃ¡rio bloqueado por tentativas excedidas");
        }

        segurancaRepositoryPort.salvar(seguranca);
    }

    @Override
    public void resetarTentativas(String email) {
        Seguranca seguranca = buscarSegurancaPorEmailOuFalhar(email);
        seguranca.setTentativas(5);
        segurancaRepositoryPort.salvar(seguranca);
    }

    @Override
    public void expirarSenha(String email) {
        Seguranca seguranca = buscarSegurancaPorEmailOuFalhar(email);
        seguranca.setDataExpiracaoSenha(LocalDateTime.now().minusDays(1));
        seguranca.setBloqueado_expiracao(Boolean.TRUE);
        segurancaRepositoryPort.salvar(seguranca);
    }

    private void validarSenhaExpirada(String email) {
        Seguranca seguranca = buscarSegurancaPorEmailOuFalhar(email);
        if (seguranca.getDataExpiracaoSenha() != null && LocalDateTime.now().isAfter(seguranca.getDataExpiracaoSenha())) {
            expirarSenha(seguranca.getEmail());
        }
        logger.info("Senha valida para o email: {}", email);
    }

    private Seguranca buscarSegurancaPorEmailOuFalhar(String email) {
        return segurancaRepositoryPort.buscarPorEmail(email)
                .orElseThrow(() -> new FichaTecnicaException(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS));
    }

    private Boolean validarTokenSeguranca(Seguranca seguranca, String token) {
        return seguranca != null
                && seguranca.getTokenSeguranca() != null
                && seguranca.getTokenSeguranca().equals(token)
                && LocalDateTime.now().isBefore(seguranca.getDataExpiracaoToken());
    }

    private Boolean validarSenha(String senha, String confirmacaoSenha) {
        return senha != null && !senha.isBlank() && senha.equals(confirmacaoSenha);
    }

    private String gerarTokenSeguranca() {
        Random random = new Random();
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            token.append(random.nextInt(10));
        }
        return token.toString();
    }
}


