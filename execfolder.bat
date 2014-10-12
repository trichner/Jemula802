@echo off
echo starting
for %%F in (%1\*.xml) do (
java -Xmx6g -jar Jemula802-1.0.jar %%~F
)
echo done

