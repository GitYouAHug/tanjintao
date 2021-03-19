/*
Navicat MySQL Data Transfer

Source Server         : ranger
Source Server Version : 50721
Source Host           : localhost:3306
Source Database       : linuxusers

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

Date: 2019-07-25 16:12:28
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for clusterinfo
-- ----------------------------
DROP TABLE IF EXISTS `clusterinfo`;
CREATE TABLE `clusterinfo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cluster` varchar(50) DEFAULT NULL,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for hostinfo
-- ----------------------------
DROP TABLE IF EXISTS `hostinfo`;
CREATE TABLE `hostinfo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(50) DEFAULT NULL,
  `host_name` varchar(50) DEFAULT NULL,
  `host_version` varchar(50) DEFAULT NULL,
  `cluster` varchar(50) DEFAULT NULL,
  `description` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for linuxconninfo
-- ----------------------------
DROP TABLE IF EXISTS `linuxconninfo`;
CREATE TABLE `linuxconninfo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` varchar(100) DEFAULT NULL,
  `passwd` mediumtext,
  `linuxPasswd` mediumtext,
  `ip` varchar(100) DEFAULT NULL,
  `host_name` varchar(50) DEFAULT NULL,
  `cluster` varchar(50) DEFAULT NULL,
  `flag` varchar(20) DEFAULT NULL,
  `description` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=110 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for linuxgroupsinfo
-- ----------------------------
DROP TABLE IF EXISTS `linuxgroupsinfo`;
CREATE TABLE `linuxgroupsinfo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupName` varchar(50) DEFAULT NULL,
  `node` varchar(50) DEFAULT NULL,
  `cluster` varchar(50) DEFAULT NULL,
  `description` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4096 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for linuxusersinfo
-- ----------------------------
DROP TABLE IF EXISTS `linuxusersinfo`;
CREATE TABLE `linuxusersinfo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `userName` varchar(50) DEFAULT NULL,
  `passwd` mediumtext,
  `groups` varchar(50) DEFAULT NULL,
  `node` varchar(50) DEFAULT NULL,
  `cluster` varchar(50) DEFAULT NULL,
  `description` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4029 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for test
-- ----------------------------
DROP TABLE IF EXISTS `test`;
CREATE TABLE `test` (
  `username` varchar(100) DEFAULT NULL,
  `passwd` varchar(100) DEFAULT NULL,
  `group` varchar(110) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
