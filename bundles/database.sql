SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

-- 
-- Datenbank: `pastebin`
-- 

-- --------------------------------------------------------

-- 
-- Tabellenstruktur für Tabelle `languages`
-- 

CREATE TABLE `languages` (
  `id` tinyint(4) NOT NULL auto_increment,
  `language` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

-- 
-- Tabellenstruktur für Tabelle `paste`
-- 

CREATE TABLE `paste` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `language` tinyint(4) NOT NULL,
  `nick` varchar(40) NOT NULL,
  `subject` varchar(255) NOT NULL,
  `paste` text NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1;
