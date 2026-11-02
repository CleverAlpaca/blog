@echo off
REM ============================================================
REM 个人博客系统 - MySQL 每日备份脚本 (v2.2)
REM 用法: 直接双击运行, 或注册计划任务 (见 deploy/backup/README.md)
REM 产出: backups\blog-YYYYMMDD-HHmmss.sql, 自动保留最近 30 份
REM ============================================================

REM ---- 配置区 (按实际环境修改) ----
set "MYSQLDUMP=D:\Program Files 64\MySQLMySQL Server 8.0\bin\mysqldump.exe"
set "MYSQL_HOST=localhost"
set "MYSQL_PORT=3306"
set "MYSQL_USER=root"
set "MYSQL_PASSWORD=123456"
set "DATABASE=blog"
set "KEEP=30"
REM 备份输出目录 (默认脚本同级 backups\)
set "OUT_DIR=%~dp0..\..\backups"

set "STAMP=%date:~0,4%%date:~5,2%%date:~8,2%-%time:~0,2%%time:~3,2%%time:~6,2%"
set "STAMP=%STAMP: =0%"
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

set "TARGET=%OUT_DIR%\blog-%STAMP%.sql"

"%MYSQLDUMP%" -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% --databases %DATABASE% --default-character-set=utf8mb4 --single-transaction --routines --triggers --set-gtid-purged=OFF > "%TARGET%"

if %errorlevel% neq 0 (
  echo [backup] 备份失败, 错误码 %errorlevel%
  del "%TARGET%" 2>nul
  exit /b 1
)

for %%F in ("%TARGET%") do set "SIZE=%%~zF"
echo [backup] 备份完成: %TARGET% (%SIZE% 字节)

REM ---- 清理: 仅保留最近 KEEP 份 ----
setlocal enabledelayedexpansion
set /a COUNT=0
for /f "delims=" %%F in ('dir /b /o-d "%OUT_DIR%\blog-*.sql" 2^>nul') do (
  set /a COUNT+=1
  if !COUNT! GTR %KEEP% (
    del "%OUT_DIR%\%%F"
    echo [backup] 清理过期备份: %%F
  )
)
echo [backup] 全部完成
