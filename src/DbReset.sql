DROP TABLE IF EXISTS Party, Entertainment, Menu, Venue;

CREATE TABLE Venue (
	vid SERIAL,
	name TEXT,
	venuecost MONEY,
	
	PRIMARY KEY (vid),
	
	CHECK (venuecost >= CAST(0.0 AS MONEY))
);

CREATE TABLE Menu (
	mid SERIAL,
	description TEXT,
	costprice MONEY,
	
	PRIMARY KEY (mid),
	
	CHECK (costprice >= CAST(0.0 AS MONEY))
);

CREATE TABLE Entertainment (
	eid SERIAL,
	description TEXT,
	costprice MONEY,
	
	PRIMARY KEY (eid),
	
	CHECK (costprice >= CAST(0.0 AS MONEY))
);

CREATE TABLE Party (
	pid SERIAL,
	name TEXT,
	mid INTEGER,
	vid INTEGER,
	eid INTEGER,
	price MONEY,
	timing TIMESTAMP,
	numberofguests INTEGER,
	
	PRIMARY KEY (pid),
	FOREIGN KEY (mid) REFERENCES Menu(mid),
	FOREIGN KEY (vid) REFERENCES Venue(vid),
	FOREIGN KEY (eid) REFERENCES Entertainment(eid),
	
	CHECK (price >= CAST(0.0 AS MONEY)),
	CHECK (numberofguests >= 0)
);
