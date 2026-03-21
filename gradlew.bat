@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

set WRAPPER_JAR=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
set WRAPPER_PROPERTIES=%APP_HOME%\gradle\wrapper\gradle-wrapper.properties

if exist "%WRAPPER_JAR%" goto wrapperJarReady
if not exist "%WRAPPER_PROPERTIES%" goto missingWrapperProperties

for /f "tokens=1,* delims==" %%A in ('findstr /B /C:"distributionUrl=" "%WRAPPER_PROPERTIES%"') do set DIST_URL=%%B
set DIST_URL=%DIST_URL:\:=%
for %%I in ("%DIST_URL%") do set DIST_ZIP=%%~nxI
set DIST_DIR=%DIST_ZIP:.zip=%
set DIST_ROOT=%DIST_DIR:-bin=%
set DIST_ROOT=%DIST_ROOT:-all=%
set BOOTSTRAP_HOME=%APP_HOME%\.gradle-bootstrap
set GRADLE_BIN=%BOOTSTRAP_HOME%\%DIST_ROOT%\bin\gradle.bat

if exist "%GRADLE_BIN%" goto bootstrapExecute
if not exist "%BOOTSTRAP_HOME%" mkdir "%BOOTSTRAP_HOME%"

powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%BOOTSTRAP_HOME%\%DIST_ZIP%'; Expand-Archive -Path '%BOOTSTRAP_HOME%\%DIST_ZIP%' -DestinationPath '%BOOTSTRAP_HOME%' -Force"
if errorlevel 1 goto missingBootstrapTools

:bootstrapExecute
call "%GRADLE_BIN%" %*
goto end

:missingWrapperProperties
echo. 1>&2
echo ERROR: Missing Gradle wrapper properties at %WRAPPER_PROPERTIES% 1>&2
goto fail

:missingBootstrapTools
echo. 1>&2
echo ERROR: Could not bootstrap Gradle because %WRAPPER_JAR% is not committed. 1>&2
echo Install Gradle 8.14.3 or regenerate the wrapper locally. 1>&2
goto fail

:wrapperJarReady

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%WRAPPER_JAR%


@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" -jar "%WRAPPER_JAR%" %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
