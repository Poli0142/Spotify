@echo off
chcp 65001 > nul
set PSQL="C:\Program Files\PostgreSQL\18\bin\psql.exe"
set PGUSER=postgres
set PGPASSWORD=1
set PGCLIENTENCODING=UTF8
cd /d %~dp0

%PSQL% -v ON_ERROR_STOP=1 -d postgres        -f 00_create_databases.sql || goto error
%PSQL% -v ON_ERROR_STOP=1 -d music_users     -f 01_users.sql            || goto error
%PSQL% -v ON_ERROR_STOP=1 -d music_catalog   -f 02_catalog.sql          || goto error
%PSQL% -v ON_ERROR_STOP=1 -d music_playlists -f 03_playlists.sql        || goto error
%PSQL% -v ON_ERROR_STOP=1 -d music_streaming -f 04_streaming.sql        || goto error

echo.
echo OK
pause
exit /b 0

:error
echo ERROR
pause
exit /b 1
