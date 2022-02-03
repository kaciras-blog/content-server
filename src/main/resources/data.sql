INSERT INTO `account` (`id`, `name`, `password`, `salt`)
VALUES (2, 'admin',
        0x963529A8B9B33673AFF975C651C56A8EA052121F229C800D8231868E22BE199B2F5870566D0EE078D65BCF1B2C823A95C5F09470ED67491B4198C9CD25CF298F,
        0x5411A9562F0C468F03131D7CD267392AABD614AC145A03FA7B8EB860A24FC79DAD339231B11CDF7656E571A9DC1AA7A0258CF81E5236AE24609976DCA3E05561);

INSERT INTO `user` (`id`, `name`, `avatar`, `deleted`, `auth`, `create_time`, `create_ip`)
VALUES (2, 'admin', null, b'0', 1,
        '0000-00-00 00:00:00.000000', 0x00000000000000000000000000000000);

INSERT INTO `user` (`id`, `name`, `avatar`, `deleted`, `auth`, `create_time`, `create_ip`)
VALUES (1, '(系统)', null, b'0', 0,
        '0000-00-00 00:00:00.000000', 0x00000000000000000000000000000000);

INSERT INTO `category` (`id`, `name`, `cover`, `description`, `background`, `theme`)
VALUES (0, '顶级分类', null, '最上层的分类，其他所有分类都是此类的下级', null, 0);

INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES (0, 0, 0);
