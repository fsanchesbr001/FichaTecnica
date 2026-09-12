package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Hidden
public class SwaggerController {

    @GetMapping({"/swagger", "/docs"})
    public String redirectToSwaggerUi() {
        return "redirect:/swagger-ui.html";
    }
}


