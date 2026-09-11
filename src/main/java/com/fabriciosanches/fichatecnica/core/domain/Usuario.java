package com.fabriciosanches.fichatecnica.core.domain;

import com.fabriciosanches.fichatecnica.enums.UserRole;

import java.util.List;

public class Usuario {
    private Long id;
    private String login;
    private String senha;
    private UserRole role;
    private String nome;

    public Usuario() {
    }

    public Usuario(Long id, String login, String senha, UserRole role, String nome) {
        this.id = id;
        this.login = login;
        this.senha = senha;
        this.role = role;
        this.nome = nome;
    }

    public Usuario(String login, String senha, UserRole role, String nome) {
        this(null, login, senha, role, nome);
    }

    public List<String> getAuthorities() {
        if (this.role == null) {
            return List.of("ROLE_USER");
        }
        if (this.role == UserRole.ADMIN) {
            return List.of("ROLE_ADMIN", "ROLE_USER");
        }
        return List.of(this.role.getRole());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}

