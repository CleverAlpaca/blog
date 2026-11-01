#!/bin/bash
# MySQL 容器首次启动时的库表初始化：按正确顺序执行建表与种子数据
# （docker-entrypoint-initdb.d 按文件名字母序执行，直接挂载 sql/ 会让 data.sql 先于 schema.sql 跑）
set -e

mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < /sql/schema.sql
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < /sql/data.sql

echo "[init] schema + data imported into ${MYSQL_DATABASE}"
