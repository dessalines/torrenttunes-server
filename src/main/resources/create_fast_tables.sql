DROP TABLE IF EXISTS song\_view\_fast;

CREATE TABLE song\_view\_fast select * from song\_view;

DROP TABLE IF EXISTS song\_view\_grouped\_fast;

CREATE TABLE song\_view\_grouped\_fast select * from song\_view\_grouped;

DROP TABLE IF EXISTS album\_view\_fast;

CREATE TABLE album\_view\_fast select * from album\_view;

DROP TABLE IF EXISTS artist\_search\_view\_fast; 

CREATE TABLE artist\_search\_view\_fast select * from artist\_search\_view;

