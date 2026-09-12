package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name = "seguranca")
@Entity(name = "Seguranca")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SegurancaEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Id
    private Long codigo;

    @Column(nullable = false, unique = true, name = "cpf")
    private String cpf;

    @Column(nullable = false, name = "email")
    private String email;

    @Column(nullable = false, name = "token_seguranca")
    private String tokenSeguranca;

    @Column(nullable = false, name = "qtde_tentativas_login")
    private Integer tentativas;

    @Column(nullable = false, name = "bloqueio_adm")
    private Boolean bloqueado_admin;

    @Column(nullable = false, name = "bloqueio_tentativas")
    private Boolean bloqueado_tentativas;

    @Column(nullable = false, name = "bloqueio_expiracao")
    private Boolean bloqueado_expiracao;

    @Column(nullable = false, name = "primeiro_acesso")
    private Boolean primeiro_acesso;

    @Column(nullable = false, name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(nullable = false, name = "data_expiracao_senha")
    private LocalDateTime dataExpiracaoSenha;

    @Column(nullable = false, name = "data_expiracao_token")
    private LocalDateTime dataExpiracaoToken;
}


