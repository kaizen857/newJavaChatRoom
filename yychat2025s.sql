DROP DATABASE IF EXISTS yychat2025s;
CREATE DATABASE yychat2025s
CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE yychat2025s;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`(
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `username` varchar(255) DEFAULT NULL,
    `password_hash` BINARY(32) NOT NULL,
     `salt` BINARY(16) NOT NULL,
    PRIMARY KEY(`id`)
)ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;

ALTER TABLE `user` ADD UNIQUE (`username`);

DROP TABLE IF EXISTS `userRelation`;

CREATE TABLE `userRelation`(
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `masterUser` varchar(255) DEFAULT NULL,
    `slaveUser` varchar(255) DEFAULT NULL,
    `relation` int(11) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `masterUser`(`masterUser`),
    KEY `slaveUser`(`slaveUser`),
    CONSTRAINT `userRelation_ibfk_1` FOREIGN KEY(`masterUser`) REFERENCES `user`(`username`),
    CONSTRAINT `userRelation_ibfk_2` FOREIGN KEY(`slaveUser`) REFERENCES `user`(`username`)
)ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `message`;

CREATE TABLE `message`(
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `isImage` TINYINT(1) NOT NULL DEFAULT 0,
    `from_user` varchar(255) DEFAULT NULL,
    `to_user` varchar(255) DEFAULT NULL,
    `content` LONGTEXT DEFAULT NULL,
    `sendTime` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
)ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `userAvatarPath`;

CREATE TABLE `userAvatarPath`(
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `username` varchar(255) DEFAULT NULL,
    `avatarPath` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `username`(`username`),
    CONSTRAINT `userAvatarPath_ibfk_1` FOREIGN KEY(`username`) REFERENCES `user`(`username`)
)ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;

ALTER TABLE `userAvatarPath`
ADD COLUMN `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
