package com.fabriciosanches.fichatecnica.domains;

import com.fabriciosanches.fichatecnica.dtos.SegurancaDTO;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "seguranca")
@Entity(name = "Seguranca")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Seguranca {

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

    public Seguranca(SegurancaDTO segurancaDTO) {
        this.codigo = null; // Código é gerado automaticamente
        this.cpf = segurancaDTO.cpf();
        this.email = segurancaDTO.email();
        this.tokenSeguranca = segurancaDTO.tokenSeguranca();
        this.tentativas = segurancaDTO.tentativas();
        this.bloqueado_admin = segurancaDTO.bloqueado_admin();
        this.bloqueado_tentativas = segurancaDTO.bloqueado_tentativas();
        this.bloqueado_expiracao = segurancaDTO.bloqueado_expiracao();
        this.primeiro_acesso = segurancaDTO.primeiro_acesso();
        this.dataCriacao = LocalDateTime.now(); // Data de criação é a data atual
        this.dataExpiracaoSenha = segurancaDTO.dataExpiracaoSenha();
        this.dataExpiracaoToken = segurancaDTO.dataExpiracaoToken();
    }
}
