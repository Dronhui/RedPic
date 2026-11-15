# 前端功能展示 · RedPic

> 本页展示 RedPic 系统的核心前端交互能力，涵盖登录鉴权、3D 数字书架、多轮记忆隔离、RAG 史料出处展示、官方原文查证与防幻觉拒答六大场景。

---

## 功能总览

| 序号 | 功能模块 | 核心能力 | 技术支撑 |
| :---: | :--- | :--- | :--- |
| 1 | Sa-Token 鉴权登录 | 用户名/密码登录、Token 下发、未登录拦截 | Sa-Token 1.38 + BCrypt |
| 2 | 3D 数字书架主界面 | 复古红色主题、书架式导航、对话入口 | 原生 HTML/CSS/JS |
| 3 | 多轮记忆隔离 | 会话级历史留存、指代消解、上下文不串台 | Redis + MessageWindowChatMemory |
| 4 | RAG 史料出处展示 | 回答末尾附史料出处（来源标签） | Spring AI VectorStore |
| 5 | 官方原文查证 | 通过数字书架跳转史料原文 | 本地私有史料库 |
| 6 | 防幻觉拒答 | 无史料命中时输出兜底话术以降低幻觉风险 | 阈值过滤 + Prompt 约束 |

---

## 1. Sa-Token 鉴权登录

<div align="center">
<img src="01_login.png" alt="Sa-Token 鉴权登录" width="700" />
<br><b>图 1 · Sa-Token 鉴权 —— 登录注册与 Token 下发，未登录请求被拦截器拦截</b>
</div>

- 登录成功后由 `StpUtil.login(userId)` 生成 Token 返回前端
- 后续请求通过 `satoken: <token>` 请求头携带身份
- `SaTokenConfig` 拦截 `/api/**`，白名单放行 login/register/logout
- 密码采用 `BCrypt.hashpw` 哈希存储，改密即登出

---

## 2. 3D 数字书架主界面

<div align="center">
<img src="02_main_ui.png" alt="3D 数字书架主界面" width="700" />
<br><b>图 2 · 3D 数字书架主界面 —— 复古红色主题承载交互入口，支持文字与图片双模态提问</b>
</div>

- 主界面采用 3D 立体书架设计，红色文化主题视觉
- 支持纯文本提问与上传老照片/遗址图像识别两种入口
- 流式打字机输出，支持前端 `AbortController` 打断

---

## 3. 多轮记忆隔离

<div align="center">
<img src="03_session_history.png" alt="多轮记忆隔离" width="700" />
<br><b>图 3 · 多轮记忆隔离 —— 会话级 Redis Key 隔离，支持指代消解，主对话上下文不被重写链路污染</b>
</div>

- 每个会话对应独立 Redis Key `redpic:chat:memory:{sessionId}`
- `MessageWindowChatMemory(maxMessages=10)` 滑动窗口控制上下文长度
- rewrite 链路使用无记忆 `rewriteChatClient`，手动只读拼历史，零副作用

---

## 4. RAG 史料出处展示

<div align="center">
<img src="04_rag_traceability.png" alt="RAG 史料出处展示" width="700" />
<br><b>图 4 · RAG 史料出处展示 —— 回答末尾附史料出处文本，有据可查</b>
</div>

- 每个回答末尾附带史料来源（由 Prompt 要求模型提取【来源：xxx】标签生成）
- 相似度分数在后端控制台日志中输出，用于调参与可观测性，前端不直接展示
- 三路 VectorStore 物理隔离，动态配额分配确保各意图召回均衡

---

## 5. 官方原文查证

<div align="center">
<img src="05_source_document.png" alt="官方原文查证" width="700" />
<br><b>图 5 · 官方原文查证 —— 通过数字书架跳转史料原文</b>
</div>

- 史料全部来自本地私有官方史料库，不依赖互联网泛化数据
- 用户可通过"数字书架"功能查看完整原文，验证回答准确性
- 物理隔离设计让大模型摆脱互联网垃圾信息干扰

---

## 6. 防幻觉拒答

<div align="center">
<img src="06_anti_hallucination.png" alt="防幻觉拒答" width="700" />
<br><b>图 6 · 防幻觉拒答 —— 知识库未命中时输出兜底话术以降低幻觉风险</b>
</div>

- 检索层：相似度阈值 0.48 过滤，空结果返回空串
- Prompt 层：System Prompt 约束"只能用参考资料作答"
- 输出层：空结果注入固定兜底话术，提示模型原样输出

---

> **相关文档**：
> - 🔙 [返回主 README](../../README.md)
> - 🧠 [多轮记忆与指代消解](../memory/memory.md)
> - 🛡️ [RAG 动态路由测试](../logs/logs.md)
> - 🔐 [接口安全测试](../api/api.md)