package org.example.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
@EnableWebMvc
public class WebMvcConfig {
    // 固定后缀可以是/message 或者 /sse 或者 /${other}/message 或者 /${other}/sse
    public static final String MESSAGE_ENDPOINT = "/mcp/message";
    public static final String SSE_ENDPOINT = "/mcp/sse";

    // mcp配置开始
    @Bean
    public WebMvcSseServerTransportProvider webMvcSseServerTransportProvider() {
        return new WebMvcSseServerTransportProvider(new ObjectMapper(), MESSAGE_ENDPOINT, SSE_ENDPOINT);
    }

    @Bean
    public RouterFunction<ServerResponse> mcpRouterFunction(WebMvcSseServerTransportProvider transportProvider) {
        return transportProvider.getRouterFunction();
    }
    // mcp配置结束
}
