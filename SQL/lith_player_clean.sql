/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 100428
Source Host           : localhost:3306
Source Database       : lith_player

Target Server Type    : MYSQL
Target Server Version : 100428
File Encoding         : 65001

Date: 2025-06-23 11:45:52
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for accounts
-- ----------------------------
DROP TABLE IF EXISTS `accounts`;
CREATE TABLE `accounts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(13) NOT NULL DEFAULT '',
  `password` varchar(128) NOT NULL DEFAULT '',
  `salt` varchar(32) DEFAULT NULL,
  `2ndpassword` varchar(134) DEFAULT NULL,
  `salt2` varchar(32) DEFAULT NULL,
  `templogin` int(11) DEFAULT 0,
  `loggedin` tinyint(1) unsigned NOT NULL DEFAULT 0,
  `lastlogin` timestamp NULL DEFAULT NULL,
  `createdat` timestamp NOT NULL DEFAULT current_timestamp(),
  `birthday` date DEFAULT NULL,
  `banned` tinyint(1) NOT NULL DEFAULT 0,
  `banreason` text DEFAULT NULL,
  `gm` tinyint(1) NOT NULL DEFAULT 0,
  `email` tinytext DEFAULT NULL,
  `macs` tinytext DEFAULT NULL,
  `tempban` timestamp NULL DEFAULT NULL,
  `greason` tinyint(4) unsigned DEFAULT NULL,
  `ACash` int(11) NOT NULL DEFAULT 0,
  `mPoints` int(11) NOT NULL DEFAULT 0,
  `gender` tinyint(1) unsigned NOT NULL DEFAULT 0,
  `SessionIP` varchar(64) DEFAULT NULL,
  `points` int(11) NOT NULL DEFAULT 0,
  `vpoints` int(11) NOT NULL DEFAULT 0,
  `monthvotes` int(11) NOT NULL DEFAULT 0,
  `totalvotes` int(11) NOT NULL DEFAULT 0,
  `lastvote` int(11) NOT NULL DEFAULT 0,
  `lastvote2` int(11) NOT NULL DEFAULT 0,
  `lastlogon` timestamp NULL DEFAULT NULL,
  `lastvoteip` varchar(64) DEFAULT NULL,
  `webadmin` int(1) DEFAULT 0,
  `rebirths` int(11) NOT NULL DEFAULT 0,
  `ip` text DEFAULT NULL,
  `mainchar` int(6) NOT NULL DEFAULT 0,
  `nxcredit` int(11) unsigned NOT NULL DEFAULT 0,
  `nxprepaid` int(11) unsigned NOT NULL DEFAULT 0,
  `redeemhn` int(11) unsigned NOT NULL DEFAULT 0,
  `tos` tinyint(1) unsigned NOT NULL DEFAULT 0,
  `mesos` bigint(255) NOT NULL DEFAULT 0,
  `mp` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `ranking1` (`id`,`banned`,`gm`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of accounts
-- ----------------------------

-- ----------------------------
-- Table structure for account_bonus
-- ----------------------------
DROP TABLE IF EXISTS `account_bonus`;
CREATE TABLE `account_bonus` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `XP` bigint(19) NOT NULL DEFAULT 0,
  `DR` bigint(19) NOT NULL DEFAULT 0,
  `IDP` bigint(19) NOT NULL DEFAULT 0,
  `STAT` bigint(19) NOT NULL DEFAULT 0,
  `OP` bigint(19) NOT NULL DEFAULT 0,
  `MR` bigint(19) NOT NULL DEFAULT 0,
  `TD` bigint(19) NOT NULL DEFAULT 0,
  `BD` bigint(19) NOT NULL DEFAULT 0,
  `IED` bigint(19) NOT NULL DEFAULT 0,
  `ETC` bigint(19) NOT NULL DEFAULT 0,
  `CD` bigint(19) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `ranking1` (`id`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of account_bonus
-- ----------------------------

-- ----------------------------
-- Table structure for account_variables
-- ----------------------------
DROP TABLE IF EXISTS `account_variables`;
CREATE TABLE `account_variables` (
  `account` int(11) NOT NULL,
  `var` varchar(36) NOT NULL DEFAULT '',
  `amount` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`account`,`var`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of account_variables
-- ----------------------------

-- ----------------------------
-- Table structure for achievements
-- ----------------------------
DROP TABLE IF EXISTS `achievements`;
CREATE TABLE `achievements` (
  `accountid` int(11) NOT NULL DEFAULT 0,
  `achievementid` int(9) NOT NULL DEFAULT 0,
  PRIMARY KEY (`accountid`,`achievementid`),
  KEY `accountid` (`accountid`),
  KEY `achievementid` (`achievementid`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of achievements
-- ----------------------------

-- ----------------------------
-- Table structure for alliances
-- ----------------------------
DROP TABLE IF EXISTS `alliances`;
CREATE TABLE `alliances` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(13) NOT NULL,
  `leaderid` int(11) NOT NULL,
  `guild1` int(11) NOT NULL,
  `guild2` int(11) NOT NULL,
  `guild3` int(11) NOT NULL DEFAULT 0,
  `guild4` int(11) NOT NULL DEFAULT 0,
  `guild5` int(11) NOT NULL DEFAULT 0,
  `rank1` varchar(13) NOT NULL DEFAULT 'Master',
  `rank2` varchar(13) NOT NULL DEFAULT 'Jr.Master',
  `rank3` varchar(13) NOT NULL DEFAULT 'Member',
  `rank4` varchar(13) NOT NULL DEFAULT 'Member',
  `rank5` varchar(13) NOT NULL DEFAULT 'Member',
  `capacity` int(11) NOT NULL DEFAULT 2,
  `notice` varchar(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `id` (`id`),
  KEY `leaderid` (`leaderid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of alliances
-- ----------------------------

-- ----------------------------
-- Table structure for androids
-- ----------------------------
DROP TABLE IF EXISTS `androids`;
CREATE TABLE `androids` (
  `uniqueid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(13) NOT NULL DEFAULT 'Android',
  `hair` int(11) NOT NULL DEFAULT 0,
  `face` int(11) NOT NULL DEFAULT 0,
  `skin` int(11) NOT NULL DEFAULT 0,
  `model` int(11) NOT NULL DEFAULT 0,
  `level` int(11) NOT NULL DEFAULT 1,
  `exp` bigint(25) NOT NULL DEFAULT 0,
  PRIMARY KEY (`uniqueid`),
  KEY `uniqueid` (`uniqueid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of androids
-- ----------------------------

-- ----------------------------
-- Table structure for battlelog
-- ----------------------------
DROP TABLE IF EXISTS `battlelog`;
CREATE TABLE `battlelog` (
  `battlelogid` int(11) NOT NULL AUTO_INCREMENT,
  `accid` int(11) NOT NULL DEFAULT 0,
  `accid_to` int(11) NOT NULL DEFAULT 0,
  `when` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`battlelogid`),
  KEY `accid` (`accid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of battlelog
-- ----------------------------

-- ----------------------------
-- Table structure for bbs_replies
-- ----------------------------
DROP TABLE IF EXISTS `bbs_replies`;
CREATE TABLE `bbs_replies` (
  `replyid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `threadid` int(10) unsigned NOT NULL,
  `postercid` int(10) unsigned NOT NULL,
  `timestamp` bigint(20) unsigned NOT NULL,
  `content` varchar(26) NOT NULL DEFAULT '',
  `guildid` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`replyid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of bbs_replies
-- ----------------------------

-- ----------------------------
-- Table structure for bbs_threads
-- ----------------------------
DROP TABLE IF EXISTS `bbs_threads`;
CREATE TABLE `bbs_threads` (
  `threadid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `postercid` int(10) unsigned NOT NULL,
  `name` varchar(26) NOT NULL DEFAULT '',
  `timestamp` bigint(20) unsigned NOT NULL,
  `icon` smallint(5) unsigned NOT NULL,
  `startpost` text NOT NULL,
  `guildid` int(10) unsigned NOT NULL,
  `localthreadid` int(10) unsigned NOT NULL,
  PRIMARY KEY (`threadid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of bbs_threads
-- ----------------------------

-- ----------------------------
-- Table structure for buddies
-- ----------------------------
DROP TABLE IF EXISTS `buddies`;
CREATE TABLE `buddies` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `characterid` int(11) NOT NULL,
  `buddyid` int(11) NOT NULL,
  `pending` tinyint(4) NOT NULL DEFAULT 0,
  `groupname` varchar(16) NOT NULL DEFAULT 'ETC',
  PRIMARY KEY (`id`),
  KEY `buddies_ibfk_1` (`characterid`),
  KEY `buddyid` (`buddyid`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of buddies
-- ----------------------------

-- ----------------------------
-- Table structure for buff
-- ----------------------------
DROP TABLE IF EXISTS `buff`;
CREATE TABLE `buff` (
  `accid` int(11) NOT NULL,
  `exp` bigint(255) NOT NULL DEFAULT 0,
  `drop` bigint(255) NOT NULL DEFAULT 0,
  `etc` bigint(255) NOT NULL DEFAULT 0,
  `meso` bigint(255) NOT NULL DEFAULT 0,
  PRIMARY KEY (`accid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of buff
-- ----------------------------

-- ----------------------------
-- Table structure for characters
-- ----------------------------
DROP TABLE IF EXISTS `characters`;
CREATE TABLE `characters` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `accountid` int(11) NOT NULL DEFAULT 0,
  `world` tinyint(1) NOT NULL DEFAULT 0,
  `name` varchar(13) NOT NULL DEFAULT '',
  `level` int(3) unsigned NOT NULL DEFAULT 0,
  `totallevel` int(11) NOT NULL DEFAULT 0,
  `exp` int(11) NOT NULL DEFAULT 0,
  `overexp` varchar(45) NOT NULL DEFAULT '0',
  `str` int(11) NOT NULL DEFAULT 0,
  `dex` int(11) NOT NULL DEFAULT 0,
  `luk` int(11) NOT NULL DEFAULT 0,
  `int` int(11) NOT NULL DEFAULT 0,
  `hp` int(11) NOT NULL DEFAULT 0,
  `mp` int(11) NOT NULL DEFAULT 0,
  `maxhp` int(11) NOT NULL DEFAULT 0,
  `maxmp` int(11) NOT NULL DEFAULT 0,
  `meso` int(11) NOT NULL DEFAULT 0,
  `hpApUsed` int(5) NOT NULL DEFAULT 0,
  `job` int(5) NOT NULL DEFAULT 0,
  `skincolor` tinyint(1) NOT NULL DEFAULT 0,
  `gender` tinyint(1) NOT NULL DEFAULT 0,
  `fame` int(5) NOT NULL DEFAULT 0,
  `hair` int(11) NOT NULL DEFAULT 0,
  `face` int(11) NOT NULL DEFAULT 0,
  `demonMarking` int(11) NOT NULL DEFAULT 0,
  `ap` int(11) NOT NULL DEFAULT 0,
  `map` int(11) NOT NULL DEFAULT 0,
  `spawnpoint` int(3) NOT NULL DEFAULT 0,
  `gm` int(3) NOT NULL DEFAULT 0,
  `party` int(11) NOT NULL DEFAULT 0,
  `buddyCapacity` int(3) NOT NULL DEFAULT 25,
  `createdate` timestamp NOT NULL DEFAULT current_timestamp(),
  `guildid` int(10) unsigned NOT NULL DEFAULT 0,
  `guildrank` tinyint(1) unsigned NOT NULL DEFAULT 5,
  `allianceRank` tinyint(1) unsigned NOT NULL DEFAULT 5,
  `guildContribution` int(11) NOT NULL DEFAULT 0,
  `pets` varchar(13) NOT NULL DEFAULT '-1,-1,-1',
  `sp` int(11) NOT NULL DEFAULT 0,
  `subcategory` int(11) NOT NULL DEFAULT 0,
  `rank` int(11) NOT NULL DEFAULT 1,
  `rankMove` int(11) NOT NULL DEFAULT 0,
  `jobRank` int(11) NOT NULL DEFAULT 1,
  `jobRankMove` int(11) NOT NULL DEFAULT 0,
  `marriageId` int(11) NOT NULL DEFAULT 0,
  `familyid` int(11) NOT NULL DEFAULT 0,
  `seniorid` int(11) NOT NULL DEFAULT 0,
  `junior1` int(11) NOT NULL DEFAULT 0,
  `junior2` int(11) NOT NULL DEFAULT 0,
  `currentrep` int(11) NOT NULL DEFAULT 0,
  `totalrep` int(11) NOT NULL DEFAULT 0,
  `gachexp` int(11) NOT NULL DEFAULT 0,
  `fatigue` tinyint(4) NOT NULL DEFAULT 0,
  `charm` mediumint(7) NOT NULL DEFAULT 0,
  `craft` mediumint(7) NOT NULL DEFAULT 0,
  `charisma` mediumint(7) NOT NULL DEFAULT 0,
  `will` mediumint(7) NOT NULL DEFAULT 0,
  `sense` mediumint(7) NOT NULL DEFAULT 0,
  `insight` mediumint(7) NOT NULL DEFAULT 0,
  `totalWins` int(11) NOT NULL DEFAULT 0,
  `totalLosses` int(11) NOT NULL DEFAULT 0,
  `pvpLevel` int(11) NOT NULL DEFAULT 1,
  `pvpExp` int(11) NOT NULL DEFAULT 0,
  `pvpPoints` int(11) NOT NULL DEFAULT 0,
  `reborns` int(11) NOT NULL DEFAULT 0,
  `apstorage` int(11) NOT NULL DEFAULT 0,
  `tier` int(11) NOT NULL DEFAULT 1,
  `stamina` int(11) NOT NULL DEFAULT 0,
  `damage` int(11) NOT NULL DEFAULT 9999,
  PRIMARY KEY (`id`),
  KEY `accountid` (`accountid`),
  KEY `id` (`id`),
  KEY `guildid` (`guildid`),
  KEY `familyid` (`familyid`),
  KEY `marriageId` (`marriageId`),
  KEY `seniorid` (`seniorid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of characters
-- ----------------------------

-- ----------------------------
-- Table structure for character_bonus
-- ----------------------------
DROP TABLE IF EXISTS `character_bonus`;
CREATE TABLE `character_bonus` (
  `id` int(11) NOT NULL,
  `str` int(11) NOT NULL DEFAULT 0,
  `dex` int(11) NOT NULL DEFAULT 0,
  `int_` int(11) NOT NULL DEFAULT 0,
  `luk` int(11) NOT NULL DEFAULT 0,
  `atk` int(11) NOT NULL DEFAULT 0,
  `matk` int(11) NOT NULL DEFAULT 0,
  `def` int(11) NOT NULL DEFAULT 0,
  `mdef` int(11) NOT NULL DEFAULT 0,
  `hp` int(11) NOT NULL DEFAULT 0,
  `mp` int(11) NOT NULL DEFAULT 0,
  `mob` int(11) NOT NULL DEFAULT 0,
  `boss` int(11) NOT NULL DEFAULT 0,
  `ied` int(11) NOT NULL DEFAULT 0,
  `cd` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `ranking1` (`id`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of character_bonus
-- ----------------------------

-- ----------------------------
-- Table structure for character_maps
-- ----------------------------
DROP TABLE IF EXISTS `character_maps`;
CREATE TABLE `character_maps` (
  `charid` int(11) NOT NULL,
  `mapid` int(11) NOT NULL,
  PRIMARY KEY (`charid`,`mapid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of character_maps
-- ----------------------------

-- ----------------------------
-- Table structure for character_slots
-- ----------------------------
DROP TABLE IF EXISTS `character_slots`;
CREATE TABLE `character_slots` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `accid` int(11) NOT NULL DEFAULT 0,
  `worldid` int(11) NOT NULL DEFAULT 0,
  `charslots` int(11) NOT NULL DEFAULT 3,
  PRIMARY KEY (`id`),
  KEY `accid` (`accid`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of character_slots
-- ----------------------------

-- ----------------------------
-- Table structure for character_variables
-- ----------------------------
DROP TABLE IF EXISTS `character_variables`;
CREATE TABLE `character_variables` (
  `charid` int(11) NOT NULL,
  `var` varchar(36) NOT NULL DEFAULT '',
  `amount` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`charid`,`var`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of character_variables
-- ----------------------------

-- ----------------------------
-- Table structure for cheatlog
-- ----------------------------
DROP TABLE IF EXISTS `cheatlog`;
CREATE TABLE `cheatlog` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `characterid` int(11) NOT NULL DEFAULT 0,
  `offense` tinytext NOT NULL,
  `count` int(11) NOT NULL DEFAULT 0,
  `lastoffensetime` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `param` tinytext NOT NULL,
  PRIMARY KEY (`id`),
  KEY `cid` (`characterid`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of cheatlog
-- ----------------------------

-- ----------------------------
-- Table structure for compensationlog_confirmed
-- ----------------------------
DROP TABLE IF EXISTS `compensationlog_confirmed`;
CREATE TABLE `compensationlog_confirmed` (
  `chrname` varchar(25) NOT NULL DEFAULT '',
  `donor` tinyint(1) NOT NULL DEFAULT 0,
  `value` int(11) NOT NULL DEFAULT 0,
  `taken` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`chrname`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of compensationlog_confirmed
-- ----------------------------

-- ----------------------------
-- Table structure for csequipment
-- ----------------------------
DROP TABLE IF EXISTS `csequipment`;
CREATE TABLE `csequipment` (
  `inventoryequipmentid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `inventoryitemid` bigint(20) unsigned NOT NULL DEFAULT 0,
  `upgradeslots` int(11) NOT NULL DEFAULT 0,
  `level` int(11) NOT NULL DEFAULT 0,
  `str` int(11) NOT NULL DEFAULT 0,
  `dex` int(11) NOT NULL DEFAULT 0,
  `int` int(11) NOT NULL DEFAULT 0,
  `luk` int(11) NOT NULL DEFAULT 0,
  `hp` int(11) NOT NULL DEFAULT 0,
  `mp` int(11) NOT NULL DEFAULT 0,
  `watk` int(11) NOT NULL DEFAULT 0,
  `matk` int(11) NOT NULL DEFAULT 0,
  `wdef` int(11) NOT NULL DEFAULT 0,
  `mdef` int(11) NOT NULL DEFAULT 0,
  `acc` int(11) NOT NULL DEFAULT 0,
  `avoid` int(11) NOT NULL DEFAULT 0,
  `hands` int(11) NOT NULL DEFAULT 0,
  `speed` int(11) NOT NULL DEFAULT 0,
  `jump` int(11) NOT NULL DEFAULT 0,
  `ViciousHammer` tinyint(2) NOT NULL DEFAULT 0,
  `itemEXP` int(11) NOT NULL DEFAULT 0,
  `durability` int(11) NOT NULL DEFAULT -1,
  `enhance` tinyint(3) NOT NULL DEFAULT 0,
  `potential1` int(5) NOT NULL DEFAULT 0,
  `potential2` int(5) NOT NULL DEFAULT 0,
  `potential3` int(5) NOT NULL DEFAULT 0,
  `potential4` int(5) NOT NULL DEFAULT 0,
  `potential5` int(5) NOT NULL DEFAULT 0,
  `socket1` int(5) NOT NULL DEFAULT -1,
  `socket2` int(5) NOT NULL DEFAULT -1,
  `socket3` int(5) NOT NULL DEFAULT -1,
  `incSkill` int(11) NOT NULL DEFAULT -1,
  `charmEXP` smallint(6) NOT NULL DEFAULT -1,
  `pvpDamage` smallint(6) NOT NULL DEFAULT 0,
  PRIMARY KEY (`inventoryequipmentid`),
  KEY `inventoryitemid` (`inventoryitemid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of csequipment
-- ----------------------------

-- ----------------------------
-- Table structure for csitems
-- ----------------------------
DROP TABLE IF EXISTS `csitems`;
CREATE TABLE `csitems` (
  `inventoryitemid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `characterid` int(11) DEFAULT NULL,
  `accountid` int(10) DEFAULT NULL,
  `packageid` int(11) DEFAULT NULL,
  `itemid` int(11) NOT NULL DEFAULT 0,
  `inventorytype` int(11) NOT NULL DEFAULT 0,
  `position` int(11) NOT NULL DEFAULT 0,
  `quantity` int(11) NOT NULL DEFAULT 0,
  `owner` tinytext DEFAULT NULL,
  `GM_Log` tinytext DEFAULT NULL,
  `uniqueid` int(11) NOT NULL DEFAULT -1,
  `flag` int(2) NOT NULL DEFAULT 0,
  `expiredate` bigint(20) NOT NULL DEFAULT -1,
  `type` tinyint(1) NOT NULL DEFAULT 0,
  `sender` varchar(13) NOT NULL DEFAULT '',
  PRIMARY KEY (`inventoryitemid`),
  KEY `inventoryitems_ibfk_1` (`characterid`),
  KEY `characterid` (`characterid`),
  KEY `inventorytype` (`inventorytype`),
  KEY `accountid` (`accountid`),
  KEY `packageid` (`packageid`),
  KEY `characterid_2` (`characterid`,`inventorytype`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of csitems
-- ----------------------------

-- ----------------------------
-- Table structure for damage_skins
-- ----------------------------
DROP TABLE IF EXISTS `damage_skins`;
CREATE TABLE `damage_skins` (
  `accid` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `level` int(11) NOT NULL DEFAULT 1,
  `exp` bigint(255) NOT NULL DEFAULT 0,
  PRIMARY KEY (`accid`,`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of damage_skins
-- ----------------------------

-- ----------------------------
-- Table structure for dojo
-- ----------------------------
DROP TABLE IF EXISTS `dojo`;
CREATE TABLE `dojo` (
  `charid` int(11) NOT NULL,
  `level` int(11) NOT NULL DEFAULT 1,
  `exp` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of dojo
-- ----------------------------

-- ----------------------------
-- Table structure for donorlog
-- ----------------------------
DROP TABLE IF EXISTS `donorlog`;
CREATE TABLE `donorlog` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `accname` varchar(25) NOT NULL DEFAULT '',
  `accId` int(11) NOT NULL DEFAULT 0,
  `chrname` varchar(25) NOT NULL DEFAULT '',
  `chrId` int(11) NOT NULL DEFAULT 0,
  `log` varchar(4096) NOT NULL DEFAULT '',
  `time` varchar(25) NOT NULL DEFAULT '',
  `previousPoints` int(11) NOT NULL DEFAULT 0,
  `currentPoints` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of donorlog
-- ----------------------------

-- ----------------------------
-- Table structure for dueyequipment
-- ----------------------------
DROP TABLE IF EXISTS `dueyequipment`;
CREATE TABLE `dueyequipment` (
  `inventoryequipmentid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `inventoryitemid` bigint(20) unsigned NOT NULL DEFAULT 0,
  `upgradeslots` int(11) NOT NULL DEFAULT 0,
  `level` int(11) NOT NULL DEFAULT 0,
  `str` int(11) NOT NULL DEFAULT 0,
  `dex` int(11) NOT NULL DEFAULT 0,
  `int` int(11) NOT NULL DEFAULT 0,
  `luk` int(11) NOT NULL DEFAULT 0,
  `hp` int(11) NOT NULL DEFAULT 0,
  `mp` int(11) NOT NULL DEFAULT 0,
  `watk` int(11) NOT NULL DEFAULT 0,
  `matk` int(11) NOT NULL DEFAULT 0,
  `wdef` int(11) NOT NULL DEFAULT 0,
  `mdef` int(11) NOT NULL DEFAULT 0,
  `acc` int(11) NOT NULL DEFAULT 0,
  `avoid` int(11) NOT NULL DEFAULT 0,
  `hands` int(11) NOT NULL DEFAULT 0,
  `speed` int(11) NOT NULL DEFAULT 0,
  `jump` int(11) NOT NULL DEFAULT 0,
  `ViciousHammer` tinyint(2) NOT NULL DEFAULT 0,
  `itemEXP` int(11) NOT NULL DEFAULT 0,
  `durability` int(11) NOT NULL DEFAULT -1,
  `enhance` tinyint(3) NOT NULL DEFAULT 0,
  `potential1` int(5) NOT NULL DEFAULT 0,
  `potential2` int(5) NOT NULL DEFAULT 0,
  `potential3` int(5) NOT NULL DEFAULT 0,
  `potential4` int(5) NOT NULL DEFAULT 0,
  `potential5` int(5) NOT NULL DEFAULT 0,
  `socket1` int(5) NOT NULL DEFAULT -1,
  `socket2` int(5) NOT NULL DEFAULT -1,
  `socket3` int(5) NOT NULL DEFAULT -1,
  `incSkill` int(11) NOT NULL DEFAULT -1,
  `charmEXP` smallint(6) NOT NULL DEFAULT -1,
  `pvpDamage` smallint(6) NOT NULL DEFAULT 0,
  PRIMARY KEY (`inventoryequipmentid`),
  KEY `inventoryitemid` (`inventoryitemid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of dueyequipment
-- ----------------------------

-- ----------------------------
-- Table structure for dueyitems
-- ----------------------------
DROP TABLE IF EXISTS `dueyitems`;
CREATE TABLE `dueyitems` (
  `inventoryitemid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `characterid` int(11) DEFAULT NULL,
  `accountid` int(10) DEFAULT NULL,
  `packageid` int(11) DEFAULT NULL,
  `itemid` int(11) NOT NULL DEFAULT 0,
  `inventorytype` int(11) NOT NULL DEFAULT 0,
  `position` int(11) NOT NULL DEFAULT 0,
  `quantity` int(11) NOT NULL DEFAULT 0,
  `owner` tinytext DEFAULT NULL,
  `GM_Log` tinytext DEFAULT NULL,
  `uniqueid` int(11) NOT NULL DEFAULT -1,
  `flag` int(2) NOT NULL DEFAULT 0,
  `expiredate` bigint(20) NOT NULL DEFAULT -1,
  `type` tinyint(1) NOT NULL DEFAULT 0,
  `sender` varchar(13) NOT NULL DEFAULT '',
  PRIMARY KEY (`inventoryitemid`),
  KEY `inventoryitems_ibfk_1` (`characterid`),
  KEY `characterid` (`characterid`),
  KEY `inventorytype` (`inventorytype`),
  KEY `accountid` (`accountid`),
  KEY `packageid` (`packageid`),
  KEY `characterid_2` (`characterid`,`inventorytype`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of dueyitems
-- ----------------------------

-- ----------------------------
-- Table structure for dueypackages
-- ----------------------------
DROP TABLE IF EXISTS `dueypackages`;
CREATE TABLE `dueypackages` (
  `PackageId` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `RecieverId` int(10) NOT NULL,
  `SenderName` varchar(13) NOT NULL,
  `Mesos` int(10) unsigned DEFAULT 0,
  `TimeStamp` bigint(20) unsigned DEFAULT NULL,
  `Checked` tinyint(1) unsigned DEFAULT 1,
  `Type` tinyint(1) unsigned NOT NULL,
  PRIMARY KEY (`PackageId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of dueypackages
-- ----------------------------

-- ----------------------------
-- Table structure for event
-- ----------------------------
DROP TABLE IF EXISTS `event`;
CREATE TABLE `event` (
  `charid` int(11) NOT NULL,
  `tower` bigint(255) NOT NULL DEFAULT 0,
  `bosspq` bigint(255) NOT NULL DEFAULT 0,
  `dojo` bigint(255) NOT NULL DEFAULT 0,
  `monsterpark` bigint(255) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of event
-- ----------------------------

-- ----------------------------
-- Table structure for extendedslots
-- ----------------------------
DROP TABLE IF EXISTS `extendedslots`;
CREATE TABLE `extendedslots` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `characterid` int(11) NOT NULL DEFAULT 0,
  `itemId` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of extendedslots
-- ----------------------------

-- ----------------------------
-- Table structure for famelog
-- ----------------------------
DROP TABLE IF EXISTS `famelog`;
CREATE TABLE `famelog` (
  `famelogid` int(11) NOT NULL AUTO_INCREMENT,
  `characterid` int(11) NOT NULL DEFAULT 0,
  `characterid_to` int(11) NOT NULL DEFAULT 0,
  `when` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`famelogid`),
  KEY `characterid` (`characterid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of famelog
-- ----------------------------

-- ----------------------------
-- Table structure for familiars
-- ----------------------------
DROP TABLE IF EXISTS `familiars`;
CREATE TABLE `familiars` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `characterid` int(11) NOT NULL DEFAULT 0,
  `familiar` int(11) NOT NULL DEFAULT 0,
  `name` varchar(40) NOT NULL DEFAULT '',
  `fatigue` int(11) NOT NULL DEFAULT 0,
  `expiry` bigint(20) NOT NULL DEFAULT 0,
  `vitality` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of familiars
-- ----------------------------

-- ----------------------------
-- Table structure for families
-- ----------------------------
DROP TABLE IF EXISTS `families`;
CREATE TABLE `families` (
  `familyid` int(11) NOT NULL AUTO_INCREMENT,
  `leaderid` int(11) NOT NULL DEFAULT 0,
  `notice` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`familyid`),
  KEY `familyid` (`familyid`),
  KEY `leaderid` (`leaderid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of families
-- ----------------------------

-- ----------------------------
-- Table structure for gifts
-- ----------------------------
DROP TABLE IF EXISTS `gifts`;
CREATE TABLE `gifts` (
  `giftid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `recipient` int(11) NOT NULL DEFAULT 0,
  `from` varchar(13) NOT NULL DEFAULT '',
  `message` varchar(255) NOT NULL DEFAULT '',
  `sn` int(11) NOT NULL DEFAULT 0,
  `uniqueid` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`giftid`),
  KEY `recipient` (`recipient`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of gifts
-- ----------------------------

-- ----------------------------
-- Table structure for gmlog
-- ----------------------------
DROP TABLE IF EXISTS `gmlog`;
CREATE TABLE `gmlog` (
  `gmlogid` int(11) NOT NULL AUTO_INCREMENT,
  `cid` int(11) NOT NULL DEFAULT 0,
  `command` text NOT NULL,
  `mapid` int(11) NOT NULL DEFAULT 0,
  `time` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`gmlogid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of gmlog
-- ----------------------------

-- ----------------------------
-- Table structure for guilds
-- ----------------------------
DROP TABLE IF EXISTS `guilds`;
CREATE TABLE `guilds` (
  `guildid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `leader` int(10) unsigned NOT NULL DEFAULT 0,
  `GP` bigint(20) NOT NULL DEFAULT 0,
  `logo` int(10) unsigned DEFAULT NULL,
  `logoColor` smallint(5) unsigned NOT NULL DEFAULT 0,
  `name` varchar(45) NOT NULL,
  `rank1title` varchar(45) NOT NULL DEFAULT 'Master',
  `rank2title` varchar(45) NOT NULL DEFAULT 'Jr. Master',
  `rank3title` varchar(45) NOT NULL DEFAULT 'Member',
  `rank4title` varchar(45) NOT NULL DEFAULT 'Member',
  `rank5title` varchar(45) NOT NULL DEFAULT 'Member',
  `capacity` int(10) unsigned NOT NULL DEFAULT 10,
  `logoBG` int(10) unsigned DEFAULT NULL,
  `logoBGColor` smallint(5) unsigned NOT NULL DEFAULT 0,
  `notice` varchar(101) DEFAULT NULL,
  `signature` int(11) NOT NULL DEFAULT 0,
  `alliance` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`guildid`),
  UNIQUE KEY `name` (`name`),
  KEY `guildid` (`guildid`),
  KEY `leader` (`leader`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of guilds
-- ----------------------------

-- ----------------------------
-- Table structure for guildskills
-- ----------------------------
DROP TABLE IF EXISTS `guildskills`;
CREATE TABLE `guildskills` (
  `guildid` int(11) NOT NULL DEFAULT 0,
  `skillid` int(11) NOT NULL DEFAULT 0,
  `level` smallint(3) NOT NULL DEFAULT 1,
  `timestamp` bigint(20) NOT NULL DEFAULT 0,
  `purchaser` varchar(13) NOT NULL DEFAULT '',
  PRIMARY KEY (`guildid`,`skillid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of guildskills
-- ----------------------------

-- ----------------------------
-- Table structure for hiredmerch
-- ----------------------------
DROP TABLE IF EXISTS `hiredmerch`;
CREATE TABLE `hiredmerch` (
  `PackageId` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `characterid` int(10) unsigned DEFAULT 0,
  `accountid` int(10) unsigned DEFAULT NULL,
  `Mesos` int(10) unsigned DEFAULT 0,
  `time` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`PackageId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of hiredmerch
-- ----------------------------

-- ----------------------------
-- Table structure for hiredmerchequipment
-- ----------------------------
DROP TABLE IF EXISTS `hiredmerchequipment`;
CREATE TABLE `hiredmerchequipment` (
  `inventoryequipmentid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `inventoryitemid` bigint(20) unsigned NOT NULL DEFAULT 0,
  `upgradeslots` int(11) NOT NULL DEFAULT 0,
  `level` int(11) NOT NULL DEFAULT 0,
  `str` int(11) NOT NULL DEFAULT 0,
  `dex` int(11) NOT NULL DEFAULT 0,
  `int` int(11) NOT NULL DEFAULT 0,
  `luk` int(11) NOT NULL DEFAULT 0,
  `hp` int(11) NOT NULL DEFAULT 0,
  `mp` int(11) NOT NULL DEFAULT 0,
  `watk` int(11) NOT NULL DEFAULT 0,
  `matk` int(11) NOT NULL DEFAULT 0,
  `wdef` int(11) NOT NULL DEFAULT 0,
  `mdef` int(11) NOT NULL DEFAULT 0,
  `acc` int(11) NOT NULL DEFAULT 0,
  `avoid` int(11) NOT NULL DEFAULT 0,
  `hands` int(11) NOT NULL DEFAULT 0,
  `speed` int(11) NOT NULL DEFAULT 0,
  `jump` int(11) NOT NULL DEFAULT 0,
  `ViciousHammer` tinyint(2) NOT NULL DEFAULT 0,
  `itemEXP` int(11) NOT NULL DEFAULT 0,
  `durability` int(11) NOT NULL DEFAULT -1,
  `enhance` tinyint(3) NOT NULL DEFAULT 0,
  `potential1` int(5) NOT NULL DEFAULT 0,
  `potential2` int(5) NOT NULL DEFAULT 0,
  `potential3` int(5) NOT NULL DEFAULT 0,
  `potential4` int(5) NOT NULL DEFAULT 0,
  `potential5` int(5) NOT NULL DEFAULT 0,
  `socket1` int(5) NOT NULL DEFAULT -1,
  `socket2` int(5) NOT NULL DEFAULT -1,
  `socket3` int(5) NOT NULL DEFAULT -1,
  `incSkill` int(11) NOT NULL DEFAULT -1,
  `charmEXP` smallint(6) NOT NULL DEFAULT -1,
  `pvpDamage` smallint(6) NOT NULL DEFAULT 0,
  PRIMARY KEY (`inventoryequipmentid`),
  KEY `inventoryitemid` (`inventoryitemid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of hiredmerchequipment
-- ----------------------------

-- ----------------------------
-- Table structure for hiredmerchitems
-- ----------------------------
DROP TABLE IF EXISTS `hiredmerchitems`;
CREATE TABLE `hiredmerchitems` (
  `inventoryitemid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `characterid` int(11) DEFAULT NULL,
  `accountid` int(10) DEFAULT NULL,
  `packageid` int(11) DEFAULT NULL,
  `itemid` int(11) NOT NULL DEFAULT 0,
  `inventorytype` int(11) NOT NULL DEFAULT 0,
  `position` int(11) NOT NULL DEFAULT 0,
  `quantity` int(11) NOT NULL DEFAULT 0,
  `owner` tinytext DEFAULT NULL,
  `GM_Log` tinytext DEFAULT NULL,
  `uniqueid` int(11) NOT NULL DEFAULT -1,
  `flag` int(2) NOT NULL DEFAULT 0,
  `expiredate` bigint(20) NOT NULL DEFAULT -1,
  `type` tinyint(1) NOT NULL DEFAULT 0,
  `sender` varchar(13) NOT NULL DEFAULT '',
  PRIMARY KEY (`inventoryitemid`),
  KEY `inventoryitems_ibfk_1` (`characterid`),
  KEY `characterid` (`characterid`),
  KEY `inventorytype` (`inventorytype`),
  KEY `accountid` (`accountid`),
  KEY `packageid` (`packageid`),
  KEY `characterid_2` (`characterid`,`inventorytype`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of hiredmerchitems
-- ----------------------------

-- ----------------------------
-- Table structure for hyperrocklocations
-- ----------------------------
DROP TABLE IF EXISTS `hyperrocklocations`;
CREATE TABLE `hyperrocklocations` (
  `trockid` int(11) NOT NULL AUTO_INCREMENT,
  `characterid` int(11) DEFAULT NULL,
  `mapid` int(11) DEFAULT NULL,
  PRIMARY KEY (`trockid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of hyperrocklocations
-- ----------------------------

-- ----------------------------
-- Table structure for imps
-- ----------------------------
DROP TABLE IF EXISTS `imps`;
CREATE TABLE `imps` (
  `impid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `characterid` int(11) NOT NULL DEFAULT 0,
  `itemid` int(11) NOT NULL DEFAULT 0,
  `level` tinyint(3) unsigned NOT NULL DEFAULT 1,
  `state` tinyint(3) unsigned NOT NULL DEFAULT 1,
  `closeness` mediumint(6) unsigned NOT NULL DEFAULT 0,
  `fullness` mediumint(6) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`impid`),
  KEY `impid` (`impid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of imps
-- ----------------------------

-- ----------------------------
-- Table structure for internlog
-- ----------------------------
DROP TABLE IF EXISTS `internlog`;
CREATE TABLE `internlog` (
  `gmlogid` int(11) NOT NULL AUTO_INCREMENT,
  `cid` int(11) NOT NULL DEFAULT 0,
  `command` tinytext NOT NULL,
  `mapid` int(11) NOT NULL DEFAULT 0,
  `time` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`gmlogid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of internlog
-- ----------------------------

-- ----------------------------
-- Table structure for inventoryequips
-- ----------------------------
DROP TABLE IF EXISTS `inventoryequips`;
CREATE TABLE `inventoryequips` (
  `inventoryitemid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `type` tinyint(1) NOT NULL DEFAULT 0,
  `characterid` int(11) DEFAULT NULL,
  `accountid` int(10) DEFAULT NULL,
  `itemid` int(11) NOT NULL DEFAULT 0,
  `itemname` varchar(255) DEFAULT NULL,
  `inventorytype` int(11) NOT NULL DEFAULT 0,
  `position` int(11) NOT NULL DEFAULT 0,
  `quantity` int(11) NOT NULL DEFAULT 0,
  `owner` tinytext DEFAULT NULL,
  `GM_Log` tinytext DEFAULT NULL,
  `uniqueid` int(11) NOT NULL DEFAULT -1,
  `flag` int(2) NOT NULL DEFAULT 0,
  `expiredate` bigint(20) NOT NULL DEFAULT -1,
  `sender` varchar(13) NOT NULL DEFAULT '',
  `upgradeslots` int(11) unsigned NOT NULL DEFAULT 0,
  `level` int(11) unsigned NOT NULL DEFAULT 0,
  `str` int(11) NOT NULL DEFAULT 0,
  `dex` int(11) NOT NULL DEFAULT 0,
  `int` int(11) NOT NULL DEFAULT 0,
  `luk` int(11) NOT NULL DEFAULT 0,
  `hp` int(11) NOT NULL DEFAULT 0,
  `hpr` int(11) NOT NULL DEFAULT 0,
  `mp` int(11) NOT NULL DEFAULT 0,
  `mpr` int(11) NOT NULL DEFAULT 0,
  `watk` int(11) NOT NULL DEFAULT 0,
  `matk` int(11) NOT NULL DEFAULT 0,
  `wdef` int(11) NOT NULL DEFAULT 0,
  `mdef` int(11) NOT NULL DEFAULT 0,
  `acc` int(11) NOT NULL DEFAULT 0,
  `avoid` int(11) NOT NULL DEFAULT 0,
  `hands` int(11) NOT NULL DEFAULT 0,
  `speed` int(11) NOT NULL DEFAULT 0,
  `jump` int(11) NOT NULL DEFAULT 0,
  `ViciousHammer` tinyint(2) NOT NULL DEFAULT 0,
  `itemEXP` int(11) NOT NULL DEFAULT 0,
  `durability` mediumint(9) NOT NULL DEFAULT -1,
  `enhance` smallint(3) NOT NULL DEFAULT 0,
  `potential1` int(5) NOT NULL DEFAULT 0,
  `potential2` int(5) NOT NULL DEFAULT 0,
  `potential3` int(5) NOT NULL DEFAULT 0,
  `potential4` int(5) NOT NULL DEFAULT 0,
  `potential5` int(5) NOT NULL DEFAULT 0,
  `socket1` int(5) NOT NULL DEFAULT -1,
  `socket2` int(5) NOT NULL DEFAULT -1,
  `socket3` int(5) NOT NULL DEFAULT -1,
  `incSkill` int(11) NOT NULL DEFAULT -1,
  `charmEXP` smallint(6) NOT NULL DEFAULT -1,
  `pvpDamage` smallint(6) NOT NULL DEFAULT 0,
  `power` int(11) NOT NULL DEFAULT 0,
  `overpower` int(11) NOT NULL DEFAULT 0,
  `totaldamage` int(11) NOT NULL DEFAULT 0,
  `bossdamage` int(11) NOT NULL DEFAULT 0,
  `ied` int(11) NOT NULL DEFAULT 0,
  `critdamage` int(11) NOT NULL DEFAULT 0,
  `allstat` int(11) NOT NULL DEFAULT 0,
  `ostr` bigint(20) NOT NULL DEFAULT 0,
  `odex` bigint(20) NOT NULL DEFAULT 0,
  `oint` bigint(20) NOT NULL DEFAULT 0,
  `oluk` bigint(20) NOT NULL DEFAULT 0,
  `oatk` bigint(20) NOT NULL DEFAULT 0,
  `omatk` bigint(20) NOT NULL DEFAULT 0,
  `odef` bigint(20) NOT NULL DEFAULT 0,
  `omdef` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`inventoryitemid`),
  UNIQUE KEY `pk` (`inventoryitemid`,`itemid`) USING BTREE,
  KEY `CHARID` (`characterid`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of inventoryequips
-- ----------------------------

-- ----------------------------
-- Table structure for inventoryitems
-- ----------------------------
DROP TABLE IF EXISTS `inventoryitems`;
CREATE TABLE `inventoryitems` (
  `inventoryitemid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `type` tinyint(1) NOT NULL DEFAULT 0,
  `characterid` int(11) DEFAULT NULL,
  `accountid` int(10) DEFAULT NULL,
  `itemid` int(11) NOT NULL DEFAULT 0,
  `itemname` varchar(255) DEFAULT NULL,
  `inventorytype` int(11) NOT NULL DEFAULT 0,
  `position` int(11) NOT NULL DEFAULT 0,
  `quantity` int(11) NOT NULL DEFAULT 0,
  `owner` tinytext DEFAULT NULL,
  `GM_Log` tinytext DEFAULT NULL,
  `uniqueid` int(11) NOT NULL DEFAULT -1,
  `flag` int(2) NOT NULL DEFAULT 0,
  `expiredate` bigint(20) NOT NULL DEFAULT -1,
  `sender` varchar(13) NOT NULL DEFAULT '',
  PRIMARY KEY (`inventoryitemid`),
  UNIQUE KEY `pk` (`inventoryitemid`,`itemid`) USING BTREE,
  KEY `CHARID` (`characterid`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of inventoryitems
-- ----------------------------

-- ----------------------------
-- Table structure for inventorylog
-- ----------------------------
DROP TABLE IF EXISTS `inventorylog`;
CREATE TABLE `inventorylog` (
  `inventorylogid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `inventoryitemid` int(10) unsigned NOT NULL DEFAULT 0,
  `msg` tinytext NOT NULL,
  PRIMARY KEY (`inventorylogid`),
  KEY `inventoryitemid` (`inventoryitemid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of inventorylog
-- ----------------------------

-- ----------------------------
-- Table structure for inventoryshop
-- ----------------------------
DROP TABLE IF EXISTS `inventoryshop`;
CREATE TABLE `inventoryshop` (
  `inventoryitemid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `charid` int(10) DEFAULT NULL,
  `itemid` int(11) NOT NULL DEFAULT 0,
  `itemname` varchar(255) DEFAULT NULL,
  `inventorytype` int(11) NOT NULL DEFAULT 0,
  `position` int(11) NOT NULL DEFAULT 0,
  `quantity` int(11) NOT NULL DEFAULT 0,
  `owner` tinytext DEFAULT NULL,
  `uniqueid` int(11) NOT NULL DEFAULT -1,
  `flag` int(2) NOT NULL DEFAULT 0,
  `expiredate` bigint(20) NOT NULL DEFAULT -1,
  `upgradeslots` int(11) unsigned NOT NULL DEFAULT 0,
  `level` int(11) unsigned NOT NULL DEFAULT 0,
  `str` int(11) NOT NULL DEFAULT 0,
  `dex` int(11) NOT NULL DEFAULT 0,
  `int` int(11) NOT NULL DEFAULT 0,
  `luk` int(11) NOT NULL DEFAULT 0,
  `hp` int(11) NOT NULL DEFAULT 0,
  `hpr` int(11) NOT NULL DEFAULT 0,
  `mp` int(11) NOT NULL DEFAULT 0,
  `mpr` int(11) NOT NULL DEFAULT 0,
  `watk` int(11) NOT NULL DEFAULT 0,
  `matk` int(11) NOT NULL DEFAULT 0,
  `wdef` int(11) NOT NULL DEFAULT 0,
  `mdef` int(11) NOT NULL DEFAULT 0,
  `acc` int(11) NOT NULL DEFAULT 0,
  `avoid` int(11) NOT NULL DEFAULT 0,
  `hands` int(11) NOT NULL DEFAULT 0,
  `speed` int(11) NOT NULL DEFAULT 0,
  `jump` int(11) NOT NULL DEFAULT 0,
  `ViciousHammer` tinyint(2) NOT NULL DEFAULT 0,
  `itemEXP` int(11) NOT NULL DEFAULT 0,
  `durability` mediumint(9) NOT NULL DEFAULT -1,
  `enhance` smallint(3) NOT NULL DEFAULT 0,
  `potential1` int(5) NOT NULL DEFAULT 0,
  `potential2` int(5) NOT NULL DEFAULT 0,
  `potential3` int(5) NOT NULL DEFAULT 0,
  `potential4` int(5) NOT NULL DEFAULT 0,
  `potential5` int(5) NOT NULL DEFAULT 0,
  `socket1` int(5) NOT NULL DEFAULT -1,
  `socket2` int(5) NOT NULL DEFAULT -1,
  `socket3` int(5) NOT NULL DEFAULT -1,
  `incSkill` int(11) NOT NULL DEFAULT -1,
  `charmEXP` smallint(6) NOT NULL DEFAULT -1,
  `pvpDamage` smallint(6) NOT NULL DEFAULT 0,
  `power` int(11) NOT NULL DEFAULT 0,
  `overpower` int(11) NOT NULL DEFAULT 0,
  `totaldamage` int(11) NOT NULL DEFAULT 0,
  `bossdamage` int(11) NOT NULL DEFAULT 0,
  `ied` int(11) NOT NULL DEFAULT 0,
  `critdamage` int(11) NOT NULL DEFAULT 0,
  `allstat` int(11) NOT NULL DEFAULT 0,
  `ostr` bigint(20) NOT NULL DEFAULT 0,
  `odex` bigint(20) NOT NULL DEFAULT 0,
  `oint` bigint(20) NOT NULL DEFAULT 0,
  `oluk` bigint(20) NOT NULL DEFAULT 0,
  `oatk` bigint(20) NOT NULL DEFAULT 0,
  `omatk` bigint(20) NOT NULL DEFAULT 0,
  `odef` bigint(20) NOT NULL DEFAULT 0,
  `omdef` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`inventoryitemid`),
  UNIQUE KEY `pk` (`inventoryitemid`,`itemid`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of inventoryshop
-- ----------------------------

-- ----------------------------
-- Table structure for inventoryslot
-- ----------------------------
DROP TABLE IF EXISTS `inventoryslot`;
CREATE TABLE `inventoryslot` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `characterid` int(10) unsigned DEFAULT NULL,
  `equip` tinyint(3) unsigned DEFAULT NULL,
  `use` tinyint(3) unsigned DEFAULT NULL,
  `setup` tinyint(3) unsigned DEFAULT NULL,
  `etc` tinyint(3) unsigned DEFAULT NULL,
  `cash` tinyint(3) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `characterid` (`characterid`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of inventoryslot
-- ----------------------------

-- ----------------------------
-- Table structure for inventorystorage
-- ----------------------------
DROP TABLE IF EXISTS `inventorystorage`;
CREATE TABLE `inventorystorage` (
  `inventoryitemid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `type` tinyint(1) NOT NULL DEFAULT 0,
  `characterid` int(11) DEFAULT NULL,
  `accountid` int(10) DEFAULT NULL,
  `itemid` int(11) NOT NULL DEFAULT 0,
  `itemname` varchar(255) DEFAULT NULL,
  `inventorytype` int(11) NOT NULL DEFAULT 0,
  `position` int(11) NOT NULL DEFAULT 0,
  `quantity` int(11) NOT NULL DEFAULT 0,
  `owner` tinytext DEFAULT NULL,
  `GM_Log` tinytext DEFAULT NULL,
  `uniqueid` int(11) NOT NULL DEFAULT -1,
  `flag` int(2) NOT NULL DEFAULT 0,
  `expiredate` bigint(20) NOT NULL DEFAULT -1,
  `sender` varchar(13) NOT NULL DEFAULT '',
  `upgradeslots` int(11) unsigned NOT NULL DEFAULT 0,
  `level` int(11) unsigned NOT NULL DEFAULT 0,
  `str` int(11) NOT NULL DEFAULT 0,
  `dex` int(11) NOT NULL DEFAULT 0,
  `int` int(11) NOT NULL DEFAULT 0,
  `luk` int(11) NOT NULL DEFAULT 0,
  `hp` int(11) NOT NULL DEFAULT 0,
  `hpr` int(11) NOT NULL DEFAULT 0,
  `mp` int(11) NOT NULL DEFAULT 0,
  `mpr` int(11) NOT NULL DEFAULT 0,
  `watk` int(11) NOT NULL DEFAULT 0,
  `matk` int(11) NOT NULL DEFAULT 0,
  `wdef` int(11) NOT NULL DEFAULT 0,
  `mdef` int(11) NOT NULL DEFAULT 0,
  `acc` int(11) NOT NULL DEFAULT 0,
  `avoid` int(11) NOT NULL DEFAULT 0,
  `hands` int(11) NOT NULL DEFAULT 0,
  `speed` int(11) NOT NULL DEFAULT 0,
  `jump` int(11) NOT NULL DEFAULT 0,
  `ViciousHammer` tinyint(2) NOT NULL DEFAULT 0,
  `itemEXP` int(11) NOT NULL DEFAULT 0,
  `durability` mediumint(9) NOT NULL DEFAULT -1,
  `enhance` smallint(3) NOT NULL DEFAULT 0,
  `potential1` int(5) NOT NULL DEFAULT 0,
  `potential2` int(5) NOT NULL DEFAULT 0,
  `potential3` int(5) NOT NULL DEFAULT 0,
  `potential4` int(5) NOT NULL DEFAULT 0,
  `potential5` int(5) NOT NULL DEFAULT 0,
  `socket1` int(5) NOT NULL DEFAULT -1,
  `socket2` int(5) NOT NULL DEFAULT -1,
  `socket3` int(5) NOT NULL DEFAULT -1,
  `incSkill` int(11) NOT NULL DEFAULT -1,
  `charmEXP` smallint(6) NOT NULL DEFAULT -1,
  `pvpDamage` smallint(6) NOT NULL DEFAULT 0,
  `power` int(11) NOT NULL DEFAULT 0,
  `overpower` int(11) NOT NULL DEFAULT 0,
  `totaldamage` int(11) NOT NULL DEFAULT 0,
  `bossdamage` int(11) NOT NULL DEFAULT 0,
  `ied` int(11) NOT NULL DEFAULT 0,
  `critdamage` int(11) NOT NULL DEFAULT 0,
  `allstat` int(11) NOT NULL DEFAULT 0,
  `ostr` bigint(20) NOT NULL DEFAULT 0,
  `odex` bigint(20) NOT NULL DEFAULT 0,
  `oint` bigint(20) NOT NULL DEFAULT 0,
  `oluk` bigint(20) NOT NULL DEFAULT 0,
  `oatk` bigint(20) NOT NULL DEFAULT 0,
  `omatk` bigint(20) NOT NULL DEFAULT 0,
  `odef` bigint(20) NOT NULL DEFAULT 0,
  `omdef` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`inventoryitemid`),
  UNIQUE KEY `pk` (`inventoryitemid`,`itemid`) USING BTREE,
  KEY `CHARID` (`characterid`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of inventorystorage
-- ----------------------------

-- ----------------------------
-- Table structure for ipbans
-- ----------------------------
DROP TABLE IF EXISTS `ipbans`;
CREATE TABLE `ipbans` (
  `ipbanid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `ip` varchar(40) NOT NULL DEFAULT '',
  PRIMARY KEY (`ipbanid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of ipbans
-- ----------------------------

-- ----------------------------
-- Table structure for iplog
-- ----------------------------
DROP TABLE IF EXISTS `iplog`;
CREATE TABLE `iplog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `accid` int(11) NOT NULL,
  `ip` varchar(45) NOT NULL,
  `time` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of iplog
-- ----------------------------

-- ----------------------------
-- Table structure for ipvotelog
-- ----------------------------
DROP TABLE IF EXISTS `ipvotelog`;
CREATE TABLE `ipvotelog` (
  `vid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `accid` varchar(45) NOT NULL DEFAULT '0',
  `ipaddress` varchar(60) NOT NULL DEFAULT '127.0.0.1',
  `votetime` datetime NOT NULL DEFAULT '2000-01-01 01:00:00',
  `success` tinyint(1) unsigned NOT NULL DEFAULT 0,
  `reason` varchar(45) NOT NULL,
  `isRecibido` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`vid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of ipvotelog
-- ----------------------------

-- ----------------------------
-- Table structure for keymap
-- ----------------------------
DROP TABLE IF EXISTS `keymap`;
CREATE TABLE `keymap` (
  `characterid` int(11) NOT NULL DEFAULT 0,
  `jobid` int(11) NOT NULL DEFAULT 0,
  `key` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `type` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `action` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`characterid`,`jobid`,`key`),
  KEY `keymap_ibfk_1` (`characterid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of keymap
-- ----------------------------

-- ----------------------------
-- Table structure for keymap_copy
-- ----------------------------
DROP TABLE IF EXISTS `keymap_copy`;
CREATE TABLE `keymap_copy` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `characterid` int(11) NOT NULL DEFAULT 0,
  `key` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `type` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `action` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `keymap_ibfk_1` (`characterid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of keymap_copy
-- ----------------------------

-- ----------------------------
-- Table structure for level_data
-- ----------------------------
DROP TABLE IF EXISTS `level_data`;
CREATE TABLE `level_data` (
  `charid` int(11) NOT NULL,
  `type` int(255) NOT NULL DEFAULT 0,
  `level` int(255) NOT NULL DEFAULT 1,
  `exp` bigint(255) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charid`,`type`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of level_data
-- ----------------------------

-- ----------------------------
-- Table structure for macbans
-- ----------------------------
DROP TABLE IF EXISTS `macbans`;
CREATE TABLE `macbans` (
  `macbanid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `mac` varchar(30) NOT NULL,
  PRIMARY KEY (`macbanid`),
  UNIQUE KEY `mac_2` (`mac`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of macbans
-- ----------------------------

-- ----------------------------
-- Table structure for macfilters
-- ----------------------------
DROP TABLE IF EXISTS `macfilters`;
CREATE TABLE `macfilters` (
  `macfilterid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `filter` varchar(30) NOT NULL,
  PRIMARY KEY (`macfilterid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of macfilters
-- ----------------------------

-- ----------------------------
-- Table structure for monsterbook
-- ----------------------------
DROP TABLE IF EXISTS `monsterbook`;
CREATE TABLE `monsterbook` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `charid` int(10) unsigned NOT NULL DEFAULT 0,
  `cardid` int(10) unsigned NOT NULL DEFAULT 0,
  `level` tinyint(2) unsigned DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `id` (`id`),
  KEY `charid` (`charid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of monsterbook
-- ----------------------------

-- ----------------------------
-- Table structure for mountdata
-- ----------------------------
DROP TABLE IF EXISTS `mountdata`;
CREATE TABLE `mountdata` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `characterid` int(10) unsigned DEFAULT NULL,
  `Level` int(3) unsigned NOT NULL DEFAULT 0,
  `Exp` int(10) unsigned NOT NULL DEFAULT 0,
  `Fatigue` int(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `characterid` (`characterid`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of mountdata
-- ----------------------------

-- ----------------------------
-- Table structure for mtsequipment
-- ----------------------------
DROP TABLE IF EXISTS `mtsequipment`;
CREATE TABLE `mtsequipment` (
  `inventoryequipmentid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `inventoryitemid` bigint(20) unsigned NOT NULL DEFAULT 0,
  `upgradeslots` int(11) NOT NULL DEFAULT 0,
  `level` int(11) NOT NULL DEFAULT 0,
  `str` int(11) NOT NULL DEFAULT 0,
  `dex` int(11) NOT NULL DEFAULT 0,
  `int` int(11) NOT NULL DEFAULT 0,
  `luk` int(11) NOT NULL DEFAULT 0,
  `hp` int(11) NOT NULL DEFAULT 0,
  `mp` int(11) NOT NULL DEFAULT 0,
  `watk` int(11) NOT NULL DEFAULT 0,
  `matk` int(11) NOT NULL DEFAULT 0,
  `wdef` int(11) NOT NULL DEFAULT 0,
  `mdef` int(11) NOT NULL DEFAULT 0,
  `acc` int(11) NOT NULL DEFAULT 0,
  `avoid` int(11) NOT NULL DEFAULT 0,
  `hands` int(11) NOT NULL DEFAULT 0,
  `speed` int(11) NOT NULL DEFAULT 0,
  `jump` int(11) NOT NULL DEFAULT 0,
  `ViciousHammer` tinyint(2) NOT NULL DEFAULT 0,
  `itemEXP` int(11) NOT NULL DEFAULT 0,
  `durability` int(11) NOT NULL DEFAULT -1,
  `enhance` tinyint(3) NOT NULL DEFAULT 0,
  `potential1` int(5) NOT NULL DEFAULT 0,
  `potential2` int(5) NOT NULL DEFAULT 0,
  `potential3` int(5) NOT NULL DEFAULT 0,
  `potential4` int(5) NOT NULL DEFAULT 0,
  `potential5` int(5) NOT NULL DEFAULT 0,
  `socket1` int(5) NOT NULL DEFAULT -1,
  `socket2` int(5) NOT NULL DEFAULT -1,
  `socket3` int(5) NOT NULL DEFAULT -1,
  `incSkill` int(11) NOT NULL DEFAULT -1,
  `charmEXP` smallint(6) NOT NULL DEFAULT -1,
  `pvpDamage` smallint(6) NOT NULL DEFAULT 0,
  PRIMARY KEY (`inventoryequipmentid`),
  KEY `inventoryitemid` (`inventoryitemid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of mtsequipment
-- ----------------------------

-- ----------------------------
-- Table structure for mtsitems
-- ----------------------------
DROP TABLE IF EXISTS `mtsitems`;
CREATE TABLE `mtsitems` (
  `inventoryitemid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `characterid` int(11) DEFAULT NULL,
  `accountid` int(10) DEFAULT NULL,
  `packageId` int(11) DEFAULT NULL,
  `itemid` int(11) NOT NULL DEFAULT 0,
  `inventorytype` int(11) NOT NULL DEFAULT 0,
  `position` int(11) NOT NULL DEFAULT 0,
  `quantity` int(11) NOT NULL DEFAULT 0,
  `owner` tinytext DEFAULT NULL,
  `GM_Log` tinytext DEFAULT NULL,
  `uniqueid` int(11) NOT NULL DEFAULT -1,
  `flag` int(2) NOT NULL DEFAULT 0,
  `expiredate` bigint(20) NOT NULL DEFAULT -1,
  `type` tinyint(1) NOT NULL DEFAULT 0,
  `sender` varchar(13) NOT NULL DEFAULT '',
  PRIMARY KEY (`inventoryitemid`),
  KEY `inventoryitems_ibfk_1` (`characterid`),
  KEY `characterid` (`characterid`),
  KEY `inventorytype` (`inventorytype`),
  KEY `accountid` (`accountid`),
  KEY `characterid_2` (`characterid`,`inventorytype`),
  KEY `packageid` (`packageId`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of mtsitems
-- ----------------------------

-- ----------------------------
-- Table structure for mtstransfer
-- ----------------------------
DROP TABLE IF EXISTS `mtstransfer`;
CREATE TABLE `mtstransfer` (
  `inventoryitemid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `characterid` int(11) DEFAULT NULL,
  `accountid` int(10) DEFAULT NULL,
  `packageid` int(11) DEFAULT NULL,
  `itemid` int(11) NOT NULL DEFAULT 0,
  `inventorytype` int(11) NOT NULL DEFAULT 0,
  `position` int(11) NOT NULL DEFAULT 0,
  `quantity` int(11) NOT NULL DEFAULT 0,
  `owner` tinytext DEFAULT NULL,
  `GM_Log` tinytext DEFAULT NULL,
  `uniqueid` int(11) NOT NULL DEFAULT -1,
  `flag` int(2) NOT NULL DEFAULT 0,
  `expiredate` bigint(20) NOT NULL DEFAULT -1,
  `type` tinyint(1) NOT NULL DEFAULT 0,
  `sender` varchar(13) NOT NULL DEFAULT '',
  PRIMARY KEY (`inventoryitemid`),
  KEY `inventoryitems_ibfk_1` (`characterid`),
  KEY `characterid` (`characterid`),
  KEY `inventorytype` (`inventorytype`),
  KEY `accountid` (`accountid`),
  KEY `packageid` (`packageid`),
  KEY `characterid_2` (`characterid`,`inventorytype`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of mtstransfer
-- ----------------------------

-- ----------------------------
-- Table structure for mtstransferequipment
-- ----------------------------
DROP TABLE IF EXISTS `mtstransferequipment`;
CREATE TABLE `mtstransferequipment` (
  `inventoryequipmentid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `inventoryitemid` bigint(20) unsigned NOT NULL DEFAULT 0,
  `upgradeslots` int(11) NOT NULL DEFAULT 0,
  `level` int(11) NOT NULL DEFAULT 0,
  `str` int(11) NOT NULL DEFAULT 0,
  `dex` int(11) NOT NULL DEFAULT 0,
  `int` int(11) NOT NULL DEFAULT 0,
  `luk` int(11) NOT NULL DEFAULT 0,
  `hp` int(11) NOT NULL DEFAULT 0,
  `mp` int(11) NOT NULL DEFAULT 0,
  `watk` int(11) NOT NULL DEFAULT 0,
  `matk` int(11) NOT NULL DEFAULT 0,
  `wdef` int(11) NOT NULL DEFAULT 0,
  `mdef` int(11) NOT NULL DEFAULT 0,
  `acc` int(11) NOT NULL DEFAULT 0,
  `avoid` int(11) NOT NULL DEFAULT 0,
  `hands` int(11) NOT NULL DEFAULT 0,
  `speed` int(11) NOT NULL DEFAULT 0,
  `jump` int(11) NOT NULL DEFAULT 0,
  `ViciousHammer` tinyint(2) NOT NULL DEFAULT 0,
  `itemEXP` int(11) NOT NULL DEFAULT 0,
  `durability` int(11) NOT NULL DEFAULT -1,
  `enhance` tinyint(3) NOT NULL DEFAULT 0,
  `potential1` int(5) NOT NULL DEFAULT 0,
  `potential2` int(5) NOT NULL DEFAULT 0,
  `potential3` int(5) NOT NULL DEFAULT 0,
  `potential4` int(5) NOT NULL DEFAULT 0,
  `potential5` int(5) NOT NULL DEFAULT 0,
  `socket1` int(5) NOT NULL DEFAULT -1,
  `socket2` int(5) NOT NULL DEFAULT -1,
  `socket3` int(5) NOT NULL DEFAULT -1,
  `incSkill` int(11) NOT NULL DEFAULT -1,
  `charmEXP` smallint(6) NOT NULL DEFAULT -1,
  `pvpDamage` smallint(6) NOT NULL DEFAULT 0,
  PRIMARY KEY (`inventoryequipmentid`),
  KEY `inventoryitemid` (`inventoryitemid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of mtstransferequipment
-- ----------------------------

-- ----------------------------
-- Table structure for mts_cart
-- ----------------------------
DROP TABLE IF EXISTS `mts_cart`;
CREATE TABLE `mts_cart` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `characterid` int(11) NOT NULL DEFAULT 0,
  `itemid` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `characterid` (`characterid`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of mts_cart
-- ----------------------------

-- ----------------------------
-- Table structure for mts_items
-- ----------------------------
DROP TABLE IF EXISTS `mts_items`;
CREATE TABLE `mts_items` (
  `id` int(11) NOT NULL,
  `tab` tinyint(1) NOT NULL DEFAULT 1,
  `price` int(11) NOT NULL DEFAULT 0,
  `characterid` int(11) NOT NULL DEFAULT 0,
  `seller` varchar(13) NOT NULL DEFAULT '',
  `expiration` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of mts_items
-- ----------------------------

-- ----------------------------
-- Table structure for notes
-- ----------------------------
DROP TABLE IF EXISTS `notes`;
CREATE TABLE `notes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `to` varchar(13) NOT NULL DEFAULT '',
  `from` varchar(13) NOT NULL DEFAULT '',
  `message` text NOT NULL,
  `timestamp` bigint(20) unsigned NOT NULL,
  `gift` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `to` (`to`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of notes
-- ----------------------------

-- ----------------------------
-- Table structure for nxcode
-- ----------------------------
DROP TABLE IF EXISTS `nxcode`;
CREATE TABLE `nxcode` (
  `code` varchar(15) NOT NULL,
  `valid` int(11) NOT NULL DEFAULT 1,
  `user` varchar(13) DEFAULT NULL,
  `type` int(11) NOT NULL DEFAULT 0,
  `item` int(11) NOT NULL DEFAULT 10000,
  PRIMARY KEY (`code`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of nxcode
-- ----------------------------

-- ----------------------------
-- Table structure for pets
-- ----------------------------
DROP TABLE IF EXISTS `pets`;
CREATE TABLE `pets` (
  `petid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT '',
  `level` int(3) unsigned NOT NULL,
  `closeness` int(6) unsigned NOT NULL,
  `fullness` int(3) unsigned NOT NULL,
  `seconds` int(11) NOT NULL DEFAULT 0,
  `flags` smallint(5) NOT NULL DEFAULT 0,
  PRIMARY KEY (`petid`),
  KEY `petid` (`petid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of pets
-- ----------------------------

-- ----------------------------
-- Table structure for playeritems
-- ----------------------------
DROP TABLE IF EXISTS `playeritems`;
CREATE TABLE `playeritems` (
  `charid` int(11) NOT NULL,
  `itemid` int(11) NOT NULL,
  `amount` bigint(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`charid`,`itemid`,`amount`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of playeritems
-- ----------------------------

-- ----------------------------
-- Table structure for playernpcs
-- ----------------------------
DROP TABLE IF EXISTS `playernpcs`;
CREATE TABLE `playernpcs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(13) NOT NULL,
  `hair` int(11) NOT NULL,
  `face` int(11) NOT NULL,
  `skin` int(11) NOT NULL,
  `x` int(11) NOT NULL DEFAULT 0,
  `y` int(11) NOT NULL DEFAULT 0,
  `map` int(11) NOT NULL,
  `charid` int(11) NOT NULL,
  `scriptid` int(11) NOT NULL,
  `foothold` int(11) NOT NULL,
  `dir` tinyint(1) NOT NULL DEFAULT 0,
  `gender` tinyint(1) NOT NULL DEFAULT 0,
  `pets` varchar(25) DEFAULT '0,0,0',
  PRIMARY KEY (`id`),
  KEY `scriptid` (`scriptid`),
  KEY `playernpcs_ibfk_1` (`charid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of playernpcs
-- ----------------------------

-- ----------------------------
-- Table structure for playernpcs_equip
-- ----------------------------
DROP TABLE IF EXISTS `playernpcs_equip`;
CREATE TABLE `playernpcs_equip` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `npcid` int(11) NOT NULL,
  `equipid` int(11) NOT NULL,
  `equippos` int(11) NOT NULL,
  `charid` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `playernpcs_equip_ibfk_1` (`charid`),
  KEY `playernpcs_equip_ibfk_2` (`npcid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of playernpcs_equip
-- ----------------------------

-- ----------------------------
-- Table structure for player_blacklist
-- ----------------------------
DROP TABLE IF EXISTS `player_blacklist`;
CREATE TABLE `player_blacklist` (
  `charid` int(11) NOT NULL,
  `itemid` int(11) NOT NULL,
  PRIMARY KEY (`charid`,`itemid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of player_blacklist
-- ----------------------------

-- ----------------------------
-- Table structure for player_pals
-- ----------------------------
DROP TABLE IF EXISTS `player_pals`;
CREATE TABLE `player_pals` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `charid` int(10) unsigned DEFAULT 0,
  `template_id` smallint(6) DEFAULT 0,
  `pos` enum('ACTIVE','STORAGE','HATCHING') DEFAULT NULL,
  `name` varchar(16) DEFAULT NULL,
  `tier` tinyint(4) unsigned DEFAULT 0,
  `rank` tinyint(4) unsigned DEFAULT 0,
  `element` int(11) DEFAULT -1,
  `gender` int(11) DEFAULT 0,
  `level` int(11) DEFAULT 0,
  `exp` bigint(20) DEFAULT 0,
  `hp` int(11) DEFAULT 0,
  `str` int(11) DEFAULT 0,
  `dex` int(11) DEFAULT 0,
  `int` int(11) DEFAULT 0,
  `luk` int(11) DEFAULT 0,
  `atk` int(11) DEFAULT 0,
  `matk` int(11) DEFAULT 0,
  `def` int(11) DEFAULT 0,
  `mdef` int(11) DEFAULT 0,
  `ability_1` smallint(6) DEFAULT 0,
  `ability_2` smallint(6) DEFAULT 0,
  `ability_3` smallint(6) DEFAULT 0,
  `ability_4` smallint(6) DEFAULT 0,
  `iv_hp` smallint(6) unsigned DEFAULT 0,
  `iv_str` smallint(6) unsigned DEFAULT 0,
  `iv_dex` smallint(6) unsigned DEFAULT 0,
  `iv_int` smallint(6) unsigned DEFAULT 0,
  `iv_luk` smallint(6) unsigned DEFAULT 0,
  `iv_atk` smallint(6) unsigned DEFAULT 0,
  `iv_matk` smallint(6) unsigned DEFAULT 0,
  `iv_def` smallint(6) unsigned DEFAULT 0,
  `iv_mdef` smallint(6) unsigned DEFAULT 0,
  `hatch_start_time` timestamp NULL DEFAULT current_timestamp(),
  `acc_1` int(11) DEFAULT NULL,
  `acc_2` int(11) DEFAULT 0,
  `acc_3` int(11) DEFAULT 0,
  `acc_4` int(11) DEFAULT 0,
  `upgrades` int(11) DEFAULT 100,
  `speed` int(11) DEFAULT 100,
  `last_breed_time` timestamp NULL DEFAULT current_timestamp(),
  `born` bigint(20) DEFAULT 0,
  `battle` int(11) DEFAULT 0,
  `skill` int(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_charid` (`charid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of player_pals
-- ----------------------------

-- ----------------------------
-- Table structure for pokemon
-- ----------------------------
DROP TABLE IF EXISTS `pokemon`;
CREATE TABLE `pokemon` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `monsterid` int(11) NOT NULL DEFAULT 0,
  `characterid` int(11) NOT NULL DEFAULT 0,
  `level` smallint(3) NOT NULL DEFAULT 1,
  `exp` int(11) NOT NULL DEFAULT 0,
  `name` varchar(255) NOT NULL DEFAULT '',
  `nature` tinyint(3) NOT NULL DEFAULT 0,
  `active` tinyint(1) NOT NULL DEFAULT 0,
  `accountid` int(11) NOT NULL DEFAULT 0,
  `itemid` int(11) NOT NULL DEFAULT 0,
  `gender` tinyint(2) NOT NULL DEFAULT -1,
  `hpiv` tinyint(3) NOT NULL DEFAULT -1,
  `atkiv` tinyint(3) NOT NULL DEFAULT -1,
  `defiv` tinyint(3) NOT NULL DEFAULT -1,
  `spatkiv` tinyint(3) NOT NULL DEFAULT -1,
  `spdefiv` tinyint(3) NOT NULL DEFAULT -1,
  `speediv` tinyint(3) NOT NULL DEFAULT -1,
  `evaiv` tinyint(3) NOT NULL DEFAULT -1,
  `acciv` tinyint(3) NOT NULL DEFAULT -1,
  `ability` tinyint(2) NOT NULL DEFAULT -1,
  PRIMARY KEY (`id`),
  KEY `id` (`id`),
  KEY `characterid` (`characterid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of pokemon
-- ----------------------------

-- ----------------------------
-- Table structure for pwreset
-- ----------------------------
DROP TABLE IF EXISTS `pwreset`;
CREATE TABLE `pwreset` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(14) NOT NULL,
  `email` varchar(100) NOT NULL,
  `confirmkey` varchar(100) NOT NULL,
  `status` tinyint(1) unsigned NOT NULL DEFAULT 0,
  `timestamp` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of pwreset
-- ----------------------------

-- ----------------------------
-- Table structure for questinfo
-- ----------------------------
DROP TABLE IF EXISTS `questinfo`;
CREATE TABLE `questinfo` (
  `questinfoid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `characterid` int(11) NOT NULL DEFAULT 0,
  `quest` int(6) NOT NULL DEFAULT 0,
  `customData` varchar(555) DEFAULT NULL,
  PRIMARY KEY (`questinfoid`),
  KEY `characterid` (`characterid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of questinfo
-- ----------------------------

-- ----------------------------
-- Table structure for questlock
-- ----------------------------
DROP TABLE IF EXISTS `questlock`;
CREATE TABLE `questlock` (
  `accid` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `time` bigint(36) NOT NULL,
  PRIMARY KEY (`accid`,`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of questlock
-- ----------------------------

-- ----------------------------
-- Table structure for quests
-- ----------------------------
DROP TABLE IF EXISTS `quests`;
CREATE TABLE `quests` (
  `accountid` int(11) NOT NULL DEFAULT 0,
  `questid` int(9) NOT NULL DEFAULT 0,
  PRIMARY KEY (`accountid`,`questid`),
  KEY `accountid` (`accountid`),
  KEY `achievementid` (`questid`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of quests
-- ----------------------------

-- ----------------------------
-- Table structure for queststatus
-- ----------------------------
DROP TABLE IF EXISTS `queststatus`;
CREATE TABLE `queststatus` (
  `queststatusid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `characterid` int(11) NOT NULL DEFAULT 0,
  `quest` int(6) NOT NULL DEFAULT 0,
  `status` tinyint(4) NOT NULL DEFAULT 0,
  `time` int(11) NOT NULL DEFAULT 0,
  `forfeited` int(11) NOT NULL DEFAULT 0,
  `customData` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`queststatusid`),
  KEY `characterid` (`characterid`),
  KEY `queststatusid` (`queststatusid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of queststatus
-- ----------------------------

-- ----------------------------
-- Table structure for queststatusmobs
-- ----------------------------
DROP TABLE IF EXISTS `queststatusmobs`;
CREATE TABLE `queststatusmobs` (
  `queststatusmobid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `queststatusid` int(10) unsigned NOT NULL DEFAULT 0,
  `mob` int(11) NOT NULL DEFAULT 0,
  `count` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`queststatusmobid`),
  KEY `queststatusid` (`queststatusid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of queststatusmobs
-- ----------------------------

-- ----------------------------
-- Table structure for quest_items
-- ----------------------------
DROP TABLE IF EXISTS `quest_items`;
CREATE TABLE `quest_items` (
  `charid` int(11) NOT NULL,
  `status` int(11) NOT NULL DEFAULT 0,
  `level` int(11) NOT NULL DEFAULT 0,
  `item1` int(11) NOT NULL DEFAULT 0,
  `item2` int(11) NOT NULL DEFAULT 0,
  `item3` int(11) NOT NULL DEFAULT 0,
  `item4` int(11) NOT NULL DEFAULT 0,
  `item5` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of quest_items
-- ----------------------------

-- ----------------------------
-- Table structure for regrocklocations
-- ----------------------------
DROP TABLE IF EXISTS `regrocklocations`;
CREATE TABLE `regrocklocations` (
  `trockid` int(11) NOT NULL AUTO_INCREMENT,
  `characterid` int(11) DEFAULT NULL,
  `mapid` int(11) DEFAULT NULL,
  PRIMARY KEY (`trockid`),
  KEY `characterid` (`characterid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of regrocklocations
-- ----------------------------

-- ----------------------------
-- Table structure for reports
-- ----------------------------
DROP TABLE IF EXISTS `reports`;
CREATE TABLE `reports` (
  `reportid` int(9) NOT NULL AUTO_INCREMENT,
  `characterid` int(11) NOT NULL DEFAULT 0,
  `type` tinyint(2) NOT NULL DEFAULT 0,
  `count` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`reportid`,`characterid`),
  KEY `characterid` (`characterid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of reports
-- ----------------------------

-- ----------------------------
-- Table structure for rings
-- ----------------------------
DROP TABLE IF EXISTS `rings`;
CREATE TABLE `rings` (
  `ringid` int(11) NOT NULL AUTO_INCREMENT,
  `partnerRingId` int(11) NOT NULL DEFAULT 0,
  `partnerChrId` int(11) NOT NULL DEFAULT 0,
  `itemid` int(11) NOT NULL DEFAULT 0,
  `partnername` varchar(255) NOT NULL,
  PRIMARY KEY (`ringid`),
  KEY `ringid` (`ringid`),
  KEY `partnerChrId` (`partnerChrId`),
  KEY `partnerRingId` (`partnerRingId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of rings
-- ----------------------------

-- ----------------------------
-- Table structure for savedlocations
-- ----------------------------
DROP TABLE IF EXISTS `savedlocations`;
CREATE TABLE `savedlocations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `characterid` int(11) NOT NULL,
  `locationtype` int(11) NOT NULL DEFAULT 0,
  `map` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `savedlocations_ibfk_1` (`characterid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of savedlocations
-- ----------------------------

-- ----------------------------
-- Table structure for scroll_log
-- ----------------------------
DROP TABLE IF EXISTS `scroll_log`;
CREATE TABLE `scroll_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `accId` int(11) NOT NULL DEFAULT 0,
  `chrId` int(11) NOT NULL DEFAULT 0,
  `scrollId` int(11) NOT NULL DEFAULT 0,
  `itemId` int(11) NOT NULL DEFAULT 0,
  `oldSlots` tinyint(4) NOT NULL DEFAULT 0,
  `newSlots` tinyint(4) NOT NULL DEFAULT 0,
  `hammer` tinyint(4) NOT NULL DEFAULT 0,
  `result` varchar(13) NOT NULL DEFAULT '',
  `whiteScroll` tinyint(1) NOT NULL DEFAULT 0,
  `legendarySpirit` tinyint(1) NOT NULL DEFAULT 0,
  `vegaId` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of scroll_log
-- ----------------------------

-- ----------------------------
-- Table structure for skillmacros
-- ----------------------------
DROP TABLE IF EXISTS `skillmacros`;
CREATE TABLE `skillmacros` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `characterid` int(11) NOT NULL DEFAULT 0,
  `position` tinyint(1) NOT NULL DEFAULT 0,
  `skill1` int(11) NOT NULL DEFAULT 0,
  `skill2` int(11) NOT NULL DEFAULT 0,
  `skill3` int(11) NOT NULL DEFAULT 0,
  `name` varchar(30) DEFAULT NULL,
  `shout` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `characterid` (`characterid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of skillmacros
-- ----------------------------

-- ----------------------------
-- Table structure for skills
-- ----------------------------
DROP TABLE IF EXISTS `skills`;
CREATE TABLE `skills` (
  `skillid` int(11) NOT NULL DEFAULT 0,
  `characterid` int(11) NOT NULL DEFAULT 0,
  `skilllevel` int(11) NOT NULL DEFAULT 0,
  `masterlevel` int(11) NOT NULL DEFAULT 0,
  `expiration` bigint(20) NOT NULL DEFAULT -1,
  `level` int(11) NOT NULL DEFAULT 1,
  `exp` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`characterid`,`skillid`),
  KEY `skills_ibfk_1` (`characterid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of skills
-- ----------------------------

-- ----------------------------
-- Table structure for skills_cooldowns
-- ----------------------------
DROP TABLE IF EXISTS `skills_cooldowns`;
CREATE TABLE `skills_cooldowns` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `charid` int(11) NOT NULL,
  `SkillID` int(11) NOT NULL,
  `length` bigint(20) NOT NULL,
  `StartTime` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `charid` (`charid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of skills_cooldowns
-- ----------------------------

-- ----------------------------
-- Table structure for speedruns
-- ----------------------------
DROP TABLE IF EXISTS `speedruns`;
CREATE TABLE `speedruns` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(13) NOT NULL,
  `leader` varchar(13) NOT NULL,
  `timestring` varchar(1024) NOT NULL,
  `time` bigint(20) NOT NULL DEFAULT 0,
  `members` varchar(1024) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of speedruns
-- ----------------------------

-- ----------------------------
-- Table structure for stake
-- ----------------------------
DROP TABLE IF EXISTS `stake`;
CREATE TABLE `stake` (
  `accid` int(11) NOT NULL,
  `amount` int(11) DEFAULT NULL,
  `starttime` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp(),
  PRIMARY KEY (`accid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of stake
-- ----------------------------

-- ----------------------------
-- Table structure for storages
-- ----------------------------
DROP TABLE IF EXISTS `storages`;
CREATE TABLE `storages` (
  `storageid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `accountid` int(11) NOT NULL DEFAULT 0,
  `slots` int(11) NOT NULL DEFAULT 0,
  `meso` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`storageid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of storages
-- ----------------------------

-- ----------------------------
-- Table structure for timer
-- ----------------------------
DROP TABLE IF EXISTS `timer`;
CREATE TABLE `timer` (
  `time` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`time`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of timer
-- ----------------------------

-- ----------------------------
-- Table structure for tournamentlog
-- ----------------------------
DROP TABLE IF EXISTS `tournamentlog`;
CREATE TABLE `tournamentlog` (
  `logid` int(11) NOT NULL AUTO_INCREMENT,
  `winnerid` int(11) NOT NULL DEFAULT 0,
  `numContestants` int(11) NOT NULL DEFAULT 0,
  `when` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`logid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of tournamentlog
-- ----------------------------

-- ----------------------------
-- Table structure for trocklocations
-- ----------------------------
DROP TABLE IF EXISTS `trocklocations`;
CREATE TABLE `trocklocations` (
  `trockid` int(11) NOT NULL AUTO_INCREMENT,
  `characterid` int(11) DEFAULT NULL,
  `mapid` int(11) DEFAULT NULL,
  PRIMARY KEY (`trockid`),
  KEY `characterid` (`characterid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of trocklocations
-- ----------------------------

-- ----------------------------
-- Table structure for wishlist
-- ----------------------------
DROP TABLE IF EXISTS `wishlist`;
CREATE TABLE `wishlist` (
  `characterid` int(11) NOT NULL,
  `sn` int(11) NOT NULL,
  KEY `characterid` (`characterid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

-- ----------------------------
-- Records of wishlist
-- ----------------------------
SET FOREIGN_KEY_CHECKS=1;
