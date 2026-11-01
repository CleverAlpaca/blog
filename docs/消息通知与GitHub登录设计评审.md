# 消息通知与第三方登录 · 设计评审方案（v2.1）

| 项 | 内容 |
| --- | --- |
| 版本 | v1.0（评审稿）→ **v1.1 已评审通过并实现** |
| 日期 | 2026-11-02 |
| 状态 | **已评审通过（拍板：D1=A 两项都做；D2=A 仅审核结果通知；D3=B 用户级开关；D4=B 自动注册；D5=B noreply 占位+提示换绑）** |
| 关联 | [邮件服务设计评审](邮件服务设计评审.md)（邮件通道已就绪）、[设计文档](设计文档.md) 附录 B |

---

## 1. 目标与边界

### 1.1 目标

把 v2.0 打通的邮件通道用起来（**事件通知**），并接入 **GitHub 第三方登录**降低注册门槛。

### 1.2 本期边界（做什么 / 不做什么）

| 做 | 不做（后续迭代） |
| --- | --- |
| 邮件通知：投稿审核结果（通过/退回）通知投稿者 | 评论被回复通知（D2 拍板收窄：本期不做） |
| 用户级邮件通知开关（个人设置页） | 站内通知中心（需新表+消息中心页） |
| GitHub OAuth 登录（本地 localhost 回调可行） | 评论通知博主（顶级评论邮件提醒） |
| GitHub 自动注册 + 已登录账号绑定 | 邮件聚合/摘要（每日一封） |
| 账号解绑 GitHub（本期只做绑定与登录） | 微信/QQ 登录（需企业资质） |
| ES 全文检索：**维持不做**（数据量小，FULLTEXT 够用，见 §7） | |
| SSR：**维持不做**（SPA 架构迁移成本远大于收益，v1.3 已论证） | |

---

## 2. 决策点（待拍板）

| # | 决策点 | 选项 | 结论 |
| --- | --- | --- | --- |
| D1 | 本期范围 | A 邮件通知 + GitHub 登录 / B 仅邮件通知 / C 仅 GitHub 登录 | **A**：两者互补（一个用活邮件通道、一个降注册门槛），互不阻塞可并行 |
| D2 | 通知事件范围 | A 仅审核结果 / B 审核结果 + 评论被回复 / C 再加博主被评论提醒 | **A（评审收窄）**：仅审核结果通知投稿者；评论通知类全部后置 |
| D3 | 通知开关粒度 | A 全局开关（site_config）/ B 用户级开关（user.email_notify） / C 双层 | **B**：一人一布尔，设置页直接出开关；全局需求出现再加一层不冲突 |
| D4 | GitHub 登录账号策略 | A 仅绑定已注册账号 / B 自动注册（GitHub 昵称+邮箱，绕过验证码）/ C 双轨 | **B**：注册邮箱必填原则由 GitHub 邮箱承接；`user.github_id` 唯一键幂等 |
| D5 | GitHub 拿不到邮箱时（用户开启"保持邮箱私密"） | A 拒绝登录 / B 用 GitHub noreply 占位邮箱注册，提示用户去设置页换绑真实邮箱 | **B**：noreply 邮箱**不可收信**，找回密码对该账号不可用——注册成功 toast 与设置页顶部显式提示 |

---

## 3. 表结构变更（user 表 2 处，不新增表）

| 变更 | 说明 |
| --- | --- |
| `github_id BIGINT NULL` + 唯一键 `uk_github_id` | GitHub 账号绑定；NULL 表示未绑定（MySQL 唯一键对 NULL 不生效，允许多个未绑定用户） |
| `email_notify TINYINT NOT NULL DEFAULT 1` | 邮件通知开关（1 开 / 0 关） |

> 总表数维持 10 张。

---

## 4. API 设计

### 4.1 GitHub 登录（`/api/front/auth` 组扩展）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | /github/authorize | 生成一次性 state（Redis 5 分钟）→ 返回 GitHub 授权页 URL（前台 `window.location` 跳转） |
| POST | /github/callback | 前台落地页提交 `{code, state}`：校验 state（一次性，校验即删）→ code 换 access_token → 拉用户与邮箱 → 按 D4 注册/绑定/登录 → 与统一登录同构返回 `{token, role, user}` |

> 实现时调整（OAuth 回调走前台落地）：GitHub OAuth App 的 Authorization callback URL 配**前台路由** `http://localhost:5173/auth/github`；GitHub 携 code/state 回前台，落地页 POST 给后端完成换取——token 不出现在 URL，比 302 直传更干净。

- 配置：`blog.oauth.github.client-id / client-secret / redirect-uri`（secret 只在后端，支持环境变量注入，不入库）；
- 前台：登录页加「GitHub 登录」按钮 → 跳 authorize；新增 `/auth/github` 落地路由接收 token 并写入会话。

### 4.2 邮件通知（后端内部触发 + 1 个开关接口）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| PUT | /auth/notify-switch | `{enabled}` 已登录用户开关邮件通知（个人设置页） |

触发点（仅审核结果，D2 拍板；均检查：收件人开启开关 + 有邮箱 + 非本人操作）：
1. **审核通过**：`approve` → 通知投稿作者「已通过审核并发布」+ 文章链接；
2. **审核退回**：`reject` → 通知投稿作者「已退回」+ 退回原因 + 我的投稿入口；

> 通知发送失败只记日志，不影响审核主流程（try/catch 隔离）。

---

## 5. 安全设计

1. **OAuth state**：Redis 一次性（5 分钟 TTL，校验即删），防 CSRF 与重放；
2. **secret 不出后端**：code 换 token 在服务端完成；client-secret 环境变量注入；
3. **邮箱枚举**：GitHub 邮箱与已有账号冲突时按 D4=C 语义绑定而非报错暴露；
4. **通知不打敏感信息**：邮件只含标题/链接/原因，不含正文全文与密码类信息；
5. 既有管线沿用：BCrypt、限频、防枚举。

## 6. 兼容与迁移

- 存量账号不受影响（github_id/email_notify 默认值即可）；Guest 浏览/评论行为零变化；
- 已发布的 v2.0 注册流程不变，GitHub 注册是**旁路**（同样走 email 唯一约束）；
- 部署：compose `.env` 增加 `GITHUB_CLIENT_ID/SECRET`（不配置则页头不展示 GitHub 入口）。

## 7. ES / SSR 维持不做的结论

- **ES**：当前文章量级（个位数~两位数）+ FULLTEXT ngram 检索质量已达标；引入 ES 带来 JVM 常驻内存 ≥1GB 与运维成本，收益比极低。触发条件：文章数上万或多维度检索需求出现时再立项；
- **SSR**：v1.3 已论证（SPA + 动态 meta/sitemap 已覆盖 SEO 基本盘）；迁移成本=三个工程重构，收益=首屏与爬虫体验边际改善。维持不做。

## 8. 里程碑与工作量

| 里程碑 | 内容 | 预估（业余时间） |
| --- | --- | --- |
| M-A 邮件通知 | user 两字段迁移 + 两个触发点 + 去重 + 设置页开关 | 1 天 |
| M-B GitHub 登录 | OAuth 全流程 + 自动注册/绑定 + 登录页入口 + 落地路由 | 1~1.5 天 |
| M-C 回归收尾 | 全量回归 + 验收 + 文档同步 | 0.5 天 |

## 9. 涉及文件预估

后端约 12 个（user 实体/迁移、GithubProperties、GithubAuthService、2 个 Controller 端点、通知服务、审核/评论服务埋点、配置）；前台约 5 处（登录页 GitHub 按钮、OAuth 落地路由、设置页开关+绑定入口、api 扩展）；admin 0；SQL：ALTER user 2 列 + schema.sql 同步。

## 10. 需要你提供的

1. GitHub OAuth App 的 **Client ID / Client Secret**（创建入口：GitHub → Settings → Developer settings → OAuth Apps；Homepage 填 `http://localhost:5173`，**Authorization callback URL 填 `http://localhost:5173/auth/github`（前台落地路由）**）——没有也能先实现，用占位配置跑通非 OAuth 部分；
2. 无其他前置。
