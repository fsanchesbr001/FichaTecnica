package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.constants.Constants;
import com.fabriciosanches.fichatecnica.domains.Seguranca;
import com.fabriciosanches.fichatecnica.domains.Usuario;
import com.fabriciosanches.fichatecnica.dtos.AtualizarUsuarioRequestDTO;
import com.fabriciosanches.fichatecnica.dtos.BloqueiosRequestDTO;
import com.fabriciosanches.fichatecnica.dtos.BloqueiosResponseDTO;
import com.fabriciosanches.fichatecnica.dtos.EnviarEmailPrimeiroAcessoRequestDTO;
import com.fabriciosanches.fichatecnica.dtos.EnviarEmailSegurancaResponseDTO;
import com.fabriciosanches.fichatecnica.dtos.RegisterDTO;
import com.fabriciosanches.fichatecnica.dtos.UsuarioListagemDTO;
import com.fabriciosanches.fichatecnica.enums.UserRole;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.mail.EmailService;
import com.fabriciosanches.fichatecnica.repository.SegurancaRepository;
import com.fabriciosanches.fichatecnica.repository.UsuarioRepository;
import com.fabriciosanches.fichatecnica.util.Utilidades;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SegurancaServiceTest {

    @Mock
    private SegurancaRepository repository;
    @Mock
    private EmailService emailService;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private SegurancaService service;

    private Seguranca seguranca;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        seguranca = criarSeguranca("user@email.com");
        usuario = new Usuario(1L, "user@email.com", "senha", UserRole.ADMIN, "Usuário Teste");
    }

    @Test
    void enviarEmailSeguranca_DeveGerarTokenSalvarEEnviarEmail() throws MessagingException {
        when(repository.findCPFByEmail("user@email.com")).thenReturn("52998224725");
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);
        when(repository.save(seguranca)).thenReturn(seguranca);

        EnviarEmailSegurancaResponseDTO result = service.enviarEmailSeguranca("user@email.com");

        assertEquals("user@email.com", result.email());
        assertEquals("52998224725", result.cpf());
        assertNotNull(result.tokenSeguranca());
        assertEquals(8, result.tokenSeguranca().length());
        assertTrue(result.dataExpiracaoToken().matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}"));
        assertEquals(result.tokenSeguranca(), seguranca.getTokenSeguranca());
        verify(emailService).sendEmail(eq("user@email.com"), eq(Constants.SUBJECT_EMAIL_RECUPERACAO_SENHA),
                eq(Constants.TEMPLATE_EMAIL_RECUPERACAO_SENHA), anyMap());
    }

    @Test
    void enviarEmailSeguranca_DeveLancarExcecaoQuandoEmailNaoExistir() {
        when(repository.findCPFByEmail("user@email.com")).thenReturn(null);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.enviarEmailSeguranca("user@email.com"));

        assertEquals("Email não encontrado", exception.getMessage());
    }

    @Test
    void enviarEmailSeguranca_DeveLancarExcecaoQuandoCpfForInvalido() {
        when(repository.findCPFByEmail("user@email.com")).thenReturn("12345678900");

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.enviarEmailSeguranca("user@email.com"));

        assertEquals("CPF inválido", exception.getMessage());
    }

    @Test
    void enviarEmailPrimeiroAcesso_DeveEnviarTemplateCorreto() throws MessagingException {
        EnviarEmailPrimeiroAcessoRequestDTO dto = new EnviarEmailPrimeiroAcessoRequestDTO(
                "user@email.com", "Usuário Teste", "Senha@123"
        );

        service.enviarEmailPrimeiroAcesso(dto);

        verify(emailService).sendEmail(eq("user@email.com"), eq(Constants.SUBJECT_EMAIL_PRIMEIRO_ACESSO),
                eq(Constants.TEMPLATE_EMAIL_PRIMEIRO_ACESSO), anyMap());
    }

    @Test
    void trocarSenhaSeguranca_DeveAtualizarSenhaEEstadoDeSeguranca() {
        String senhaBase64 = Utilidades.encodeToBase64("NovaSenha@123");
        seguranca.setTokenSeguranca("12345678");
        seguranca.setDataExpiracaoToken(LocalDateTime.now().plusHours(1));
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);
        when(usuarioRepository.findByLoginUsuario("user@email.com")).thenReturn(usuario);

        service.trocarSenhaSeguranca("user@email.com", "52998224725", "12345678", senhaBase64, senhaBase64);

        assertNull(seguranca.getTokenSeguranca());
        assertNull(seguranca.getDataExpiracaoToken());
        assertFalse(seguranca.getPrimeiro_acesso());
        assertFalse(seguranca.getBloqueado_admin());
        assertFalse(seguranca.getBloqueado_tentativas());
        assertFalse(seguranca.getBloqueado_expiracao());
        assertEquals(5, seguranca.getTentativas());
        verify(repository, atLeastOnce()).save(seguranca);
        verify(usuarioRepository).save(argThat(u -> !"NovaSenha@123".equals(u.getSenha()) && u.getSenha() != null));
    }

    @Test
    void trocarSenhaSeguranca_DeveLancarExcecaoQuandoDadosNaoExistirem() {
        when(repository.findByEmail("user@email.com")).thenReturn(null);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.trocarSenhaSeguranca("user@email.com", "52998224725", "12345678", "a", "a"));

        assertEquals(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS, exception.getMessage());
    }

    @Test
    void trocarSenhaSeguranca_DeveLancarExcecaoQuandoCpfNaoCorresponder() {
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.trocarSenhaSeguranca("user@email.com", "00000000000", "12345678", "a", "a"));

        assertEquals("CPF não corresponde ao email", exception.getMessage());
    }

    @Test
    void trocarSenhaSeguranca_DeveLancarExcecaoQuandoTokenForInvalido() {
        seguranca.setTokenSeguranca("12345678");
        seguranca.setDataExpiracaoToken(LocalDateTime.now().minusMinutes(1));
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.trocarSenhaSeguranca("user@email.com", "52998224725", "12345678", "a", "a"));

        assertEquals("Token de segurança inválido ou expirado", exception.getMessage());
    }

    @Test
    void trocarSenhaSeguranca_DeveLancarExcecaoQuandoSenhaNaoConferir() {
        seguranca.setTokenSeguranca("12345678");
        seguranca.setDataExpiracaoToken(LocalDateTime.now().plusMinutes(10));
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.trocarSenhaSeguranca("user@email.com", "52998224725", "12345678", "senha1", "senha2"));

        assertEquals("Senhas não coincidem ou são inválidas", exception.getMessage());
    }

    @Test
    void trocarSenhaSeguranca_DeveLancarExcecaoQuandoUsuarioNaoForEncontrado() {
        String senhaBase64 = Utilidades.encodeToBase64("NovaSenha@123");
        seguranca.setTokenSeguranca("12345678");
        seguranca.setDataExpiracaoToken(LocalDateTime.now().plusMinutes(10));
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);
        when(usuarioRepository.findByLoginUsuario("user@email.com")).thenReturn(null);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.trocarSenhaSeguranca("user@email.com", "52998224725", "12345678", senhaBase64, senhaBase64));

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void bloqueioAdmSeguranca_DeveBloquearQuandoPossivel() {
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        BloqueiosResponseDTO result = service.bloqueioAdmSeguranca(new BloqueiosRequestDTO("user@email.com"));

        assertEquals(Constants.MSG_BLOQUEIO_ADM_SETADO, result.mensagem());
        assertTrue(seguranca.getBloqueado_admin());
    }

    @Test
    void bloqueioAdmSeguranca_DeveRetornarMensagemQuandoJaBloqueado() {
        seguranca.setBloqueado_admin(true);
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        BloqueiosResponseDTO result = service.bloqueioAdmSeguranca(new BloqueiosRequestDTO("user@email.com"));

        assertEquals(Constants.MSG_BLOQUEIO_ADM_JA_SETADO, result.mensagem());
    }

    @Test
    void bloqueioAdmSeguranca_DeveRetornarMensagemQuandoSegurancaNaoExistir() {
        when(repository.findByEmail("user@email.com")).thenReturn(null);

        BloqueiosResponseDTO result = service.bloqueioAdmSeguranca(new BloqueiosRequestDTO("user@email.com"));

        assertEquals(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS, result.mensagem());
    }

    @Test
    void desbloqueioAdmSeguranca_DeveDesbloquearQuandoPossivel() {
        seguranca.setBloqueado_admin(true);
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        BloqueiosResponseDTO result = service.desbloqueioAdmSeguranca(new BloqueiosRequestDTO("user@email.com"));

        assertEquals(Constants.MSG_BLOQUEIO_ADM_DESATIVADO, result.mensagem());
        assertFalse(seguranca.getBloqueado_admin());
    }

    @Test
    void desbloqueioAdmSeguranca_DeveRetornarMensagemQuandoSegurancaNaoExistir() {
        when(repository.findByEmail("user@email.com")).thenReturn(null);

        BloqueiosResponseDTO result = service.desbloqueioAdmSeguranca(new BloqueiosRequestDTO("user@email.com"));

        assertEquals(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS, result.mensagem());
    }

    @Test
    void desbloqueioAdmSeguranca_DeveRetornarMensagemQuandoJaEstiverDesbloqueado() {
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        BloqueiosResponseDTO result = service.desbloqueioAdmSeguranca(new BloqueiosRequestDTO("user@email.com"));

        assertEquals(Constants.MSG_BLOQUEIO_ADM_DESATIVADO, result.mensagem());
    }

    @Test
    void errouSenha_DeveReduzirTentativas() {
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        service.errouSenha("user@email.com");

        assertEquals(4, seguranca.getTentativas());
        verify(repository).save(seguranca);
    }

    @Test
    void errouSenha_DeveBloquearAoExcederTentativas() {
        seguranca.setTentativas(1);
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.errouSenha("user@email.com"));

        assertEquals("Usuário bloqueado por tentativas excedidas", exception.getMessage());
        assertTrue(seguranca.getBloqueado_tentativas());
        assertFalse(seguranca.getBloqueado_admin());
        assertFalse(seguranca.getBloqueado_expiracao());
        assertFalse(seguranca.getPrimeiro_acesso());
    }

    @Test
    void errouSenha_DeveLancarExcecaoQuandoSegurancaNaoExistir() {
        when(repository.findByEmail("user@email.com")).thenReturn(null);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.errouSenha("user@email.com"));

        assertEquals(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS, exception.getMessage());
    }

    @Test
    void resetarTentativas_DeveVoltarParaCinco() {
        seguranca.setTentativas(1);
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        service.resetarTentativas("user@email.com");

        assertEquals(5, seguranca.getTentativas());
        verify(repository).save(seguranca);
    }

    @Test
    void resetarTentativas_DeveLancarExcecaoQuandoSegurancaNaoExistir() {
        when(repository.findByEmail("user@email.com")).thenReturn(null);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.resetarTentativas("user@email.com"));

        assertEquals(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS, exception.getMessage());
    }

    @Test
    void validarAcesso_DeveLancarExcecaoQuandoBloqueadoAdministrativamente() {
        seguranca.setBloqueado_admin(true);
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.validarAcesso("user@email.com"));

        assertEquals(Constants.MSG_ERRO_BLOQUEIO_ADM, exception.getMessage());
    }

    @Test
    void validarAcesso_DeveLancarExcecaoQuandoPrimeiroAcesso() {
        seguranca.setPrimeiro_acesso(true);
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.validarAcesso("user@email.com"));

        assertEquals(Constants.MSG_ERRO_BLOQUEIO_PRIMEIRO_ACESSO, exception.getMessage());
    }

    @Test
    void validarAcesso_DeveLancarExcecaoQuandoBloqueadoPorExpiracao() {
        seguranca.setBloqueado_expiracao(true);
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.validarAcesso("user@email.com"));

        assertEquals(Constants.MSG_ERRO_BLOQUEIO_EXPIRACAO, exception.getMessage());
    }

    @Test
    void validarAcesso_DeveLancarExcecaoQuandoBloqueadoPorTentativas() {
        seguranca.setBloqueado_tentativas(true);
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.validarAcesso("user@email.com"));

        assertEquals(Constants.MSG_ERRO_BLOQUEIO_TENTATIVAS, exception.getMessage());
    }

    @Test
    void validarAcesso_DeveLancarExcecaoQuandoSegurancaNaoExistir() {
        when(repository.findByEmail("user@email.com")).thenReturn(null);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.validarAcesso("user@email.com"));

        assertEquals(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS, exception.getMessage());
    }

    @Test
    void validarAcesso_DevePermitirQuandoSemBloqueios() {
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        assertDoesNotThrow(() -> service.validarAcesso("user@email.com"));
    }

    @Test
    void registrarUsuario_DeveSalvarUsuarioSegurancaEEnviarEmail() throws MessagingException {
        RegisterDTO dto = new RegisterDTO("novo@email.com", null, UserRole.ADMIN, "Novo Usuário", "52998224725");
        when(usuarioRepository.findByLogin("novo@email.com")).thenReturn(null);

        service.registrarUsuario(dto);

        verify(usuarioRepository).save(argThat(u -> u.getLogin().equals("novo@email.com") && u.getRole() == UserRole.ADMIN));
        verify(repository).save(argThat(s -> s.getEmail().equals("novo@email.com") && s.getPrimeiro_acesso()));
        verify(emailService).sendEmail(eq("novo@email.com"), eq(Constants.SUBJECT_EMAIL_PRIMEIRO_ACESSO),
                eq(Constants.TEMPLATE_EMAIL_PRIMEIRO_ACESSO), anyMap());
    }

    @Test
    void registrarUsuario_DeveLancarExcecaoQuandoLoginJaExistir() {
        RegisterDTO dto = new RegisterDTO("novo@email.com", null, UserRole.ADMIN, "Novo Usuário", "52998224725");
        when(usuarioRepository.findByLogin("novo@email.com")).thenReturn(mock(UserDetails.class));

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.registrarUsuario(dto));

        assertEquals("Usuário já existe com o login: novo@email.com", exception.getMessage());
    }

    @Test
    void excluirUsuario_DeveExcluirSegurancaEUsuario() {
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);
        when(usuarioRepository.findByLoginUsuario("user@email.com")).thenReturn(usuario);

        service.excluirUsuario("user@email.com");

        verify(repository).delete(seguranca);
        verify(usuarioRepository).delete(usuario);
    }

    @Test
    void excluirUsuario_DeveLancarExcecaoQuandoSegurancaNaoExistir() {
        when(repository.findByEmail("user@email.com")).thenReturn(null);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.excluirUsuario("user@email.com"));

        assertEquals(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS, exception.getMessage());
    }

    @Test
    void excluirUsuario_DeveLancarExcecaoQuandoUsuarioNaoExistir() {
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);
        when(usuarioRepository.findByLoginUsuario("user@email.com")).thenReturn(null);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.excluirUsuario("user@email.com"));

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void primeiroAcesso_DeveRecriarUsuarioComDadosOriginais() throws MessagingException {
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);
        when(usuarioRepository.findByLoginUsuario("user@email.com")).thenReturn(usuario);
        when(usuarioRepository.findByLogin("user@email.com")).thenReturn(null);

        service.primeiroAcesso("user@email.com");

        verify(repository).delete(seguranca);
        verify(usuarioRepository).delete(usuario);
        verify(usuarioRepository).save(argThat(u -> u.getLogin().equals("user@email.com") && u.getRole() == UserRole.ADMIN));
        verify(repository, atLeastOnce()).save(any(Seguranca.class));
        verify(emailService).sendEmail(eq("user@email.com"), eq(Constants.SUBJECT_EMAIL_PRIMEIRO_ACESSO),
                eq(Constants.TEMPLATE_EMAIL_PRIMEIRO_ACESSO), anyMap());
    }

    @Test
    void primeiroAcesso_DeveLancarExcecaoQuandoCpfForInvalido() {
        seguranca.setCpf(null);
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);
        when(usuarioRepository.findByLoginUsuario("user@email.com")).thenReturn(usuario);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.primeiroAcesso("user@email.com"));

        assertEquals("CPF inválido para o usuário: user@email.com", exception.getMessage());
    }

    @Test
    void primeiroAcesso_DeveLancarExcecaoQuandoSegurancaNaoExistir() {
        when(repository.findByEmail("user@email.com")).thenReturn(null);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.primeiroAcesso("user@email.com"));

        assertEquals(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS, exception.getMessage());
    }

    @Test
    void primeiroAcesso_DeveLancarExcecaoQuandoUsuarioNaoExistir() {
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);
        when(usuarioRepository.findByLoginUsuario("user@email.com")).thenReturn(null);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.primeiroAcesso("user@email.com"));

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void expirarSenha_DeveMarcarSenhaComoExpirada() {
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        service.expirarSenha("user@email.com");

        assertTrue(seguranca.getBloqueado_expiracao());
        assertTrue(seguranca.getDataExpiracaoSenha().isBefore(LocalDateTime.now()));
        verify(repository).save(seguranca);
    }

    @Test
    void expirarSenha_DeveLancarExcecaoQuandoSegurancaNaoExistir() {
        when(repository.findByEmail("user@email.com")).thenReturn(null);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.expirarSenha("user@email.com"));

        assertEquals(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS, exception.getMessage());
    }

    @Test
    void validarSenhaExpirada_DeveExpirarQuandoDataJaPassou() {
        seguranca.setDataExpiracaoSenha(LocalDateTime.now().minusDays(1));
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        service.validarSenhaExpirada("user@email.com");

        assertTrue(seguranca.getBloqueado_expiracao());
        verify(repository).save(seguranca);
    }

    @Test
    void validarSenhaExpirada_DeveLancarExcecaoQuandoSegurancaNaoExistir() {
        when(repository.findByEmail("user@email.com")).thenReturn(null);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.validarSenhaExpirada("user@email.com"));

        assertEquals(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS, exception.getMessage());
    }

    @Test
    void findByEmailComDadosUsuario_DeveRetornarDtoEnriquecido() {
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);
        when(usuarioRepository.findByLoginUsuario("user@email.com")).thenReturn(usuario);

        UsuarioListagemDTO result = service.findByEmailComDadosUsuario("user@email.com");

        assertEquals("Usuário Teste", result.nome());
        assertEquals("ADMIN", result.role());
    }

    @Test
    void findByEmailComDadosUsuario_DeveRetornarNullQuandoSegurancaNaoExistir() {
        when(repository.findByEmail("user@email.com")).thenReturn(null);

        assertNull(service.findByEmailComDadosUsuario("user@email.com"));
    }

    @Test
    void atualizarUsuario_DeveAtualizarSegurancaENomeERole() {
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);
        when(usuarioRepository.findByLoginUsuario("user@email.com")).thenReturn(usuario);

        AtualizarUsuarioRequestDTO dados = new AtualizarUsuarioRequestDTO(true, true, false, false,
                "Nome Atualizado", UserRole.SYSTEM);

        UsuarioListagemDTO result = service.atualizarUsuario("user@email.com", dados);

        assertTrue(seguranca.getBloqueado_admin());
        assertTrue(seguranca.getBloqueado_tentativas());
        assertFalse(seguranca.getPrimeiro_acesso());
        assertEquals("Nome Atualizado", usuario.getNome());
        assertEquals(UserRole.SYSTEM, usuario.getRole());
        assertEquals("SYSTEM", result.role());
    }

    @Test
    void atualizarUsuario_DeveLancarExcecaoQuandoSegurancaNaoExistir() {
        when(repository.findByEmail("user@email.com")).thenReturn(null);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.atualizarUsuario("user@email.com",
                        new AtualizarUsuarioRequestDTO(true, null, null, null, null, null)));

        assertEquals("Usuário não encontrado: user@email.com", exception.getMessage());
    }

    @Test
    void atualizarUsuario_DeveLancarExcecaoQuandoUsuarioNaoExistirNaTabelaUsuarios() {
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);
        when(usuarioRepository.findByLoginUsuario("user@email.com")).thenReturn(null);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.atualizarUsuario("user@email.com",
                        new AtualizarUsuarioRequestDTO(true, null, null, null, null, null)));

        assertEquals("Usuário não encontrado na tabela usuarios: user@email.com", exception.getMessage());
    }

    @Test
    void findAllComDadosUsuario_DeveMontarListaComDadosCombinados() {
        Seguranca seguranca2 = criarSeguranca("outro@email.com");
        when(repository.findAll()).thenReturn(List.of(seguranca, seguranca2));
        when(usuarioRepository.findByLoginUsuario("user@email.com")).thenReturn(usuario);
        when(usuarioRepository.findByLoginUsuario("outro@email.com")).thenReturn(null);

        List<UsuarioListagemDTO> result = service.findAllComDadosUsuario();

        assertEquals(2, result.size());
        assertEquals("Usuário Teste", result.get(0).nome());
        assertNull(result.get(1).nome());
    }

    @Test
    void findAllComDadosUsuario_DeveRetornarListaVaziaQuandoNaoHouverRegistros() {
        when(repository.findAll()).thenReturn(List.of());

        List<UsuarioListagemDTO> result = service.findAllComDadosUsuario();

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_DeveRetornarTodosOsRegistrosDeSeguranca() {
        when(repository.findAll()).thenReturn(List.of(seguranca));

        List<Seguranca> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("user@email.com", result.get(0).getEmail());
    }

    @Test
    void findByEmail_DeveRetornarNullQuandoNaoEncontrar() {
        when(repository.findByEmail("user@email.com")).thenReturn(null);

        assertNull(service.findByEmail("user@email.com"));
    }

    @Test
    void findByEmail_DeveRetornarSegurancaQuandoEncontrar() {
        when(repository.findByEmail("user@email.com")).thenReturn(seguranca);

        Seguranca result = service.findByEmail("user@email.com");

        assertEquals("user@email.com", result.getEmail());
    }

    private Seguranca criarSeguranca(String email) {
        Seguranca dados = new Seguranca();
        dados.setCodigo(1L);
        dados.setEmail(email);
        dados.setCpf("52998224725");
        dados.setTokenSeguranca("12345678");
        dados.setTentativas(5);
        dados.setBloqueado_admin(false);
        dados.setBloqueado_tentativas(false);
        dados.setBloqueado_expiracao(false);
        dados.setPrimeiro_acesso(false);
        dados.setDataCriacao(LocalDateTime.now().minusDays(1));
        dados.setDataExpiracaoSenha(LocalDateTime.now().plusDays(30));
        dados.setDataExpiracaoToken(LocalDateTime.now().plusHours(2));
        return dados;
    }
}

