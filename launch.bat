@echo off
@title Kaotic
set PATH=C:\Program Files\Java\jdk-21\bin;%PATH%
set CLASSPATH=.;dist\*;dist\lib\*
java ^
    -Xms4G ^
    -Xmx8G ^
    -XX:+UnlockExperimentalVMOptions ^
    -XX:+AlwaysPreTouch ^
    -XX:-UseG1GC ^
    -XX:-ZUncommit ^
    -XX:+UseZGC ^
    -XX:MaxMetaspaceSize=2G ^
    -XX:MetaspaceSize=1G ^
    server.Start
pause