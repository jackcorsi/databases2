DROP TABLE IF EXISTS Party

CREATE TABLE Party (
	pid INTEGER,
	name TEXT,
	mid INTEGER,
	vid INTEGER,
	eid INTEGER,
	price MONEY,
	timing DATETIME,
	numberofguests INTEGER,
	
	PRIMARY KEY (pid),
	FOREIGN KEY (mid) REFERENCES Menu(mid),
	FOREIGN KEY (vid) REFERENCES Venue(vid),
	FOREIGN KEY (eid) REFERENCES Entertainment(eid),
	
	CHECK (price >= 0),
	CHECK (numberofguests >= 0)
)

DROP TABLE IF EXISTS Venue

CREATE TABLE Venue (
	vid INTEGER,
	name TEXT,
	venuecost MONEY,
	
	PRIMARY KEY (vid),
	
	CHECK (venuecost >= 0)
)

DROP TABLE IF EXISTS Menu 

CREATE TABLE Menu (
	mid INTEGER,
	description TEXT,
	costprice MONEY,
	
	CHECK (costprice >= 0)
)

DROP TABLE IF EXISTS Entertainment

CREATE TABLE Entertainment (
	eid INTEGER,
	description TEXT,
	costprice MONEY,
	
	CHECK (costprice >= 0)
)