@Echo Off
title PAXLE

If %1.==GENARGS. GoTo :GENARGS
If %1.==-rdebug. GoTo :STARTDEBUG

set JAVA_ARGS=
for /f "tokens=1*" %%q in (start.ini) do (
	Call %0 GENARGS %%q
) 

:STARTJAVA
java%JAVA_ARGS% -jar ${equinox.runtime.jar} -console 
goto END

:STARTDEBUG
java%JAVA_ARGS% -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -jar ${equinox.runtime.jar} -console 
goto END

:GENARGS
if %3!==! Set JAVA_ARGS=%JAVA_ARGS% %2
if not %3!==! Set JAVA_ARGS=%JAVA_ARGS% %2=%3

:END



