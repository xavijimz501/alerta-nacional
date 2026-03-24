package com.alerta.alerta_nacional.services;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "facebook.page")
@Getter
@Setter
public class FacebookProperties {

    private String id;
    private String accessToken;

}
