package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.RegisterDTO;
import com.fabriciosanches.fichatecnica.repository.UsuarioRepository;
import com.fabriciosanches.fichatecnica.security.DadosTokenJWT;
import com.fabriciosanches.fichatecnica.security.TokenService;
import com.fabriciosanches.fichatecnica.dtos.AutenticacaoDTO;
import com.fabriciosanches.fichatecnica.domains.Usuario;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {


    private AuthenticationManager manager;

    private TokenService tokenService;

    private UsuarioRepository usuarioRepository;

    public AutenticacaoController(AuthenticationManager manager,
                                  TokenService tokenService,
                                  UsuarioRepository usuarioRepository) {
        this.manager = manager;
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<DadosTokenJWT> efetuarLogin(@RequestBody @Valid AutenticacaoDTO dados){
        var userPwd = new UsernamePasswordAuthenticationToken(dados.login(),dados.senha());
        var authentication = manager.authenticate(userPwd);
        var token = tokenService.gerarToken((Usuario) authentication.getPrincipal());
        return ResponseEntity.ok(new DadosTokenJWT(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrarUsuario(@RequestBody @Valid RegisterDTO dados){
        if(usuarioRepository.findByLogin(dados.login())!=null){
            return ResponseEntity.badRequest().build();
        }
        var encriptdedPassword = new BCryptPasswordEncoder().encode(dados.senha());
        var usuario = new Usuario(dados.login(),encriptdedPassword,dados.role(),
                LocalDate.now(),LocalDate.now().plusMonths(3),
                0,Boolean.FALSE,Boolean.FALSE);

        usuarioRepository.save(usuario);

        return ResponseEntity.ok().build();
    }

}
