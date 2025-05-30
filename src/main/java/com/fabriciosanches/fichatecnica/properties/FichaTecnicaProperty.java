package com.fabriciosanches.fichatecnica.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("ficha-tecnica")
public class FichaTecnicaProperty {
    private String originAllowed;
    private  final Mail mail = new Mail();

    @Getter
    @Setter
    public  static class Mail{
        private String host;
        private Integer port;
        private String username;
        private String password;
        private String from;
    }
}
