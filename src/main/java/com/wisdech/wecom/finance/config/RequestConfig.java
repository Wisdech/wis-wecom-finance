package com.wisdech.wecom.finance.config;


import com.wisdech.utils4j.request.RequestFilter;
import com.wisdech.utils4j.request.RequestLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RequestConfig implements WebMvcConfigurer {

    @Bean
    public RequestFilter requestFilter() {
        return new RequestFilter();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestLogger())
                .excludePathPatterns("/message-assets/**")
                .addPathPatterns("/**");
    }

}