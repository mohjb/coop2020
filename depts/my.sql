
DROP TABLE IF EXISTS `dept`;
CREATE TABLE `dept` (
  `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `json` json NOT NULL,
  `meta` json NOT NULL,
  `name` varchar(255) NOT NULL,
  `parent` int(11) DEFAULT NULL,
   KEY `index2` (`dt`),
   KEY `index3` (`name`),
   KEY `index4` (`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ;

DROP TABLE IF EXISTS `usr`;
CREATE TABLE `usr` (
  `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `json` json NOT NULL,
  `meta` json NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `usrId` varchar(255) DEFAULT NULL,
  `pw` varchar(255) DEFAULT NULL,
  `expire` int DEFAULT NULL,
   KEY `index1` (`name`),
   KEY `index2` (`usrId`),
   KEY `index3` (`dt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `mmbr`;
CREATE TABLE `mmbr` (
	`id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`json` json NOT NULL,
	`meta` json NOT NULL,
	`level` enum('disabled','readonly','owner','admin') NOT NULL DEFAULT 'readonly',
	`dept` int(11) NOT NULL,
	`usr` int(11) NOT NULL,
	KEY `usrId_idx` (`usr`,`dt`),
	KEY `deptId_idx` (`dept`,`dt`),
	KEY `index4` (`dt`),
	FOREIGN KEY (`dept`) REFERENCES `dept`(`id`),
	FOREIGN KEY (`usr` ) REFERENCES `usr` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `msg`;
CREATE TABLE `msg` (
	`id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`json` json,
	`meta` json,
	`usr` int(11) NOT NULL,
	`msg` text NOT NULL,
	`dept` int(11) DEFAULT NULL,
	KEY `index2` (`usr`,`dt`),
	KEY `index3` (`dt`),
	KEY `index5` (`dept`,`dt`),
	FOREIGN KEY (`usr`) REFERENCES `usr` (`id`),
	FOREIGN KEY (`dept`) REFERENCES `dept` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `msgDest`;
CREATE TABLE `msgDest` (
  `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `json` json,
  `meta` json,
  `msg` int(11) NOT NULL,
  `usr` int(11) NOT NULL,
   KEY `index3` (`dt`),
   KEY `index4` (`usr`,`dt`),
  FOREIGN KEY (`msg`) REFERENCES `msg` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  FOREIGN KEY (`usr`) REFERENCES `usr` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `msgAttachment`;
CREATE TABLE `msgAttachment` (
	`id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`json` json,
	`meta` json,
	`msg` int(11) NOT NULL,
	`path` text,
	'mime` text,
	`payload` longblob ,
	KEY `index3` (`dt`),
	FOREIGN KEY (`msg`) REFERENCES `msg` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `log`;
CREATE TABLE `log` (
	`id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`json` json,
	`meta` json,
	`usr` int(11) NOT NULL,
	`act` text,
	KEY `index1` (`usr`,`dt`),
	KEY `index3` (`dt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
