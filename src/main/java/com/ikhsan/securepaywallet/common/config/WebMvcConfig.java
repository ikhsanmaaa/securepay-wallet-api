package com.ikhsan.securepaywallet.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ikhsan.securepaywallet.auth.session.interceptor.SessionActivityInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final SessionActivityInterceptor sessionActivityInterceptor;

    public WebMvcConfig(
            SessionActivityInterceptor sessionActivityInterceptor) {
        this.sessionActivityInterceptor = sessionActivityInterceptor;
    }

    @Override
    public void addInterceptors(
            InterceptorRegistry registry) {

        registry.addInterceptor(
                sessionActivityInterceptor);
    }
}
