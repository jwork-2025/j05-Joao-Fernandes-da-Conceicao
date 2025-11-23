@echo off
setlocal

rem 运行游戏脚本 (Windows)
pushd "%~dp0" >nul

for /f %%A in ('powershell -NoProfile -Command "$PSStyle.OutputRendering='PlainText'; Write-Host '启动游戏引擎...'" ^| find /c /v ""') do rem noop

call compile.bat
if %ERRORLEVEL% NEQ 0 (
  for /f %%A in ('powershell -NoProfile -Command "$PSStyle.OutputRendering='PlainText'; Write-Host '编译失败，无法运行游戏'" ^| find /c /v ""') do rem noop
  popd >nul
  endlocal & exit /b 1
)

for /f %%A in ('powershell -NoProfile -Command "$PSStyle.OutputRendering='PlainText'; Write-Host '运行游戏...'" ^| find /c /v ""') do rem noop
java -cp build\classes com.gameengine.example.GameExample
set ERR=%ERRORLEVEL%

popd >nul
endlocal & exit /b %ERR%

