package com.phongkoxai.shortvideosappx.common.config;

import com.phongkoxai.shortvideosappx.auth.repository.httpClient.OutboundIdentityClient;
import com.phongkoxai.shortvideosappx.auth.repository.httpClient.OutboundUserClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpInterfaceConfig {

    @Bean
    RestClient googleOAuthRestClient() {
        return RestClient.builder().build();
    }

    @Bean
    OutboundIdentityClient outboundIdentityClient(RestClient googleOAuthRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(googleOAuthRestClient))
                .build();

        return factory.createClient(OutboundIdentityClient.class);
    }

    @Bean
    OutboundUserClient outboundUserClient(RestClient googleOAuthRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(googleOAuthRestClient))
                .build();

        return factory.createClient(OutboundUserClient.class);
    }
}