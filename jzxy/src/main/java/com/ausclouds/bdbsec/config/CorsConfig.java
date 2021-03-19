package com.ausclouds.bdbsec.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
	
	
    @Value("${ausclouds.cors.address:}")
    private String corsAddress = "";

    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        for (String item:corsAddress.split(",")) {
            corsConfiguration.addAllowedOrigin(item);
        }
     
        corsConfiguration.addAllowedHeader("productid");
        corsConfiguration.addAllowedHeader("xxl_sso_sessionid");
        corsConfiguration.addAllowedHeader("content-type");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setAllowCredentials(true);
        return corsConfiguration;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> CorsFilterRegistration() {
        FilterRegistrationBean<CorsFilter> registration =
                new FilterRegistrationBean<CorsFilter>();
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig());
        registration.setFilter(new CorsFilter(source));
        registration.setName("CorsFilter");
        registration.setOrder(1);
        registration.addUrlPatterns("/*");
        return registration;
    }
}