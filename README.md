# MySQL で大量データを扱うフィージビリティスタディ


## データベースセットアップ

1. MySQLサーバーに接続

```bash
   % mysql -u root -h 127.0.0.1   
   Welcome to the MySQL monitor.  Commands end with ; or \g.
   Your MySQL connection id is 84
   Server version: 5.7.23 MySQL Community Server (GPL)
   
   Copyright (c) 2000, 2018, Oracle and/or its affiliates. All rights reserved.
   
   Oracle is a registered trademark of Oracle Corporation and/or its
   affiliates. Other names may be trademarks of their respective
   owners.
   
   Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.
```
  
1. データベースの作成

```mysql
CREATE DATABASE `sample` CHARACTER SET utf8;
```

1. ユーザーの作成

```mysql
CREATE USER sample@'localhost' IDENTIFIED BY 'sample';
CREATE USER sample@'%' IDENTIFIED BY 'sample';
 
GRANT ALL PRIVILEGES ON sample.* TO sample@'localhost';
GRANT ALL PRIVILEGES ON sample.* TO sample@'%';
```

## テーブルの準備

```mysql
DROP TABLE IF EXISTS serial_number;
CREATE TABLE serial_number (
    SERIAL_NUMBER varchar(14),
    DEVICE_ID varchar(16),
    MAC_ADDRESS varchar(12),
    UPDATE_DATE datetime,
    PRIMARY KEY (SERIAL_NUMBER, DEVICE_ID)
);
```

## データベースにデータを投入

```mysql
mysql> LOAD DATA INFILE 'data.csv' INTO TABLE serial_number FIELDS TERMINATED BY ',' ENCLOSED BY '"';
Query OK, 21747173 rows affected (1 min 47.56 sec)
Records: 21747173 Deleted: 0 Skipped: 0 Warnings: 0
```

標準入力を使用して登録する

```mysql
mysql> LOAD DATA INFILE 'data.csv' INTO TABLE serial_number FIELDS TERMINATED BY ',' ENCLOSED BY '"';
Query OK, 21747173 rows affected (1 min 47.56 sec)
Records: 21747173 Deleted: 0 Skipped: 0 Warnings: 0
```

```bash
cat data.csv | mysql -h127.0.0.1 -u ncms -p ncms-devel -e "LOAD DATA LOCAL INFILE  '/dev/stdin'  INTO TABLE serial_number FIELDS TERMINATED BY ',' ENCLOSED BY '\"';"
```

## 数十万件レベルので結合

### 結合する対象を一時テーブルに登録して結合

```sql
CREATE TEMPORARY TABLE tmp_serial_number (
    serial_number varchar(14) primary key
);
INSERT INTO tmp_serial_number SELECT serial_number FROM serial_number LIMIT 100000;
```

```sql
mysql> SELECT
    A.serial_number,
    B.device_id
FROM
    tmp_serial_number A
LEFT OUTER JOIN serial_number B
    ON A.serial_number = B.serial_number
100000 rows in set (0.16 sec)
```

## 億レベルのレコード

- 億オーダーのレコードを一発で入れるの時間がかかり過ぎる
  - メモリ調整とかなにもしていいない

ロードするファイルを分割して繰り返す

```bash
split -l 100000000 data.csv data-
```

```sh
#!/bin/sh -xv

basedir="$(cd "$(dirname "${BASH_SOURCE:-$0}")";cd ..;pwd;)"

rm data-*
split -l 1000000 data.csv data-

for var in $(ls -1 data-*); do
    time cat ${var} | mysql -h127.0.0.1 -u sample -psample sample -e "LOAD DATA LOCAL INFILE  '/dev/stdin'  INTO TABLE serial_number FIELDS TERMINATED BY ',' ENCLOSED BY '\"';"
    rm ${var}
done
```

## JSON ファイルから CSV データを作成

```bash
gzcat  HAC_SERDEV_LIST_20171130_365.json.gz | jq -r '.[] | [.SERIAL_NUMBER, .DEVICE_ID, .MAC_ADDRESS, .UPDATE_DATE] | @csv' > HAC_SERDEV_LIST_20171130_365.csv
```
