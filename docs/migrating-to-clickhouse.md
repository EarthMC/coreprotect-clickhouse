# Migrating to coreprotect-clickhouse
###### *Note: This guide is written for and tested with version 22.4 of CoreProtect, and version 1.21.4 of Minecraft*

This guide walks you through importing an existing CoreProtect database in MySQL into ClickHouse. Importing SQLite data is not supported, as support for that is dropped completely in this fork.

## Prerequisites
- A functional ClickHouse server that is able to connect to your MySQL server using password authentication.
- Time.

## 1. System Properties
Before doing anything, it's recommended to add the following system property to your server:

```
-net.earthmc.coreprotect-clickhouse.database.read-only=true
```

This flag prevents new data from being written to the database during the setup and conversion process that would otherwise cause inconsistencies..

## 2. Configuration
Upon first starting your server with the plugin, a new coreprotect-clickhouse folder will be created in the plugins folder where you'll find the config. Configure your database credentials as usual in order
to connect the plugin to your ClickHouse server, make sure that the `use-mysql` option remains `true`. Also make sure to carry over any config options you might have changed in the original CoreProtect
configuration that you don't want to lose.

### Partitioning
A new config option added by this fork is `clickhouse-partitioning`, this option is used to define what partitioning to use for the database tables, refer to the
[ClickHouse docs](https://clickhouse.com/docs/engines/table-engines/mergetree-family/custom-partitioning-key) for more information. The default value for this option is
`toStartOfInterval(parseDateTimeBestEffort(toString(time), 0, 'UTC'), toIntervalQuarter(2))` (6 months) and only applies to the following tables: block, item, container, entity.

If you decide to change this option, the tables that use partitioning will have to be re-created for it to take effect.

## 3. The `/co convert` command
In order to make the actual migration possible, a new /co convert command is added that is usable by console.

### Logging in
To get started, you must first log in to your MySQL server containing the CoreProtect data using the `/co convert login <address> <database> <username> <password>` command. The plugin will then attempt
to connect to your MySQL database and will print its CoreProtect upon connecting successfully.

You can check your login status at any time by using `/co convert` (no arguments).

> [!WARNING]  
> Login credentials are automatically persisted in the `mysql-credentials.json` file, you should delete this file once you are done with the migration.

### Preparing the `row-numbers.json` file
In order to stay compatible with CoreProtect's API/ABI, MySQL's auto increment feature is simulated by keeping track of counters for each table locally. These row counts must be populated manually
using the `/co prepare` command in order to prevent multiple rows in a table from having the same row id.

This functionality may be replaced in the future by randomly generated snowflakes, which are better suited for this sort of thing.

## 4. Table migration order

### Required Tables
The following tables are required to be migrated first. It's important to restart your server once these are imported, so that the different item/entity mappings can be applied.

Once these tables are migrated, you could already disable the read-only flag and re-open the server for players while the rest of the migrations happen in the background.

```
co convert blockdata_map -t
co convert material_map -t
co convert entity_map -t
co convert art_map -t
co convert world -t
co convert user -t
co convert username_log -t
```

### Secondary Tables

```
co convert version -t
co convert chat
co convert command
co convert session
co convert sign
co convert skull
co convert item
co convert container
co convert block
```

#### Notes
- Migrating the version table is not necessary but is included for completeness.
- Regarding entities, see the section below.
- When using a local file-based backend for ClickHouse it may be possible to run into a too many open files error when importing the container table, if this happens you can temporarily change the
data type to string.
```
truncate table co_container
alter table co_container alter column metadata type String
<run import>
alter table co_container update metadata = '{}' where metadata = ''
alter table co_container alter column metadata type JSON
```

### A note about entities
The way that CoreProtect currently stores important data about items and entities is currently quite brittle to say the least, due to spigot API changes in 1.21.3 any entity metadata saved
in older versions is now unreadable in new versions.

It's important to note that such entities can still be rolled back, they will just be without any metadata (such as villager profession/trades for villagers).

A converter exists for post 1.21.3 entity data, but it locks up the main thread due to having to individually spawn in each entity at the place where it was killed, and is so far untested.
If you attempt to run this converter, you should add `-Ddisable.watchdog=true` to your system properties to prevent the server from being forcefully killed, and use a superflat world
to prevent delays due to world generation.
