# MySQL で大量データを扱うフィージビリティスタディ

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

## JSON ファイルから CSV データを作成

```bash
gzcat  HAC_SERDEV_LIST_20171130_365.json.gz | jq -r '.[] | [.SERIAL_NUMBER, .DEVICE_ID, .MAC_ADDRESS, .UPDATE_DATE] | @csv' > HAC_SERDEV_LIST_20171130_365.csv
```
