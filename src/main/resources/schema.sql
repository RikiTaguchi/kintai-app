CREATE TABLE IF NOT EXISTS works (
	id UUID,
	userId UUID,
	date char(10),
	dayOfWeek char(1),
	classM Boolean,
	classK Boolean,
	classS Boolean,
	classA Boolean,
	classB Boolean,
	classC Boolean,
	classD Boolean,
	classCount int,
	helpArea varchar(256),
	timeStart char(5),
	timeEnd char(5),
	breakTime int,
	officeTimeStart char(5),
	officeTimeEnd char(5),
	officeTime int,
	otherWork varchar(256),
	otherTimeStart char(5),
	otherTimeEnd char(5),
	otherBreakTime int,
	otherTime int,
	carfare int,
	outOfTime int,
	overTime int,
	nightTime int,
	supportSalary varchar(5)
);

CREATE TABLE IF NOT EXISTS users (
	id UUID,
	loginId varchar(256),
	userName varchar(256),
	password varchar(256),
	classAreaId UUID
);

CREATE TABLE IF NOT EXISTS managers (
	id UUID,
	loginId varchar(256),
	password varchar(256),
	classArea varchar(256)
);

CREATE TABLE IF NOT EXISTS salaries (
	id UUID,
	userId UUID,
	dateFrom varchar(10),
	classSalary int,
	officeSalary int,
	supportSalary int,
	carfare int
);

CREATE TABLE IF NOT EXISTS worktemplates (
	id UUID,
	userId UUID,
	title varchar(256),
	classM Boolean,
	classK Boolean,
	classS Boolean,
	classA Boolean,
	classB Boolean,
	classC Boolean,
	classD Boolean,
	helpArea varchar(256),
	timeStart char(5),
	timeEnd char(5),
	breakTime int,
	officeTimeStart char(5),
	officeTimeEnd char(5),
	otherWork varchar(256),
	otherTimeStart char(5),
	otherTimeEnd char(5),
	otherBreakTime int,
	carfare int
);
