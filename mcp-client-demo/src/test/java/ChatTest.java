import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ChatTest {
    public static final String SYSTEM_PROMPT =
            "# 角色\n" +
                    "  你是一名合格的个人助理，擅长将回答用户问话并帮助其解决问题。\n" +
                    "## 任务\n" +
                    "1. 可选工具如下： ${{availableTools}}\n" +
                    "2. 根据用户 #用户问话# 和提供的 #可选工具# ，并从可选工具中选择最合适的工具\n" +
                    "## 限制\n" +
                    "- 用json格式输出，并且只返回json数据，不返回多余的文字内容，且仅输出 #可选工具# 中存在的内容。\n" +
                    "- 返回的信息中添加扩展字段mcpFlag，true代表命中工具，false代表没有命中工具。\n" +
                    "- 如果命中工具，额外添加扩展字段name，代表工具名称，并且添加扩展字段arguments，代表调用参数";

    public static void main(String[] args) {
        McpSyncClient mcpSyncClient = getMcpSyncClient();
        String url = "http://10.16.28.76:8067/ai-adapter/rest/v1/completions";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-type", "application/json;charset=UTF-8");
        httpPost.setHeader("Accept", "application/json");
        JSONObject json = new JSONObject();
        json.put("service_code", "LLM_CHAT");
        json.put("model", "qwen2.5-32b-instruct");
        JSONArray messages = new JSONArray();
        JSONObject sysMessage = new JSONObject();
        sysMessage.put("role", "system");
        String realSystemPrompt = SYSTEM_PROMPT.replace("${{availableTools}}", getTools(mcpSyncClient));
        sysMessage.put("content", realSystemPrompt);
        messages.add(sysMessage);
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", "帮我介绍下三国演义中的典韦");
        messages.add(userMessage);
        json.put("messages", messages);
        json.put("top_p", 1);
        json.put("frequency_penalty", 0.0);
        json.put("max_tokens", 4096);
        json.put("stream", false);
        json.put("presence_penalty", 0.0);
        json.put("temperature", 0.0);
        StringEntity entity = new StringEntity(json.toJSONString(), "UTF-8");
        entity.setContentType("application/json;charset=UTF-8");
        httpPost.setEntity(entity);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                System.out.println(content);
                JSONObject jsonObject = JSONObject.parseObject(content);
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray choices = data.getJSONArray("choices");
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                JSONObject realContent = message.getJSONObject("content");
                Boolean mcpFlag = realContent.getBoolean("mcpFlag");
                if (mcpFlag) {
                    String name = realContent.getString("name");
                    Map<String, Object> arguments = realContent.getJSONObject("arguments");
                    McpSchema.CallToolResult callToolResult = callTool(mcpSyncClient, name, arguments);
                    System.out.println(callToolResult.content());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static McpSchema.CallToolResult callTool(McpSyncClient client, String name, Map<String, Object> arguments) {
        McpSchema.CallToolRequest callToolRequest = new McpSchema.CallToolRequest(
                name,
                arguments
        );
        return client.callTool(callToolRequest);
    }

    private static String getTools(McpSyncClient client) {
        McpSchema.ListToolsResult listToolsResult = client.listTools();
        List<McpSchema.Tool> tools = listToolsResult.tools();
        return JSON.toJSONString(tools);
    }

    private static McpSyncClient getMcpSyncClient() {
        McpClientTransport transport = HttpClientSseClientTransport
                .builder("http://localhost:12000")
                .sseEndpoint("/mcp/sse")
                .objectMapper(new ObjectMapper())
                .build();

        McpSyncClient client = McpClient.sync(transport).clientInfo(new McpSchema.Implementation("my-mcp-client", "1.0.0"))
                .capabilities(McpSchema.ClientCapabilities.builder().roots(true).sampling().build())
                .requestTimeout(Duration.ofSeconds(60)).build();
        client.initialize();
        return client;
    }

}
