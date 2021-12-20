@ECHO OFF
rmdir /s /q .gradle
rmdir /s /q build
IF EXIST build\libs\grabber-1.0.0.jar del /F /Q build\libs\grabber-1.0.0.jar
call gradlew build
IF NOT EXIST build\libs\grabber-1.0.0.jar exit 1
IF EXIST loader\grabber-1.0.0.jar del /F /Q loader\grabber-1.0.0.jar
copy build\libs\grabber-1.0.0.jar loader\grabber-1.0.0.jar

cd loader

IF EXIST classes.h del /F /Q classes.h

java -jar packer.jar grabber-1.0.0.jar
IF EXIST eloader\eloader\classes.h del /F /Q eloader\eloader\classes.h
copy classes.h eloader\eloader\classes.h
cd eloader

msbuild eloader.sln /p:Configuration=Release /p:Platform=x64
cd ..\..
IF EXIST output-total rmdir /S /Q output-total
mkdir output-total
copy loader\eloader\output\Release\grabber.x64.dll output-total\grabber.x64.dll
PAUSE