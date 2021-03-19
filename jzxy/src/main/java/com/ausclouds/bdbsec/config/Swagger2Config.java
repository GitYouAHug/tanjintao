package com.ausclouds.bdbsec.config;
/*package com.example.jzxy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Bean
    public Docket createRestApi() {
       // List<Parameter> pars = new ArrayList<Parameter>(); DocumentationType.SWAGGER_2
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.jzxy.controller"))
                .paths(PathSelectors.any())
                .build();
                //.globalOperationParameters(pars)
                //.apiInfo(apiInfo())
                
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("拾花酿春 RESTful API")
                .description("展示先做基础功能，后面再添加业务")
                //.termsOfServiceUrl("https://www.cnblogs.com/xiebq/")
                .version("1.0")
                .build();
    }

}
*/