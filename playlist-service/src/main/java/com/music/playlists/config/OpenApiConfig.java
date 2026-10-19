package com.music.playlists.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Playlist Service API")
                        .version("1.0")
                        .description("Плейлисты"))
                .servers(List.of(
                        new Server().url("http://localhost:8090").description("gateway"),
                        new Server().url("http://localhost:8093").description("напрямую")));
    }
}
