## Mydada AI 智能答题平台

基于 Spring Boot + Redis + MySQL + ChatGLM AI  的 **AI 答题应用平台。**
用户可以基于 AI 快速制作并发布多种答题应用，支持检索和分享应用、在线答题并基于评分算法或 AI 得到回答总结；管理员可以审核应用、集中管理整站内容，并进行统计分析。

## 技术选型

- Java Spring Boot 开发框架
- 存储层：MySQL 数据库 + Redis 缓存 + 腾讯云 COS 对象存储
- MyBatis-Plus 及 MyBatis X 自动生成
- 基于 Redisson 的限流算法
- 基于 ChatGLM 大模型的通用 AI 能力
- 幂等设计 + 分布式 ID 雪花算法
- 多种设计模式
## 架构设计
![](https://cdn.nlark.com/yuque/0/2024/jpeg/42967483/1721202984205-d8947dc3-28e6-4210-9870-283b2c8e7177.jpeg)
