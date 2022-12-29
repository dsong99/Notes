
DROP TABLE stock_holdings IF EXISTS;


CREATE TABLE stock_holdings (
	ID MEDIUMINT NOT NULL PRIMARY KEY,
    Symbol VARCHAR(10),
    Name VARCHAR(200),
    LastSale VARCHAR(10),
    NetChange VARCHAR(10),
    PercentChange VARCHAR(10),
    MarketCap VARCHAR(30),
    Country VARCHAR(20),
    IPOYear VARCHAR(10),
    Volume VARCHAR(20),
    Sector VARCHAR(30),
    Industry VARCHAR(100)
);

ALTER TABLE stock_holdings AUTO_INCREMENT = 0
