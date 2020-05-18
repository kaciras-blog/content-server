/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `access_log`
(
    `ip`         binary(16)           NOT NULL,
    `method`     tinytext         DEFAULT NULL,
    `path`       text                 NOT NULL,
    `params`     text             DEFAULT NULL,
    `user_agent` tinytext         DEFAULT NULL,
    `length`     int(10) unsigned DEFAULT NULL,
    `status`     smallint(5) unsigned NOT NULL,
    `time`       datetime(6)          NOT NULL,
    `delay`      smallint(5) unsigned NOT NULL
) ENGINE = Aria
  DEFAULT CHARSET = utf8mb4 PAGE_CHECKSUM=1;

CREATE TABLE IF NOT EXISTS `account`
(
    `id`       int(10) unsigned NOT NULL,
    `name`     varchar(16)      NOT NULL COMMENT '登录名不一定与用户名相同',
    `password` binary(64)       NOT NULL,
    `salt`     binary(64)       NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='本地账户系统';

CREATE TABLE IF NOT EXISTS `article`
(
    `id`          int(10) unsigned     NOT NULL AUTO_INCREMENT,
    `category`    smallint(5) unsigned NOT NULL DEFAULT 0,
    `title`       tinytext             NOT NULL,
    `url_title`   tinytext             NOT NULL,
    `cover`       binary(33)           NOT NULL,
    `summary`     tinytext             NOT NULL DEFAULT '',
    `deleted`     bit(1)               NOT NULL DEFAULT b'0',
    `update_time` datetime(6)          NOT NULL DEFAULT current_timestamp(6) COMMENT '不要自动更新',
    `create_time` datetime(6)          NOT NULL DEFAULT current_timestamp(6),
    `view_count`  int(10) unsigned     NOT NULL DEFAULT 0,
    `content`     mediumtext           NOT NULL,
    PRIMARY KEY (`id`),
    KEY `category` (`category`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `category`
(
    `id`          smallint(5) unsigned NOT NULL AUTO_INCREMENT,
    `name`        varchar(32)          NOT NULL,
    `cover`       binary(33)                    DEFAULT NULL,
    `description` text                 NOT NULL,
    `background`  binary(33)                    DEFAULT NULL,
    `theme`       tinyint(4)           NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `category_tree`
(
    `ancestor`   smallint(5) unsigned NOT NULL,
    `descendant` smallint(5) unsigned NOT NULL,
    `distance`   tinyint(3) unsigned  NOT NULL,
    PRIMARY KEY (`descendant`, `ancestor`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `discussion`
(
    `id`        int(10) unsigned    NOT NULL AUTO_INCREMENT,
    `object_id` int(10) unsigned    NOT NULL,
    `type`      tinyint(3) unsigned NOT NULL DEFAULT 0,
    `floor`     int(10) unsigned    NOT NULL,
    `parent`    int(10) unsigned    NOT NULL DEFAULT 0,
    `user_id`   int(10) unsigned    NOT NULL,
    `nickname`  varchar(16)                  DEFAULT NULL,
    `content`   text                NOT NULL,
    `time`      datetime(6)         NOT NULL,
    `state`     tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '0-正常 1-删除 2-待审',
    `vote`      int(10)             NOT NULL DEFAULT 0 COMMENT '冗余，排序用',
    `address`   binary(16)          NOT NULL,
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`),
    KEY `parent` (`parent`),
    KEY `object_id_type` (`object_id`, `type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `discussion_vote`
(
    `id`      int(10) unsigned NOT NULL,
    `address` binary(16)       NOT NULL,
    PRIMARY KEY (`id`, `address`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `draft`
(
    `id`         int(11) unsigned NOT NULL AUTO_INCREMENT,
    `user_id`    int(11) unsigned NOT NULL,
    `article_id` int(11) unsigned          DEFAULT NULL,
    `time`       datetime(6)      NOT NULL DEFAULT current_timestamp(6),
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `draft_history`
(
    `id`         int(10) unsigned NOT NULL AUTO_INCREMENT,
    `save_count` int(10) unsigned NOT NULL,
    `title`      tinytext         NOT NULL DEFAULT '',
    `summary`    text             NOT NULL DEFAULT '',
    `cover`      binary(33)                DEFAULT NULL,
    `keywords`   tinytext         NOT NULL DEFAULT '',
    `content`    mediumtext       NOT NULL DEFAULT '',
    `time`       datetime(6)      NOT NULL DEFAULT current_timestamp(6) ON UPDATE current_timestamp(6),
    PRIMARY KEY (`id`, `save_count`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `keyword`
(
    `id`    int(10) unsigned NOT NULL,
    `value` varchar(64)      NOT NULL,
    PRIMARY KEY (`id`, `value`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `oauth`
(
    `oauth_id` varchar(50)         NOT NULL,
    `type`     tinyint(3) unsigned NOT NULL,
    `local_id` int(10) unsigned    NOT NULL,
    PRIMARY KEY (`oauth_id`, `type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='注册IP统一记录在user表';

CREATE TABLE IF NOT EXISTS `user`
(
    `id`       int(10) unsigned    NOT NULL AUTO_INCREMENT,
    `name`     varchar(16)         NOT NULL COMMENT '显示名可以重复',
    `avatar`   binary(33)          NOT NULL,
    `deleted`  bit(1)              NOT NULL DEFAULT b'0',
    `auth`     tinyint(3) unsigned NOT NULL,
    `reg_time` datetime(6)         NOT NULL DEFAULT current_timestamp(6),
    `reg_ip`   binary(16)          NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = Aria
  DEFAULT CHARSET = utf8mb4 PAGE_CHECKSUM=1;

/*!40101 SET SQL_MODE = IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS = IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
