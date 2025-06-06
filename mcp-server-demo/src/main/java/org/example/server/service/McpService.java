package org.example.server.service;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class McpService {
    private final WebMvcSseServerTransportProvider provider;
    private McpSyncServer syncServer;

    @PostConstruct
    public void start() {
        syncServer = McpServer.sync(provider)
                .serverInfo("my-mcp-server", "1.0.0")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(true)
                        .logging()
                        .build())
                .build();
        addTool();
    }

    public void addTool() {
        String schema = "{\"type\":\"object\",\"id\":\"urn:jsonschema:Operation\",\"properties\":{\"name\":{\"type\":\"string\"}}}";
        McpServerFeatures.SyncToolSpecification syncToolSpecification = new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("searchPerson", "查询三国人物资料", schema),
                (exchange, arguments) -> {
                    List<McpSchema.Content> result = new ArrayList<>();

                    try {
                        String name = (String) arguments.get("name");

                        if (name == null || name.isEmpty()) {
                            result.add(new McpSchema.TextContent("错误：人物姓名不能为空"));
                            return new McpSchema.CallToolResult(result, true);
                        }

                        // 直接使用返回的格式化字符串
                        String personInfo = "典韦以超凡勇力、舍身护主的忠烈形象载入史册，其宛城之战的结局成为三国史上最动人的悲歌之一";
                        if (personInfo != null) {
                            result.add(new McpSchema.TextContent(personInfo));
                        }
                    } catch (Exception e) {
                        result.add(new McpSchema.TextContent("搜索错误: " + e.getMessage()));
                        return new McpSchema.CallToolResult(result, true);
                    }

                    return new McpSchema.CallToolResult(result, false);
                });
        syncServer.addTool(syncToolSpecification);
    }

}
