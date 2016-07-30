
VERSION=4.1

all:
	mvn clean -Pwindows32
	mvn package -Pwindows32
	cp target/gnetwatch-${VERSION}-SNAPSHOT-distribution.zip distrib/gnetwatch-${VERSION}-win32-x86.zip
	mvn clean -Pwindows64
	mvn package -Pwindows64
	cp target/gnetwatch-${VERSION}-SNAPSHOT-distribution.zip distrib/gnetwatch-${VERSION}-win32-x86_64.zip
	mvn clean -Plinux32
	mvn package -Plinux32
	cp target/gnetwatch-${VERSION}-SNAPSHOT-distribution.tar.bz2 distrib/gnetwatch-${VERSION}-linux-x86.tar.bz2
	mvn clean -Plinux64
	mvn package -Plinux64
	cp target/gnetwatch-${VERSION}-SNAPSHOT-distribution.tar.bz2 distrib/gnetwatch-${VERSION}-linux-x86_64.tar.bz2

