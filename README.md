# Nvwa
- Brain: enter, control
- Agent: Reasoning (Loop), Tool(Extension, Function, DataStore), Model

## 企业级AI应用"六层架构"
- 基础设施
- 数据
- 模型
- 智能体
- 能力
- 应用

## Agent架构
- 数据：清洗、分片、索引
- Prompt：定义、格式
- Agent：RAG、工具调用、多轮对话，自动化执行任务，复杂流程编排
- LLM：适配

- memory：记录交互历史、迭代、优化（私人记忆）
- RAG：减少幻觉
- Tool Integration：外部调用，"检索-执行-反馈"闭环，返回String
- 任务分解：请求拆分多个任务，并行/串行执行
- 多模态：文本、图像、音频、视频
- ONNX：机器学习模型文件格式（PyTorch、TensorFlow、MXNet等）

# Agent（代理）
- 模型、工具、编排
- 模型：大模型集成，推理/逻辑框架（？）
- 工具：现实世界增强，API、RAG
- 编排：记忆、状态、推理、规划
-- Prompt指导推理、规划
-- 提示工程框架
-- 任务规划领域
  
* ReAct：即时工程框架，为LLM提供推理思维过程策略，对用户查询采取行动，ReAct优于SOTA基线
* CoT：思维链，快速工程框架，通过中间步骤实现推理能力
  -- 子技术：self-consistency（自洽）、active-prompt（主动提示，目前效果差于人工提示）、multimodal CoT（多模态CoT），每种技术都有优缺点、具体取决于具体应用
* ToT：思维树，非常适合探索或战略前瞻性任务的提示工程框架，涵盖CoT技术，允许模型探索各种CoT
  -- ToT -> CoT 作为LLM解决一般问题的中间步骤

## ReAct
- a. 问题：用户输入，随Prompt提供
- b. 思考：LLM对下一步应该怎么做的想法
- c. 行动：LLM决定下一步要做什么
-- 1、这里是可以选择工具的地方
-- 2、例：动作可以是 flight、search、code、无（不使用工具）
- d. 输入：LLM决定向工具提供哪些输入（如果有）
- e. 观察：动作/动作输入的结果
- f. 最终答案：LLM给用户的最终答案
* [b-e] 思考 -> 行动 -> 输入 -> 观察，根据需要可重复n次

∴ {
    问题
    -> {
        匹配Agent(?)
    }
    -> {
        任务拆分
        -> [{
            匹配Tool(?)
            -> {
                LLM
                -> {
                    思考
                    -> 行动
                    -> {
                        提取输入
                        -> 工具调用
                        -> 响应结果
                    }
                }
                -> 观察(结果/进一步LLM)
            }
            -> 结果
        }]
        -> 结果整合
    }
    -> 最终结果
}

### ReAct
1. 一级：Prompt + Tools + Examples
2. 二级：DataStores + Tools + Examples
3. 三级：垂直LLM（领域知识训练、更专业的静态库）
* 根据实际情况，应用于Agent框架
* 利用各种优势，最大限度地减少它们的劣势
* 允许更健壮，适应性更强的解决方案

### CoT思维链
Prompt：指令（Instruction），逻辑依据（Rationale），示例（Examples）
1. 指令：描述问题，告知大模型输出格式
2. 逻辑依据：CoT中间推理过程，包含问题的解决方案，中间推理步骤，问题相关的外部知识
3. 示例：以少量样本的方式为大模型提供正确输出的基本格式，每个示例都包括 问题、推理过程、答案
* Zero-Shot（0样本）：仅在指令中添加"Let's think step to step"(一步一步的思考)，唤醒LLM的推理能力 
* Few-Shot（少样本）：在示例中详细描述"解题步骤"，让LLM照猫画虎

* CoT对简单任务无效，反而会导致思维僵化
* 适用于数学、编程、金融等领域

### ToT思维树
1. 广度优先搜索（BFS）
2. 深度优先搜索（DFS）
* 多路径维护/回溯
* 适用于处理不确定性、量化多路径风险、战略规划类任务

## Tool（工具）
- google主要工具种类：Extension（扩展）、Function（函数）、Data Store（数据存储）

### Extension
Agent --- ? --- API
- 自定义编码不可扩展，边缘情况会使代码非常复杂，很容易出现异常导致场景中断
1. 通过示例教Agent如何使用API
2. 教Agent成功调用API需要哪些入参或参数
* Extension可以独立于Agent制作，但应该作为Agent配置的一部分
* Agent在运行时使用模型和示例来决定使用哪个扩展来解决用户的问题
* 通过Extension的内置Example，支持Agent为任务动态选择

User <-> UI/中间件 <-> Agent <-> Extension <-> API

∴ 问题 -> Agent -> Extension -> LLM -> API -> 答案

### Function
Agent --- Function  ?  API
1. 模型通过入参调用函数，但不进行实时API调用
2. 函数在客户端执行，Extension在云端执行
* 这样可以为开发人员提供更精细的数据流控制

#### 使用Function而不使用Extension的常见原因
1. API调用在APP堆栈的另一层进行，在Agent架构流程之外
   e.g. 中间件系统，前端框架等
2. 防止Agent直接调用API的安全或身份验证限制
   e.g. API不在公网暴露，Agent无法访问到API等
3. API禁止Agent实时调用或操作顺序限制
   e.g. 批处理操作，人工循环审查等
4. 需要额外的数据转换逻辑，API的响应Agent无法执行
   e.g. API没有返回结果数量限制；Function可以为开发人员提供额外的数据转换机会
5. 开发人员希望不为API部署额外的服务，同时迭代Agent开发
   e.g. Function调用可以像API的"stubbing"

User <-> UI/中间件 <-> Agent <-> Function     API
            ↑                                 ↑
            -----------------------------------

∴ 问题 -> client -> Agent(Prompt + Example) -> LLM(生成示例格式) -> Agent -> client(参数解析) -> API -> client(获取响应)
1. 希望LLM建议使用函数（函数调用不运行函数）
2. 正在运行可能需要几秒钟以上的异步操作，这样的场景适用于Function
3. 希望Function调用和参数生成运行在不同的设备上

### DataStore
Agent --- ? --- 私人文档/网站/结构化数据
LLM：静态数据
DataStore：动态/实时数据

模型知识扩展：
- 网站内容
- pdf、word、csv、excel等格式的结构化数据
- html、pdf、txt等非结构化数据

User Query --(1)--> Embedding ---------(2)---------
    |                                             |
    ↓                                             ↓
  Agent <--(4)-- Retrieved Content <--(3)-- Vector Database
    |
   (5)
    ↓
 Response

1. 将用户查询发送到嵌入模型，未查询生成嵌入
2. 使用SCaNN等匹配算法将查询嵌入与向量库的内容进行匹配
3. 以text格式将向量库中检索匹配的内容发回Agent
4. Agent接受用户问题和向量库匹配内容，然后执行响应或动作
5. 向用户发送最终响应























