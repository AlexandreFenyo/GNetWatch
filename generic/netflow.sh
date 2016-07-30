#!/bin/zsh

# log file example:
# <1280000;6000000;8413;720000;111565;14811;146666;146666;0;3093333;http;windows (445);oracle;proxy;lotus;windows (139);printer (9100);windows (138);telnet;default>

export PATH="/bin:/usr/local/bin:$PATH"

LOGDIR='/var/tmp'
INSTALLDIR='/home/fenyo/devel/netflow'
NSEC=60
# /usr/local/bin/nfcapd -D -l "$LOGDIR" -t "$NSEC" -x "$INSTALLDIR/netflow.sh %f" > /dev/null 2>&1 &

cp "$1" "$LOGDIR/aggreg.log"
echo -n "<" > "$INSTALLDIR/gnetwatch.log.tmp"

FILTEROTHERS=""
for port in 80 445 1521 8080 1352 139 9100 138 23 others
do
  if [ $port = others ]
  then
    echo "not ($FILTEROTHERS)" | sed 's/( or /(/' | read FILTER
  else
    FILTER="port $port"
    FILTEROTHERS="$FILTEROTHERS or port $port"
  fi

  echo filter: "$FILTER"

  nfdump -r "$LOGDIR/aggreg.log" -n 1 -s port/bytes "$FILTER" | grep Summary | sed 's/.*total bytes: //' | sed 's/,.*//' | read BYTES

  if echo "$BYTES" | fgrep ' M' > /dev/null
  then
    echo "$BYTES" | sed 's/ M//' | read BYTES
    echo $(( BYTES * 1000000 )) | sed 's/\..*//' | read BYTES
  fi

  if echo "$BYTES" | fgrep ' G' > /dev/null
  then
    echo "$BYTES" | sed 's/ G//' | read BYTES
    echo $(( BYTES * 1000000000 )) | sed 's/\..*//' | read BYTES
  fi

  echo $(( 8 * BYTES / NSEC )) | read BPS
  echo -n "$BPS;" >> "$INSTALLDIR/gnetwatch.log.tmp"
done
echo "http;windows (445);oracle;proxy;lotus;windows (139);printer (9100);windows (138);telnet;default>" >> "$INSTALLDIR/gnetwatch.log.tmp"

cat "$INSTALLDIR/gnetwatch.log.tmp" >> "$INSTALLDIR/gnetwatch.log"
