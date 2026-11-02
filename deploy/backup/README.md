# MySQL 备份与恢复（v2.2）

## 每日自动备份（Windows 计划任务）

1. 检查 `backup-mysql.bat` 顶部配置区（mysqldump 路径 / 密码 / 保留份数）；
2. 以管理员身份注册计划任务（每日 03:00）：

```bat
schtasks /Create /TN "BlogMySQLBackup" /SC DAILY /ST 03:00 /TR "D:\Alpaca\Workspace\Zcode_workspace\blog\deploy\backup\backup-mysql.bat" /RL LIMITED
```

删除任务：`schtasks /Delete /TN "BlogMySQLBackup" /F`

## 手动备份

双击 `backup-mysql.bat`，或命令行执行。产物：`blog/backups/blog-YYYYMMDD-HHmmss.sql`（含建库语句 `--databases`，自动保留最近 30 份）。

## 恢复

```bat
mysql -h localhost -u root -p < backups\blog-YYYYMMDD-HHmmss.sql
```

> dump 带 `--databases`（含 `CREATE DATABASE IF NOT EXISTS blog`），直接整库导入即可；如目标库已有数据，恢复会覆盖同名表。

## Docker Compose 部署形态（未启用，文档化）

```bash
# 手动备份（容器内 mysqldump 输出到挂载卷）
docker exec blog-mysql sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --databases blog' > backups\blog-compose.sql
# 恢复
docker exec -i blog-mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD"' < backups\blog-compose.sql
```

需要全自动时可在 compose 增加 cron 备份容器（如 `mysql-backup` 镜像），当前按 D4 决策暂不引入。

## 安全提醒

备份文件含全量用户数据（BCrypt 密码哈希等），`backups/` 已加入 `.gitignore`——**不要提交或上传到公开渠道**。
