#!/bin/sh -xv

basedir="$(
    cd "$(dirname "${BASH_SOURCE:-$0}")"
    cd ..
    pwd
)"

# rm data-*
# split -l 1000000 data.csv data-

for var in $(ls -1 data-*); do
    time cat ${var} | mysql -h127.0.0.1 -u sample -psample sample -e "LOAD DATA LOCAL INFILE  '/dev/stdin'  INTO TABLE serial_number FIELDS TERMINATED BY ',' ENCLOSED BY '\"';"
    rm ${var}
done
