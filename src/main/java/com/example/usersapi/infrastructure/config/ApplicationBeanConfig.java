package com.example.usersapi.infrastructure.config;

import com.example.usersapi.domain.service.UserDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationBeanConfig {

    /**
     * UserDomainService es POJO puro (sin @Service) para mantener el dominio
     * libre de cualquier dependencia de framework. Spring lo gestiona desde aquí.
     */
    @Bean
    public UserDomainService userDomainService() {
        return new UserDomainService();
    }
}
