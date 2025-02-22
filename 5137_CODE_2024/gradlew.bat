
:: This file, gradlew.bat, is a Windows batch script used to run Gradle, 
:: a build automation tool commonly used in Java projects. The script is part of the Gradle Wrapper, which allows 
:: you to run a specific version of Gradle without requiring the user to install Gradle manually. 
:: Let's break down the script step by step:
:: This script ensures that Gradle can be run on any Windows machine without requiring a pre-installed version of Gradle,
:: making it easier to manage project dependencies and build configurations.

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
@rem ##########################################################################  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.m
@rem Find java.exe
@rem Setup the command line@rem Execute Gradle
@rem End local scope for the variables with windows NT shell
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead ofif %ERRORLEVEL% equ 0 goto mainEnd

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
