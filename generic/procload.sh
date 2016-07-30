#!/bin/sh
# procload.sh user@HOST
ssh $1 "uptime | sed 's/.*: \([0-9.]*\).*/\1/' | sed 's/\.//' | sed 's/^0*//'"
