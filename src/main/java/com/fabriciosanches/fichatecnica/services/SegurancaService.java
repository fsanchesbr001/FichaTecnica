package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.constants.Constants;
import com.fabriciosanches.fichatecnica.domains.Seguranca;
import com.fabriciosanches.fichatecnica.domains.Usuario;
import com.fabriciosanches.fichatecnica.dtos.*;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.mail.EmailService;
import com.fabriciosanches.fichatecnica.repository.SegurancaRepository;
import com.fabriciosanches.fichatecnica.repository.UsuarioRepository;
import com.fabriciosanches.fichatecnica.util.Utilidades;
import jakarta.mail.MessagingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class SegurancaService {

    private final Logger logger = LogManager.getLogger(SegurancaService.class);
    private final SegurancaRepository repository;
    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;
    private final SegurancaRepository segurancaRepository;

    public SegurancaService(SegurancaRepository repository, EmailService emailService, UsuarioRepository usuarioRepository, SegurancaRepository segurancaRepository) {
        this.repository = repository;
        this.emailService = emailService;
        this.usuarioRepository = usuarioRepository;
        this.segurancaRepository = segurancaRepository;
    }

    private String findCPFByEmail(String email) {
        logger.info("Buscando CPF por email: {}", email);
        String cpf = repository.findCPFByEmail(email);
        if (cpf == null) {
            logger.info("CPF não encontrado para o email: {}", email);
            return null;
        }
        logger.info("CPF encontrado: {}", cpf);
        return cpf;
    }

    public EnviarEmailResponseDTO enviarEmailSeguranca(String email) throws MessagingException {
        logger.info("Preparando email de segurança para: {}", email);
        String cpf = findCPFByEmail(email);
        if (cpf == null) {
            throw new FichaTecnicaException("Email não encontrado");
        }

        if(!Utilidades.validarCPF(cpf)){
            throw new FichaTecnicaException("CPF inválido");
        }

        String token = gerarTokenSeguranca();

        Seguranca seguranca = repository.findByEmail(email);
        seguranca.setTokenSeguranca(token);
        seguranca.setDataExpiracaoToken(LocalDateTime.now().plusMinutes(90));
        logger.info("Token de segurança gerado: {}", token);
        repository.save(seguranca);

        SegurancaDTO segurancaDTO = new SegurancaDTO(seguranca);
        logger.info("Dados de segurança preparados: {}", segurancaDTO);


        // Formatar as datas
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataExpiracaoTokenFormatada = seguranca.getDataExpiracaoToken().format(formatter);

        EnviarEmailResponseDTO enviarEmailResponseDTO = new EnviarEmailResponseDTO(segurancaDTO, dataExpiracaoTokenFormatada);

        logger.info("Enviando email de segurança ");
        emailService.sendEmail(enviarEmailResponseDTO);
        logger.info("Email enviado com sucesso");

        return enviarEmailResponseDTO;
    }

    private  String gerarTokenSeguranca() {
        Random random = new Random();
        StringBuilder token = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int numero = random.nextInt(10); // Gera um número entre 0 e 9
            token.append(numero);
        }

        return token.toString();

    }

    private Seguranca findByEmail(String email) {
        logger.info("Buscando dados de segurança por email: {}", email);
        Seguranca seguranca = repository.findByEmail(email);
        if (seguranca == null) {
            logger.info("Dados de segurança não encontrada para o email: {}", email);
            return null;
        }
        logger.info("Dados de segurança encontrada: {}", seguranca);
        return seguranca;
    }

    private Boolean validarTokenSeguranca(Seguranca seguranca, String token) {
        logger.info("Validando token de segurança: {}", token);
        if (seguranca == null || seguranca.getTokenSeguranca() == null) {
            logger.warn("Dados de segurança ou token não encontrados");
            return false;
        }
        boolean isValid = seguranca.getTokenSeguranca().equals(token)
                && LocalDateTime.now().isBefore(seguranca.getDataExpiracaoToken());
        logger.info("Token de segurança válido: {}", isValid);
        return isValid;
    }

    private Boolean validarSenha(String senha, String confirmacaoSenha) {
        logger.info("Validando senha e confirmação de senha");
        boolean isValid = senha != null && !senha.isBlank() && senha.equals(confirmacaoSenha);
        logger.info("Senhas válidas: {}", isValid);
        return isValid;
    }

    private String gerarSenhaSeguranca(String senhaNormal) {
        logger.info("Gerando senha de segurança");
        String senha = Utilidades.geraSenha(senhaNormal); // Exemplo de senha segura
        logger.info("Senha de segurança gerada: {}", senha);
        return senha;
    }

    public Boolean trocarSenhaSeguranca(String email, String cpf, String tokenSeguranca, String senha, String confirmacaoSenha) {
        logger.info("Iniciando processo de troca de senha de segurança");
        Seguranca seguranca = findByEmail(email);

        if (seguranca == null) {
            logger.warn("Dados de segurança não encontrados para o email: {}", email);
            throw new FichaTecnicaException("Dados de segurança não encontrados");
        }

        if (!validarTokenSeguranca(seguranca, tokenSeguranca)) {
            logger.warn("Token de segurança inválido ou expirado");
            throw new FichaTecnicaException("Token de segurança inválido ou expirado");
        }

        if (!validarSenha(senha, confirmacaoSenha)) {
            logger.warn("Senhas não coincidem ou são inválidas");
            throw new FichaTecnicaException("Senhas não coincidem ou são inválidas");
        }

        seguranca.setTokenSeguranca(null); // Limpa o token após a troca de senha
        seguranca.setDataExpiracaoToken(null); // Limpa a data de expiração do token
        seguranca.setDataExpiracaoSenha(LocalDateTime.now().plusDays(90)); // Define nova data de expiração da senha
        seguranca.setPrimeiro_acesso(Boolean.FALSE); // Marca como não é o primeiro acesso
        seguranca.setBloqueado_admin(Boolean.FALSE);
        seguranca.setBloqueado_tentativas(Boolean.FALSE);
        seguranca.setBloqueado_expiracao(Boolean.FALSE);
        seguranca.setTentativas(5); // Define a nova senha criptografada
        repository.save(seguranca);

        String senhaNormmal = Utilidades.decodeFromBase64(senha);

        String senhaCriptografada = gerarSenhaSeguranca(senhaNormmal);

        Usuario usuario = usuarioRepository.findByLoginUsuario(email);
        if (usuario == null) {
            logger.warn("Usuário não encontrado para o email: {}", email);
            throw new FichaTecnicaException("Usuário não encontrado");
        }
        usuario.setSenha(senhaCriptografada);
        usuarioRepository.save(usuario);

        logger.info("Senha de segurança trocada com sucesso para o email: {}", email);
        return true;
    }

    public BloqueiosResponseDTO primeiroAcessoSeguranca(BloqueiosRequestDTO bloqueiosRequestDTO) {
        logger.info("Iniciando processo de bloqueio de primeiro acesso");
        String email = bloqueiosRequestDTO.email();
        Seguranca seguranca = findByEmail(email);

        if (seguranca == null) {
            logger.warn("Dados de segurança não encontrados para o email: {}", email);
            return new BloqueiosResponseDTO(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS);
        }

        if (seguranca.getPrimeiro_acesso()) {
            logger.warn("Usuário já esta bloqueado por primeiro acesso");
            return new BloqueiosResponseDTO(Constants.MSG_BLOQUEIO_PRIMEIRO_ACESSO_JA_SETADO);
        }

        seguranca.setPrimeiro_acesso(Boolean.TRUE);
        seguranca.setBloqueado_admin(Boolean.FALSE);
        seguranca.setBloqueado_tentativas(Boolean.FALSE);
        seguranca.setBloqueado_expiracao(Boolean.FALSE);
        seguranca.setTokenSeguranca(null);
        seguranca.setDataExpiracaoToken(null); // Limpa a data de expiração do token
        seguranca.setTentativas(null); // Reseta as tentativas
        seguranca.setDataExpiracaoSenha(null); // Limpa a data de expiração da senha
        repository.save(seguranca);

        logger.info("Bloqueio de primeiro acesso setado com sucesso para o email: {}", email);
        return new BloqueiosResponseDTO(Constants.
                MSG_BLOQUEIO_PRIMEIRO_ACESSO_SETADO);



    }

    public BloqueiosResponseDTO bloqueioAdmSeguranca(BloqueiosRequestDTO bloqueiosRequestDTO) {
        logger.info("Iniciando processo de bloqueio administrativo");
        String email = bloqueiosRequestDTO.email();
        Seguranca seguranca = findByEmail(email);

        if (seguranca == null) {
            logger.warn("Dados de segurança não encontrados para o email: {}", email);
            return new BloqueiosResponseDTO(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS);
        }

        if (seguranca.getBloqueado_admin()) {
            logger.warn(Constants.MSG_BLOQUEIO_ADM_JA_SETADO);
            return new BloqueiosResponseDTO(Constants.MSG_BLOQUEIO_ADM_JA_SETADO);
        }

        seguranca.setBloqueado_admin(Boolean.TRUE);
        repository.save(seguranca);

        logger.info("Bloqueio administrativo setado com sucesso para o email: {}", email);
        return new BloqueiosResponseDTO(Constants.
                MSG_BLOQUEIO_ADM_SETADO);

    }

    public BloqueiosResponseDTO desbloqueioAdmSeguranca(BloqueiosRequestDTO bloqueiosRequestDTO) {
        logger.info("Iniciando processo de desbloqueio administrativo");
        String email = bloqueiosRequestDTO.email();
        Seguranca seguranca = findByEmail(email);

        if (seguranca == null) {
            logger.warn("Dados de segurança não encontrados para o email: {}", email);
            return new BloqueiosResponseDTO(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS);
        }

        if (!seguranca.getBloqueado_admin()) {
            logger.warn(Constants.MSG_BLOQUEIO_ADM_DESATIVADO);
            return new BloqueiosResponseDTO(Constants.MSG_BLOQUEIO_ADM_DESATIVADO);
        }

        seguranca.setBloqueado_admin(Boolean.FALSE);
        repository.save(seguranca);

        logger.info("Bloqueio administrativo desativado com sucesso para o email: {}", email);
        return new BloqueiosResponseDTO(Constants.
                MSG_BLOQUEIO_ADM_DESATIVADO);

    }

    public void errouSenha(String email){
        logger.info("Iniciando processo de erro de senha");
        Seguranca seguranca = findByEmail(email);

        if (seguranca == null) {
            logger.warn("Dados de segurança não encontrados para o email: {}", email);
            throw new FichaTecnicaException(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS);
        }

        seguranca.setTentativas(seguranca.getTentativas() - 1);
        if (seguranca.getTentativas() <= 0) {
            seguranca.setBloqueado_tentativas(Boolean.TRUE);
            seguranca.setBloqueado_admin(Boolean.FALSE);
            seguranca.setBloqueado_expiracao(Boolean.FALSE);
            seguranca.setPrimeiro_acesso(Boolean.FALSE);
            repository.save(seguranca);
            logger.warn("Usuário bloqueado por tentativas excedidas");
            throw new FichaTecnicaException("Usuário bloqueado por tentativas excedidas");
        }

        repository.save(seguranca);
        logger.info("Tentativa de senha registrada. Tentativas restantes: {}", seguranca.getTentativas());
    }

    public void resetarTentativas(String email) {
        logger.info("Iniciando processo de reset de tentativas");
        Seguranca seguranca = findByEmail(email);

        if (seguranca == null) {
            logger.warn("Dados de segurança não encontrados para o email: {}", email);
            throw new FichaTecnicaException(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS);
        }

        seguranca.setTentativas(5); // Reseta as tentativas
        repository.save(seguranca);

        logger.info("Reset de tentativas realizado com sucesso para o email: {}", email);
    }

    public void validarAcesso(String email) {
        logger.info("Iniciando processo de validação de acesso");
        Seguranca seguranca = findByEmail(email);

        if (seguranca == null) {
            logger.warn("Dados de segurança não encontrados para o email: {}", email);
            throw new FichaTecnicaException(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS);
        }

        validarSenhaExpirada(seguranca.getEmail());

        if (seguranca.getBloqueado_admin()) {
            logger.warn("Usuário bloqueado administrativamente");
            throw new FichaTecnicaException("Usuário bloqueado administrativamente");
        } else if (seguranca.getPrimeiro_acesso()) {
            logger.info("Usuário bloqueado por primeiro acesso");
        } else if (seguranca.getBloqueado_expiracao()) {
            logger.warn("Usuário bloqueado por expiração de senha");
            throw new FichaTecnicaException("Usuário bloqueado por expiração de senha");
        } else if (seguranca.getBloqueado_tentativas()) {
            logger.warn("Usuário bloqueado por tentativas excedidas");
            throw new FichaTecnicaException("Usuário bloqueado por tentativas excedidas");
        } else {
            logger.info("Usuário com acesso normal");
        }
    }

    public void registrarUsuario(RegisterDTO registerDTO) {
        logger.info("Iniciando processo de registro de usuário");
        if (usuarioRepository.findByLogin(registerDTO.login()) != null) {
            logger.warn("Usuário já existe com o login: {}", registerDTO.login());
            throw new FichaTecnicaException("Usuário já existe com o login: " + registerDTO.login());
        }

        Usuario usuario = new Usuario(registerDTO.login(), null, registerDTO.role(),registerDTO.nome());

        usuarioRepository.save(usuario);

        Seguranca seguranca = new Seguranca();
        seguranca.setEmail(registerDTO.login());
        seguranca.setDataCriacao(LocalDateTime.now());
        seguranca.setCpf(registerDTO.cpf());
        seguranca.setPrimeiro_acesso(Boolean.TRUE);
        seguranca.setBloqueado_admin(Boolean.FALSE);
        seguranca.setBloqueado_tentativas(Boolean.FALSE);
        seguranca.setBloqueado_expiracao(Boolean.FALSE);
        seguranca.setTentativas(null); // Define o número de tentativas
        seguranca.setDataExpiracaoSenha(null); // Define a data de expiração da senha
        seguranca.setDataExpiracaoToken(null); // Limpa a data de expiração do token
        seguranca.setTokenSeguranca(null); // Limpa o token de segurança

        repository.save(seguranca);

        logger.info("Usuário registrado com sucesso: {}", usuario.getLogin());

    }

    public void excluirUsuario(String email) {
        logger.info("Iniciando processo de exclusão de usuário");

        Seguranca seguranca = findByEmail(email);
        if (seguranca == null) {
            logger.warn("Dados de segurança não encontrados para o email: {}", email);
            throw new FichaTecnicaException("Dados de segurança não encontrados");
        }

        segurancaRepository.delete(seguranca);

        Usuario usuario = usuarioRepository.findByLoginUsuario(email);
        if (usuario == null) {
            logger.warn("Usuário não encontrado para o email: {}", email);
            throw new FichaTecnicaException("Usuário não encontrado");
        }
        usuarioRepository.delete(usuario);
        logger.info("Usuário excluído com sucesso: {}", email);
    }

    public void expirarSenha(String email) {
        logger.info("Iniciando processo de expiração de senha");

        Seguranca seguranca = findByEmail(email);
        if (seguranca == null) {
            logger.warn("Dados de segurança não encontrados para o email: {}", email);
            throw new FichaTecnicaException("Dados de segurança não encontrados");
        }

        seguranca.setDataExpiracaoSenha(LocalDateTime.now().minusDays(1));// Define a data de expiração da senha para o passado
        seguranca.setBloqueado_expiracao(Boolean.TRUE);
        repository.save(seguranca);

        logger.info("Senha expirada com sucesso para o email: {}", email);
    }

    public void validarSenhaExpirada(String email) {
        logger.info("Iniciando processo de validação de senha expirada");

        Seguranca seguranca = findByEmail(email);
        if (seguranca == null) {
            logger.warn("Dados de segurança não encontrados para o email: {}", email);
            throw new FichaTecnicaException("Dados de segurança não encontrados");
        }

        if(seguranca.getDataExpiracaoSenha() != null && LocalDateTime.now().isAfter(seguranca.getDataExpiracaoSenha())){
            expirarSenha(seguranca.getEmail());
        }


        logger.info("Senha válida para o email: {}", email);
    }
}
