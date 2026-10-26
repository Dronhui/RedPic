# RedPic 红图
基于 Spring Boot 3 + Spring AI + DeepSeek 的红色文旅 AI 对话助手

## ✨ 已完成功能
- 基础 AI 对话接口 `POST /api/v1/chat`
- 统一返回体 Result 封装
- 请求参数校验（@Valid + @NotBlank）
- 全局异常处理器（400/500 统一兜底）

## 🛠 技术栈
- JDK 17
- Spring Boot 3.2.x
- Spring AI
- DeepSeek 大模型

## 🚀 启动说明
1. 在 `src/main/resources` 目录下，复制 `application-example.yml` 并重命名为 `application.yml`
2. 在 `application.yml` 中填入自己的 DeepSeek API-Key
3. 启动项目，使用 Postman 等工具发送 POST 请求测试

## 📡 接口示例
**请求地址**：`POST http://localhost:8080/api/v1/chat`

**请求体**：
```json
{
  "message": "你好"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": "你好呀！"
}
```
