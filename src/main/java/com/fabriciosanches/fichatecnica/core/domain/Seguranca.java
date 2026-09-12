package com.fabriciosanches.fichatecnica.core.domain;

import java.time.LocalDateTime;

public class Seguranca {
    private Long codigo;
    private String cpf;
    private String email;
    private String tokenSeguranca;
    private Integer tentativas;
    private Boolean bloqueado_admin;
    private Boolean bloqueado_tentativas;
    private Boolean bloqueado_expiracao;
    private Boolean primeiro_acesso;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataExpiracaoSenha;
    private LocalDateTime dataExpiracaoToken;

    public Seguranca() {
    }

    public Seguranca(Long codigo, String cpf, String email, String tokenSeguranca, Integer tentativas,
                     Boolean bloqueado_admin, Boolean bloqueado_tentativas, Boolean bloqueado_expiracao,
                     Boolean primeiro_acesso, LocalDateTime dataCriacao, LocalDateTime dataExpiracaoSenha,
                     LocalDateTime dataExpiracaoToken) {
        this.codigo = codigo;
        this.cpf = cpf;
        this.email = email;
        this.tokenSeguranca = tokenSeguranca;
        this.tentativas = tentativas;
        this.bloqueado_admin = bloqueado_admin;
        this.bloqueado_tentativas = bloqueado_tentativas;
        this.bloqueado_expiracao = bloqueado_expiracao;
        this.primeiro_acesso = primeiro_acesso;
        this.dataCriacao = dataCriacao;
        this.dataExpiracaoSenha = dataExpiracaoSenha;
        this.dataExpiracaoToken = dataExpiracaoToken;
    }

    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTokenSeguranca() {
        return tokenSeguranca;
    }

    public void setTokenSeguranca(String tokenSeguranca) {
        this.tokenSeguranca = tokenSeguranca;
    }

    public Integer getTentativas() {
        return tentativas;
    }

    public void setTentativas(Integer tentativas) {
        this.tentativas = tentativas;
    }

    public Boolean getBloqueado_admin() {
        return bloqueado_admin;
    }

    public void setBloqueado_admin(Boolean bloqueado_admin) {
        this.bloqueado_admin = bloqueado_admin;
    }

    public Boolean getBloqueado_tentativas() {
        return bloqueado_tentativas;
    }

    public void setBloqueado_tentativas(Boolean bloqueado_tentativas) {
        this.bloqueado_tentativas = bloqueado_tentativas;
    }

    public Boolean getBloqueado_expiracao() {
        return bloqueado_expiracao;
    }

    public void setBloqueado_expiracao(Boolean bloqueado_expiracao) {
        this.bloqueado_expiracao = bloqueado_expiracao;
    }

    public Boolean getPrimeiro_acesso() {
        return primeiro_acesso;
    }

    public void setPrimeiro_acesso(Boolean primeiro_acesso) {
        this.primeiro_acesso = primeiro_acesso;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataExpiracaoSenha() {
        return dataExpiracaoSenha;
    }

    public void setDataExpiracaoSenha(LocalDateTime dataExpiracaoSenha) {
        this.dataExpiracaoSenha = dataExpiracaoSenha;
    }

    public LocalDateTime getDataExpiracaoToken() {
        return dataExpiracaoToken;
    }

    public void setDataExpiracaoToken(LocalDateTime dataExpiracaoToken) {
        this.dataExpiracaoToken = dataExpiracaoToken;
    }
}


