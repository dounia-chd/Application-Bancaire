@echo off
set MAVEN_OPTS=--enable-preview
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 --enable-preview" -Dspring-boot.run.profiles=dev
pause
