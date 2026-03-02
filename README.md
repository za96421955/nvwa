# Nvwa (女娲)

## JDK
```
17+
```

## 环境变量
```
// LLM API 接口地址
AI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1

// API Key
AI_API_KEY=XXXXXX

// 使用的模型
AI_MODEL=qwen-plus
```

## 编译运行
```
mvn clean package -DskipTests

java -jar target/nvwa.jar
```

## 基础命令
```
/sl 安全等级
0: 谨慎操作 (所有操作均需授权)
1: 文件只读 (除文件读取外，均需授权)
2: 风险控制 (系统判断有风险的操作, 需要授权)
3: 完全信任 (所有操作均无需授权, 全自动执行)
> /sl 0

---
/new 新会话
> /new

---
/wf 执行工作流
> /wf work_flow.txt
```

## 工作流 Demo
### 整理文件
```
/sl 3
/new
当前时间：2026年3月2日
文件目录：/Users/chenchen/Desktop/3，工作
1. 整理我 **2025年** hiot相关的工作文件，放到 "/Users/chenchen/Desktop/3，工作/2026" 目录下面
2. 梳理工作内容，了解清楚我去年的工作情况
3. 整理并输出为工作总结，word格式
```
### 编程
```
/sl 2
/new
项目目录：/Users/chenchen/Desktop/3，工作/item
在我明确可以操作前，不要做任何执行
1. 删除并重新从master拉取 feature/ai-code 分支
2. 根据 /Users/chenchen/Desktop/SDD.md 文档修改代码
3. 提交并推送 git，添加日志
```

## !!!
```
联网搜索工具目前固定使用的是 阿里千问API 及 qwen3.5-plus
https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
qwen3.5-plus
AI_API_KEY=XXXXXX
```





















