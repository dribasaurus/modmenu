@echo off
setlocal
java -version 2>&1 | findstr /C:"25." >nul
if errorlevel 1 (
  echo ERROR: Minecraft 26.1.2 requires Java 25. Install a JDK 25 and set JAVA_HOME/PATH.
  exit /b 1
)
where gradle >nul 2>nul
if errorlevel 1 (
  echo ERROR: Gradle 9.4 or newer was not found on PATH.
  echo Install Gradle 9.4, then run this file again.
  exit /b 1
)
gradle --version | findstr /C:"Gradle 9.4" >nul
if errorlevel 1 echo WARNING: This project is intended for Gradle 9.4. Continuing anyway.
gradle build
if errorlevel 1 exit /b 1
echo.
echo Build complete. Your mod JAR is in build\libs\
endlocal
