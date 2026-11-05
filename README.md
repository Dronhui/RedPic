# RedPic 红图 · 红色文化 AI 图像识别讲解系统

> 基于 Java 17 + Spring Boot 3.3.5 + Spring AI 1.0.0 打造的红色文旅智能导览系统。核心能力为红色老照片、革命遗址实拍图的自动识别与专业讲解，配套多轮对话、RAG 知识库检索等能力，以图像为核心载体，数字化传承红色文化。

## ✨ 核心特性

- **🧠 三模型混合编排，各取所长**：文本对话接入 **DeepSeek**（长上下文、高性价比）；向量检索接入 **智谱 Embedding-3**（中文史料语义优势）；视觉识别接入 **阿里千问 Qwen3-VL-Plus**（针对历史人物与老旧建筑识别专项优化）。通过 `ChatClient` 工厂模式与 `@Qualifier` 隔离，实现 Provider 无缝扩展。
- **🌊 真·SSE 流式输出**：拒绝假打字机！前后端全链路响应式编程（Spring WebFlux + `Flux` + 前端 `ReadableStream`）。大模型边生成边输出，首字延迟低至毫秒级，支持用户随时发送新消息打断上一轮对话（AbortController）。
- **🎯 大一统接口与八维业务路由**：将纯文本、多模态识图、RAG 查库合并为统一的 `/api/v1/chat/unified` 接口。前端通过勾选“角色（专家/普通）”与“结合本地史料库（RAG开关）”，后端智能编排 8 种业务分支，代码高度解耦。
- **🛡️ 三层防幻觉机制**：`检索层` RAG 退化为纯纯的查库工具；`Prompt 层` 强约束“涉及史实必须一字不差，禁止脑补编造机构与时间”；`输出层` 正则清洗与兜底话术。从根源抑制大模型在严肃历史题材中的幻觉问题。

## 📐 系统架构

```mermaid
graph TD
    %% 前端交互层
    Client[前端 · 云朵机器人 (流式接收/可打断)] -->|POST /api/v1/chat/unified| Controller[大一统 ChatController]
    
    %% 业务编排层
# RedPic 红图 · 红色文化 AI 图像识别讲解系统

> 基于 Java 17 + Spring Boot 3.3.5 + Spring AI 1.0.0 打造的红色文旅智能导览系统。核心能力为红色老照片、革命遗址实拍图的自动识别与专业讲解，配套多轮对话、RAG 知识库检索等能力，以图像为核心载体，数字化传承红色文化。

## ✨ 核心特性

- **🧠 三模型混合编排，各取所长**：文本对话接入 **DeepSeek**（长上下文、高性价比）；向量检索接入 **智谱 Embedding-3**（中文史料语义优势）；视觉识别接入 **阿里千问 Qwen3-VL-Plus**（针对历史人物与老旧建筑识别专项优化）。通过 `ChatClient` 工厂模式与 `@Qualifier` 隔离，实现 Provider 无缝扩展。
- **🌊 真·SSE 流式输出**：拒绝假打字机！前后端全链路响应式编程（Spring WebFlux + `Flux` + 前端 `ReadableStream`）。大模型边生成边输出，首字延迟低至毫秒级，支持用户随时发送新消息打断上一轮对话（AbortController）。
- **🎯 大一统接口与八维业务路由**：将纯文本、多模态识图、RAG 查库合并为统一的 `/api/v1/chat/unified` 接口。前端通过勾选“角色（专家/普通）”与“结合本地史料库（RAG开关）”，后端智能编排 8 种业务分支，代码高度解耦。
- **🛡️ 三层防幻觉机制**：`检索层` RAG 退化为纯纯的查库工具；`Prompt 层` 强约束“涉及史实必须一字不差，禁止脑补编造机构与时间”；`输出层` 正则清洗与兜底话术。从根源抑制大模型在严肃历史题材中的幻觉问题。

## 📐 系统架构

```mermaid
graph TD
    %% 前端交互层
    Client[前端 · 云朵机器人 (流式接收/可打断)] -->|POST /api/v1/chat/unified| Controller[大一统 ChatController]
    
    %% 业务编排层
    Controller --> Service[Unified ChatService]
    
    Service -->|1. 传图?| Vision[千问 Vision 识图]
    Vision -->|实体写入| Memory[(记忆上下文)]
    
    Service -->|2. 路由判断| Router{useRag ?}
    
    %% RAG 链路
    Router -->|Yes| Rewrite[Query Rewrite 意图重写]
    Rewrite -->|结合记忆| RagSearch[RagSearchService (纯查库工具)]
    RagSearch -->|向量检索| VectorStore[(SimpleVectorStore)]
    RagSearch -->|拼接资料与专家Prompt| LLM[DeepSeek 生成]
    
    %% 纯聊天链路
    Router -->|No| LLM
    
    %% 流式输出
    LLM -->|Flux String SSE| Client
```

## 💡 设计决策与难点突破

### 1. 视觉模型选型与三模型解耦
早期使用单一视觉模型时，对特定历史人物存在明显的“脸盲”现象。经过横向评测，最终将视觉模块迁移至**阿里千问 Qwen3-VL-Plus**。工程上，避免模型绑定，通过 `AiModelConfig` 统一装配 `ChatClient`，确保未来接入新模型只需修改配置，无需改动业务代码。

### 2. RAG 纯工具化与统一生成（解耦）
摒弃了传统 RAG “检索+生成”一把抓的黑盒模式。将 `RagSearchService` 降级为只负责“查库并返回纯文本”。生成环节上浮至大一统 `ChatService`，结合动态人设（专家/普通）拼装 SystemPrompt 统一调用大模型。既保证了人设的灵活切换，又让后续接入 SSE 流式输出变得极其简单。

### 3. Query Rewrite 解决跨模态指代消解
在多模态连续追问场景下，用户常使用代词（如“他有什么故事”）。系统在检索前，利用大模型结合会话记忆，将问题重写为独立问句（如“陈奇涵有什么故事”），确保向量检索的召回精度。

### 4. 原生 SSE 流式与打断机制
后端使用 Spring AI 的 `.stream().content()` 返回 `Flux<String>`，配合 `MediaType.TEXT_EVENT_STREAM_VALUE`。前端彻底抛弃 `setInterval` 假打字机，通过 `fetch` 的 `getReader()` 解析流式数据。同时引入 `AbortController`，当 AI 还在长篇大论时，用户随时可以发送新消息，瞬间掐断上一次网络请求，实现“用户至上”的交互体验。

## 🛠 技术栈

| 分类 | 技术选型 | 备注 |
| :--- | :--- | :--- |
| **语言 & 框架** | Java 17, Spring Boot 3.3.5 | 基础后端框架 |
| **AI 编排层** | Spring AI 1.0.0 | 统一模型抽象与编排 |
| **文本生成模型** | DeepSeek `deepseek-chat` | 长上下文、高性价比 |
| **视觉识别模型** | 阿里千问 `Qwen3-VL-Plus` | 老旧照片与历史人物专精 |
| **向量化模型** | 智谱 `embedding-3` | 中文史料语义优势 |
| **向量存储** | Spring AI `SimpleVectorStore` | 本地 JSON 文件快照持久化 |
| **流式传输** | Spring WebFlux `Flux` + SSE | 前端打字机效果支撑 |
| **前端** | 原生 HTML/CSS/JS (Trae VibeCoding) | 红色复古主题 / 可拖拽缩放 / 流式渲染 |

## 🚀 快速开始

### 本地运行

```bash
# 1. 克隆代码
git clone https://github.com/your-name/redpic.git && cd redpic

# 2. 配置环境
cp src/main/resources/application-example.yml src/main/resources/application.yml
# 填入各自的 API Key (DeepSeek / 智谱 / 阿里百炼 DashScope)

# 3. 启动服务 (默认 8080 端口)
./mvnw spring-boot:run
```

### 接口测试 (流式大一统接口)

```bash
curl -X POST http://localhost:8080/api/v1/chat/unified \
  -H "Content-Type: multipart/form-data" \
  -F "question=他有什么故事" \
  -F "useRag=true" \
  -F "type=expert" \
  -F "session=test-session-001" \
  -F "image=@chenqihan.jpg"
```

## 📂 项目结构

```text
redpic/
├── src/main/java/org/example/redpic/
│   ├── controller/        # REST 控制器 (大一统 ChatController)
│   ├── service/           # 业务接口定义
│   │   └── impl/          # Chat / RagSearch / Vision / Knowledge 实现
│   ├── config/            # ChatClient 工厂、Prompts 加载、内存配置
│   ├── common/            # 统一响应体 Result<T> (普通接口使用)
│   └── exception/         # 全局异常处理
├── src/main/resources/
│   ├── prompt/            # 外置 Prompt 模板 (.txt)
│   ├── static/            # 前端云朵机器人 (index.html)
│   ├── application.yml
└── vector_store.json      # 本地向量库快照文件
```

## 🗺 开发路线图

### ✅ 已完成
- [x] 基础接口规范与全局异常处理
- [x] 三模型（DeepSeek/智谱/千问）接入与解耦装配
- [x] RAG 知识库构建链路（解析、切片、向量化、持久化）
- [x] 大一统 ChatService 重构，支持 8 种业务组合路由
- [x] 前后端全链路真实 SSE 流式输出与打断机制
- [x] 前端交互升级：复古红色主题、可拖拽缩放、角色与 RAG 开关面板
- [x] 防幻觉 Prompt 约束与知识库兜底话术

### ⏳ 开发中 (D4 - D10)
- [ ] **用户鉴权**：引入 MySQL + MyBatis-Plus + Sa-Token 登录验证。
- [ ] **持久化记忆**：替换 InMemory，落地 Redis + MySQL 方案。
- [ ] **RAG 评测**：建立人工标注集，统计不同 TopK 下的召回率指标。
- [ ] **一键部署**：完成 Docker Compose 编排并部署至云服务器。
- [ ] 接口安全：限流与敏感词过滤。