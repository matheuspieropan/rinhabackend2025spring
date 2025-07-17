package org.pieropan.rinhaspring.configuration;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private WebClient createWebClient(String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(TIMEOUT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TIMEOUT.toMillis())
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public WebClient defaultProcessorWebClient(@Value("${pagamento.processor.default.url}") String baseUrl) {
        return createWebClient(baseUrl);
    }

    @Bean
    public WebClient fallbackProcessorWebClient(@Value("${pagamento.processor.fallback.url}") String baseUrl) {
        return createWebClient(baseUrl);
    }
}