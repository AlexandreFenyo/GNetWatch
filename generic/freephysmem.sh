#!/bin/sh
# freephysmem.sh user@HOST
ssh $1 "grep MemFree < /proc/meminfo | awk '{ print \$2; }'"
