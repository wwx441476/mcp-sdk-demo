import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

import java.time.Duration;
import java.util.Map;

public class HttpClientSseMcpTest {
    public static void main(String[] args) {
        McpClientTransport transport = HttpClientSseClientTransport
                .builder("http://localhost:12000")
                .sseEndpoint("/mcp/sse")
                .objectMapper(new ObjectMapper())
                .build();

        McpSyncClient client = McpClient.sync(transport).clientInfo(new McpSchema.Implementation("my-mcp-client", "1.0.0"))
                .capabilities(McpSchema.ClientCapabilities.builder().roots(true).sampling().build())
                .requestTimeout(Duration.ofSeconds(60)).build();
        McpSchema.InitializeResult initialize = client.initialize();
        System.out.println("client initialized: " + initialize);
        tools(client);
        searchPerson(client);
    }

    public static void tools(McpSyncClient client) {
        McpSchema.ListToolsResult listToolsResult = client.listTools();
        listToolsResult.tools().forEach(System.out::println);
    }

    public static void searchPerson(McpSyncClient client) {
        McpSchema.CallToolRequest callToolRequest = new McpSchema.CallToolRequest(
                "searchPerson",
                Map.of("name", "典韦")
        );
        McpSchema.CallToolResult callToolResult = client.callTool(callToolRequest);
        System.out.println(callToolResult.content());
    }

}
