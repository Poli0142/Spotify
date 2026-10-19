@echo off
cd /d %~dp0

call .\mvnw.cmd -q -DskipTests package || goto error

call :run config-server 8888
call :run eureka-server 8761
call :run user-service 8091 nowait
call :run catalog-service 8092 nowait
call :run playlist-service 8093 nowait
call :run streaming-service 8094 nowait
call :wait 8091
call :wait 8092
call :wait 8093
call :wait 8094
call :run api-gateway 8090

echo waiting for gateway...
call :ready users
call :ready tracks
call :ready playlists
call :ready stream/top

echo.
echo Site:    http://localhost:8090
echo Eureka:  http://localhost:8761
echo Swagger: http://localhost:8090/swagger-ui.html
start "" http://localhost:8090
pause
exit /b 0

:run
echo %1
start "%1" java -jar %1\target\%1-1.0.0.jar
if not "%3"=="nowait" call :wait %2
exit /b 0

:wait
curl -s -o nul http://localhost:%1/actuator/health
if errorlevel 1 (
    timeout /t 2 /nobreak > nul
    goto wait
)
exit /b 0

:ready
timeout /t 3 /nobreak > nul
curl -sf -o nul http://localhost:8090/api/%1
if errorlevel 1 goto ready
exit /b 0

:error
pause
exit /b 1
