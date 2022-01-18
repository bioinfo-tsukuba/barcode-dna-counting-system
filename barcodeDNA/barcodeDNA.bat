@echo off
set LOCAL_JAVA=.\Tools\jre11.0.3
pushd %0\..
SET Path=%LOCAL_JAVA%\bin;%Path%
cls
java -Xmx1024m -classpath barcodeDNA.jar barcode.Main
pause
exit
