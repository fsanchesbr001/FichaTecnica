package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.constants.Constants;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.UsuarioRepository;
import com.fabriciosanches.fichatecnica.security.DadosTokenJWT;
import com.fabriciosanches.fichatecnica.security.TokenService;
import com.fabriciosanches.fichatecnica.dtos.AutenticacaoDTO;
import com.fabriciosanches.fichatecnica.domains.Usuario;
import com.fabriciosanches.fichatecnica.services.SegurancaService;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/auth")
public class AutenticacaoController {


    private final AuthenticationManager manager;

    private final TokenService tokenService;

    private final SegurancaService segurancaService;

    public AutenticacaoController(AuthenticationManager manager,
                                  TokenService tokenService,
                                  UsuarioRepository usuarioRepository,
                                  SegurancaService segurancaService) {
        this.manager = manager;
        this.tokenService = tokenService;
        this.segurancaService = segurancaService;
    }

    //Testado
    @PostMapping("/login")
    public ResponseEntity<DadosTokenJWT> efetuarLogin(@RequestBody @Valid AutenticacaoDTO dados){
        LoggerFactory.getLogger(this.getClass()).info("Fluxo entrou no método efetuarLogin - Usuário: {}", dados.login());

        if(dados.login() == null || dados.senha() == null){
            return ResponseEntity.badRequest().build();
        }
        try{
            segurancaService.validarAcesso(dados.login());
            var userPwd = new UsernamePasswordAuthenticationToken(dados.login(),dados.senha());
            var authentication = manager.authenticate(userPwd);
            segurancaService.resetarTentativas(dados.login());

            var usuario = (Usuario) authentication.getPrincipal();
            var token = tokenService.gerarToken(usuario);
            var expirationMinutes = tokenService.getExpirationMinutes();
            var expiresAt = tokenService.getTokenExpiresAt();
            var role = usuario.getRole().getRole();

            return ResponseEntity.ok(new DadosTokenJWT(
                token,
                expirationMinutes,
                expiresAt,
                usuario.getLogin(),
                usuario.getNome(),
                role
            ));
        }catch (BadCredentialsException e){
            segurancaService.errouSenha(dados.login());
            return ResponseEntity.badRequest().body(new DadosTokenJWT(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS));
        }
        catch (FichaTecnicaException ex) {
            return ResponseEntity.badRequest().body(new DadosTokenJWT(ex.getMessage()));
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body(new DadosTokenJWT(e.getMessage()));
        }
    }




}
