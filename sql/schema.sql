-- =============================================================
-- 个人博客系统 - 建库建表脚本
-- 适用数据库 : MySQL 8.x
-- 字符集     : utf8mb4（支持 emoji）
-- 存储引擎   : InnoDB
-- 执行方式   : mysql -u root -p < schema.sql
-- 说明       : 表结构与《docs/设计文档.md》第 5 章一一对应
-- =============================================================

CREATE DATABASE IF NOT EXISTS `blog`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE `blog`;

-- -------------------------------------------------------------
-- 1. 用户表（个人博客默认仅一名管理员，role 字段为多用户扩展预留）
-- -------------------------------------------------------------
CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`    VARCHAR(64)  NOT NULL                COMMENT '登录账号',
  `password`    VARCHAR(128) NOT NULL                COMMENT '密码（BCrypt 加密，$2a/$2b 前缀均可）',
  `nickname`    VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '昵称（站点展示名）',
  `avatar`      VARCHAR(512) NOT NULL DEFAULT ''     COMMENT '头像 URL',
  `email`       VARCHAR(128) NOT NULL DEFAULT ''     COMMENT '邮箱',
  `intro`       VARCHAR(512) NOT NULL DEFAULT ''     COMMENT '个人简介（关于页/侧栏展示）',
  `role`        VARCHAR(20)  NOT NULL DEFAULT 'ADMIN' COMMENT '角色：ADMIN 管理员 / USER 普通用户（预留）',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1 启用 / 0 禁用',
  `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0 未删 / 1 已删',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表';

-- -------------------------------------------------------------
-- 2. 文章表
-- -------------------------------------------------------------
CREATE TABLE `article` (
  `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `user_id`       BIGINT   NOT NULL                COMMENT '作者用户ID（关联 user.id）',
  `category_id`   BIGINT            DEFAULT NULL   COMMENT '分类ID（关联 category.id，可空）',
  `title`         VARCHAR(200) NOT NULL            COMMENT '文章标题',
  `summary`       VARCHAR(500) NOT NULL DEFAULT '' COMMENT '摘要（列表页与 SEO description 使用）',
  `cover_image`   VARCHAR(512) NOT NULL DEFAULT '' COMMENT '封面图 URL',
  `content`       LONGTEXT NOT NULL                COMMENT '正文（Markdown 原文）',
  `status`        TINYINT  NOT NULL DEFAULT 0      COMMENT '状态：0 草稿 / 1 已发布 / 2 已下架',
  `is_top`        TINYINT  NOT NULL DEFAULT 0      COMMENT '是否置顶：1 是 / 0 否',
  `views`         BIGINT   NOT NULL DEFAULT 0      COMMENT '浏览量（Redis 累加，定时任务批量落库）',
  `likes`         BIGINT   NOT NULL DEFAULT 0      COMMENT '点赞数（冗余计数，明细见 like_record）',
  `comment_count` INT      NOT NULL DEFAULT 0      COMMENT '评论数（冗余计数，列表页免统计查询）',
  `deleted`       TINYINT  NOT NULL DEFAULT 0      COMMENT '逻辑删除：0 未删 / 1 已删',
  `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（归档时间轴取此字段）',
  `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status_create_time` (`status`, `create_time`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_title` (`title`(100))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文章表';

-- -------------------------------------------------------------
-- 3. 分类表
-- -------------------------------------------------------------
CREATE TABLE `category` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name`        VARCHAR(50) NOT NULL                COMMENT '分类名称',
  `description` VARCHAR(255) NOT NULL DEFAULT ''    COMMENT '分类描述',
  `sort`        INT         NOT NULL DEFAULT 0      COMMENT '排序（越小越靠前）',
  `deleted`     TINYINT     NOT NULL DEFAULT 0      COMMENT '逻辑删除：0 未删 / 1 已删',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '分类表';

-- -------------------------------------------------------------
-- 4. 标签表
-- -------------------------------------------------------------
CREATE TABLE `tag` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name`        VARCHAR(50) NOT NULL                COMMENT '标签名称',
  `deleted`     TINYINT     NOT NULL DEFAULT 0      COMMENT '逻辑删除：0 未删 / 1 已删',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '标签表';

-- -------------------------------------------------------------
-- 5. 文章-标签关联表（多对多中间表）
-- -------------------------------------------------------------
CREATE TABLE `article_tag` (
  `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` BIGINT NOT NULL COMMENT '文章ID（关联 article.id）',
  `tag_id`     BIGINT NOT NULL COMMENT '标签ID（关联 tag.id）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文章-标签关联表';

-- -------------------------------------------------------------
-- 6. 评论表（文章评论，前台游客可评论）
-- -------------------------------------------------------------
CREATE TABLE `comment` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `article_id`  BIGINT        NOT NULL                COMMENT '所属文章ID（关联 article.id）',
  `user_id`     BIGINT                 DEFAULT NULL   COMMENT '评论用户ID（登录用户使用，游客为 NULL，预留）',
  `parent_id`   BIGINT                 DEFAULT NULL   COMMENT '父评论ID（顶级评论为 NULL，仅支持两级：评论 + 楼内回复）',
  `nickname`    VARCHAR(64)   NOT NULL DEFAULT ''     COMMENT '显示昵称（游客填写或取自用户表）',
  `email`       VARCHAR(128)  NOT NULL DEFAULT ''     COMMENT '邮箱（预留头像/邮件通知，选填）',
  `content`     VARCHAR(1000) NOT NULL                COMMENT '评论内容（纯文本）',
  `status`      TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：1 显示 / 0 隐藏（后台可隐藏不删除）',
  `ip`          VARCHAR(64)   NOT NULL DEFAULT ''     COMMENT '评论来源 IP（防刷与审计）',
  `deleted`     TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0 未删 / 1 已删',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文章评论表';

-- -------------------------------------------------------------
-- 7. 点赞记录表（防止重复点赞）
-- -------------------------------------------------------------
CREATE TABLE `like_record` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `target_type` TINYINT     NOT NULL DEFAULT 1      COMMENT '目标类型：1 文章（预留评论点赞等扩展）',
  `target_id`   BIGINT      NOT NULL                COMMENT '目标ID（如文章ID）',
  `visitor_id`  VARCHAR(64) NOT NULL                COMMENT '访客标识：登录用户取用户ID，游客取设备指纹（IP+UA 哈希）',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_target_visitor` (`target_type`, `target_id`, `visitor_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '点赞记录表';

-- -------------------------------------------------------------
-- 8. 友情链接表
-- -------------------------------------------------------------
CREATE TABLE `friend_link` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '友链ID',
  `name`        VARCHAR(50)  NOT NULL                COMMENT '站点名称',
  `logo`        VARCHAR(512) NOT NULL DEFAULT ''     COMMENT '站点图标 URL',
  `url`         VARCHAR(512) NOT NULL                COMMENT '站点地址',
  `description` VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '站点描述',
  `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序（越小越靠前）',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1 显示 / 0 隐藏',
  `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0 未删 / 1 已删',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '友情链接表';

-- -------------------------------------------------------------
-- 9. 留言板表（独立于文章评论的站点留言）
-- -------------------------------------------------------------
CREATE TABLE `message` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '留言ID',
  `user_id`     BIGINT                 DEFAULT NULL   COMMENT '留言用户ID（游客为 NULL，预留）',
  `parent_id`   BIGINT                 DEFAULT NULL   COMMENT '父留言ID（顶级留言为 NULL，仅支持两级）',
  `nickname`    VARCHAR(64)   NOT NULL DEFAULT ''     COMMENT '显示昵称',
  `email`       VARCHAR(128)  NOT NULL DEFAULT ''     COMMENT '邮箱（选填）',
  `content`     VARCHAR(1000) NOT NULL                COMMENT '留言内容（纯文本）',
  `status`      TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：1 显示 / 0 隐藏',
  `ip`          VARCHAR(64)   NOT NULL DEFAULT ''     COMMENT '留言来源 IP',
  `deleted`     TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0 未删 / 1 已删',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '留言板表';

-- -------------------------------------------------------------
-- 10. 站点配置表（键值对：站点名称/描述/公告/备案号等，后台可视化维护）
-- -------------------------------------------------------------
CREATE TABLE `site_config` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key`   VARCHAR(50)   NOT NULL                COMMENT '配置键（如 site_name/announcement/icp）',
  `config_value` VARCHAR(1000) NOT NULL DEFAULT ''     COMMENT '配置值',
  `description`  VARCHAR(255)  NOT NULL DEFAULT ''     COMMENT '配置说明（后台展示用）',
  `deleted`      TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0 未删 / 1 已删',
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '站点配置表';
