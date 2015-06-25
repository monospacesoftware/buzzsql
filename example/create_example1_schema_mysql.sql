CREATE DATABASE IF NOT EXISTS example1;
USE example1;

DROP TABLE IF EXISTS `table_example1`;
DROP PROCEDURE IF EXISTS `storedproc_example1`;

CREATE TABLE `table_example1` (
  `col_pk` int(10) unsigned NOT NULL auto_increment,
  `col_string` varchar(256) default NULL,
  `col_int` int(10) unsigned default NULL,
  `col_date` datetime default NULL,
  PRIMARY KEY  (`col_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DELIMITER $$

CREATE PROCEDURE `storedproc_example1`(
  IN in_param_str varchar(256),
  INOUT inout_param_int integer,
  OUT out_param_string varchar(256)
)
BEGIN

  IF inout_param_int > 10 THEN
    SET out_param_string = 'GREATER';
  ELSE
    SET out_param_string = 'LESS';
  END IF;

  SET inout_param_int = 10;

  SELECT current_date(), rand() from dual;

END $$

DELIMITER ;
