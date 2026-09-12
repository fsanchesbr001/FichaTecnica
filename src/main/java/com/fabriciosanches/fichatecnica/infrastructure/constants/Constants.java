package com.fabriciosanches.fichatecnica.infrastructure.constants;

public class Constants {
    /** Constantes para bloqueio de usuÃ¡rio */
    public static final String MSG_BLOQUEIO_ADM_JA_SETADO = "UsuÃ¡rio jÃ¡ esta bloqueado administrativamente";
    public static final String MSG_BLOQUEIO_ADM_SETADO = "Bloqueio administrativo setado.";
    public static final String MSG_BLOQUEIO_ADM_DESATIVADO = "Bloqueio administrativo desativado.";
    public static final String MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS = "BLQSNF-Credenciais de acesso incorretas, Contacte o " +
            "ADMINISTRADOR do sistema.";
    public static final String MSG_ERRO_BLOQUEIO = "Erro ao setar bloqueio.";
    public static final String MSG_ERRO_BLOQUEIO_ADM = "BLQADM-UsuÃ¡rio bloqueado administrativamente, Contacte o " +
            "ADMINISTRADOR do sistema.";
    public static final String MSG_ERRO_BLOQUEIO_PRIMEIRO_ACESSO = "BLQPAC-UsuÃ¡rio bloqueado por primeiro acesso";
    public static final String MSG_ERRO_BLOQUEIO_TENTATIVAS = "BLQTEN-UsuÃ¡rio bloqueado por tentativas excedidas";
    public static final String MSG_ERRO_BLOQUEIO_EXPIRACAO = "BLQEXP-UsuÃ¡rio bloqueado por expiraÃ§Ã£o de senha";

    /** Constantes para email */
    public static final String SUBJECT_EMAIL_RECUPERACAO_SENHA = "RecuperaÃ§Ã£o de senha - Ficha TÃ©cnica - Ollivander CafÃ©";
    public static final String TEMPLATE_EMAIL_RECUPERACAO_SENHA = "trocasenha";
    public static final String SUBJECT_EMAIL_PRIMEIRO_ACESSO = "Primeiro acesso - Ficha TÃ©cnica - Ollivander CafÃ©";
    public static final String TEMPLATE_EMAIL_PRIMEIRO_ACESSO = "primeiroacesso";



}

