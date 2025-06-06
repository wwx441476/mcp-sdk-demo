 ⚠️ 故障排除指南

本文档列出了用户在使用mcp sdk demo时遇到的常见问题以及如何解决这些问题。


## 1. 检查依赖

- 首先要确保使用的是SpringBoot 2和JDK8，才能保证与的Java SDK兼容。
- maven版本我使用的是3.6.1，否则可能会产生编译失败的问题。
  - Idea中Maven推荐使用 (Maven 3) --版本3.6.1
    - 正在导入 -> 导入程序的JDK -> 选择JDK8
    - 运行程序 -> JRE -> 选择JDK8
    - 项目结构 -> 项目 -> SDK -> 选择JDK8
    - 这里吐槽一下Idea里面需要在多个地方配置JDK8，不太友好

## 2. Port 12000 Fails to Start

**错误:**  
`Port 12000 failed to start` or `Address already in use`

**解决方案:**  
- Make sure port 12000 is not being used by another application.
- You can change the port with `application.yml server.port <another_port>`.

## 3. 启动顺序
- 在maven工作区，执行clean、package指令
- 运行和调试点击启动McpServerApplication
- 运行StdioClientMcpTest测试类
- 运行HttpClientSseMcpTest测试类
- 运行ChatTest测试类

