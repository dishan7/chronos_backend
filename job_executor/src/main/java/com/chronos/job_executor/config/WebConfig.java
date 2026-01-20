package com.chronos.job_executor.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebConfig {

    @Value("${app.scheduler.base-url}")
    private String baseUrl;

    @Bean
    @Primary
    @Qualifier("jobSchedulerClient")
    public WebClient jobSchedulerClient() {
        // Create a factory that handles the base URL explicitly
        System.out.println("new build");
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);

        // This mode ensures the path is appended correctly to the host
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);
        return WebClient.builder()
                .uriBuilderFactory(factory)
                .filter((request, next) -> {
                    System.out.println(">>> ACTUAL REQUEST URL = " + request.url());
                    return next.exchange(request);
                })
                .build();
    }

}
