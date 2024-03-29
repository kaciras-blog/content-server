/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

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
    `cover`       binary(16)                    DEFAULT NULL,
    `summary`     tinytext             NOT NULL DEFAULT '',
    `deleted`     bit(1)               NOT NULL DEFAULT b'0',
    `update_time` datetime(6)          NOT NULL DEFAULT current_timestamp(6),
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
    `cover`       binary(16)                    DEFAULT NULL,
    `description` text                 NOT NULL,
    `background`  binary(16)                    DEFAULT NULL,
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
    `id`         int(10) unsigned    NOT NULL AUTO_INCREMENT,
    `type`       tinyint(3) unsigned NOT NULL DEFAULT 0,
    `object_id`  int(10) unsigned    NOT NULL,
    `parent`     int(10) unsigned    NOT NULL DEFAULT 0,
    `floor`      int(10) unsigned    NOT NULL,
    `nest_id`    int(10) unsigned    NOT NULL,
    `nest_floor` int(10) unsigned    NOT NULL,
    `nest_size`  int(10) unsigned    NOT NULL DEFAULT 0,
    `user_id`    int(10) unsigned    NOT NULL,
    `nickname`   varchar(16)         DEFAULT NULL,
    `email`      tinytext            DEFAULT NULL,
    `content`    text                NOT NULL,
    `state`      tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '0-正常 1-删除 2-待审',
    `time`       datetime(6)         NOT NULL,
    `address`    binary(16)          NOT NULL,
    PRIMARY KEY (`id`),
    KEY `type_object_id` (`type`, `object_id`),
    KEY `nest_id` (`nest_id`)
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
    `cover`      binary(16)                DEFAULT NULL,
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
    `id`          int(10) unsigned NOT NULL AUTO_INCREMENT,
    `name`        varchar(16)      NOT NULL COMMENT '显示名可以重复',
    `avatar`      binary(16)                DEFAULT NULL,
    `email`       tinytext                  DEFAULT NULL,
    `deleted`     bit(1)           NOT NULL DEFAULT b'0',
    `auth`        tinyint(3)       NOT NULL,
    `create_time` datetime(6)      NOT NULL,
    `create_ip`   binary(16)       NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = Aria
  DEFAULT CHARSET = utf8mb4 PAGE_CHECKSUM=1;

/*!40101 SET SQL_MODE = IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS = IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES = IFNULL(@OLD_SQL_NOTES, 1) */;
