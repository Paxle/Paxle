connect 'jdbc:derby:/home/theli/paxle/workspace/Debug/command-db';

create table CrawledCommand (id integer not null, result varchar(255), resultText varchar(255), location varchar(512), profileID integer default -1, depth integer default 0, primary key (id));

CREATE INDEX CRAWLED_LOCATION_IDX on CrawledCommand (location);
 
create table EnqueuedCommand (id integer not null, result varchar(255), resultText varchar(255), location varchar(512), profileID integer default -1, depth integer default 0, primary key (id));

CREATE INDEX ENQUEUED_LOCATION_IDX on EnqueuedCommand (location);

INSERT INTO EnqueuedCommand (id, result, resultText, location, profileID, depth) SELECT id, result, resultText, location, profileID, depth FROM COMMAND WHERE resultText is null;

INSERT INTO CrawledCommand (id, result, resultText, location, profileID, depth) SELECT id, result, resultText, location, profileID, depth FROM COMMAND WHERE resultText is not null;

DROP TABLE Command;
