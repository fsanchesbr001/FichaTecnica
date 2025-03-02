package com.fabriciosanches.fichatecnica.domains;

import com.fabriciosanches.fichatecnica.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Table(name = "usuarios")
@Entity(name = "Usuario")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Getter
@Setter
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String login;
    private String senha;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private LocalDate dataAlteracao;
    private LocalDate dataExpiracao;

    private Integer tentativas;
    private Boolean expirado;
    private Boolean bloqueado;

    public Usuario(String login, String senha, UserRole role,
                   LocalDate dataAlteracao, LocalDate dataExpiracao,
                   Integer tentativas, Boolean expirado, Boolean bloqueado) {
        this.login = login;
        this.senha = senha;
        this.role = role;
        this.dataAlteracao = dataAlteracao;
        this.dataExpiracao = dataExpiracao;
        this.tentativas = tentativas;
        this.expirado = expirado;
        this.bloqueado = bloqueado;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(this.role == UserRole.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER"));

        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return login;
    }

}
