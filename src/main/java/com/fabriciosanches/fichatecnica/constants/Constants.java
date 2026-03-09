package com.fabriciosanches.fichatecnica.constants;

public class Constants {
    /** Constantes para bloqueio de usuário */
    public static final String MSG_BLOQUEIO_ADM_JA_SETADO = "Usuário já esta bloqueado administrativamente";
    public static final String MSG_BLOQUEIO_ADM_SETADO = "Bloqueio administrativo setado.";
    public static final String MSG_BLOQUEIO_ADM_DESATIVADO = "Bloqueio administrativo desativado.";
    public static final String MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS = "BLQSNF-Credenciais de acesso incorretas, Contacte o " +
            "ADMINISTRADOR do sistema.";
    public static final String MSG_ERRO_BLOQUEIO = "Erro ao setar bloqueio.";
    public static final String MSG_ERRO_BLOQUEIO_ADM = "BLQADM-Usuário bloqueado administrativamente, Contacte o " +
            "ADMINISTRADOR do sistema.";
    public static final String MSG_ERRO_BLOQUEIO_PRIMEIRO_ACESSO = "BLQPAC-Usuário bloqueado por primeiro acesso";
    public static final String MSG_ERRO_BLOQUEIO_TENTATIVAS = "BLQTEN-Usuário bloqueado por tentativas excedidas";
    public static final String MSG_ERRO_BLOQUEIO_EXPIRACAO = "BLQEXP-Usuário bloqueado por expiração de senha";

    /** Constantes para email */
    public static final String SUBJECT_EMAIL_RECUPERACAO_SENHA = "Recuperação de senha - Ficha Técnica - Ollivander Café";
    public static final String TEMPLATE_EMAIL_RECUPERACAO_SENHA = "trocasenha";
    public static final String SUBJECT_EMAIL_PRIMEIRO_ACESSO = "Primeiro acesso - Ficha Técnica - Ollivander Café";
    public static final String TEMPLATE_EMAIL_PRIMEIRO_ACESSO = "primeiroacesso";



}
