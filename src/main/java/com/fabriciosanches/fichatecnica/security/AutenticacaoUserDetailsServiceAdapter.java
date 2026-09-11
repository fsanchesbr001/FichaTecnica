package com.fabriciosanches.fichatecnica.security;

import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.in.AutenticarUsuarioPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AutenticacaoUserDetailsServiceAdapter implements UserDetailsService {

    private final AutenticarUsuarioPort autenticarUsuarioPort;

    public AutenticacaoUserDetailsServiceAdapter(AutenticarUsuarioPort autenticarUsuarioPort) {
        this.autenticarUsuarioPort = autenticarUsuarioPort;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Usuario usuario = autenticarUsuarioPort.buscarPorLogin(username);
            return new UsuarioSecurityDetails(usuario);
        } catch (Exception ex) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + username, ex);
        }
    }
}

