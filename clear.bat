@echo off
setlocal

rem 清理构建产物 (Windows)
pushd "%~dp0" >nul

for /f %%A in ('powershell -NoProfile -Command "$PSStyle.OutputRendering='PlainText'; Write-Host '开始清理构建产物...'" ^| find /c /v ""') do rem noop

if exist build (
  rmdir /s /q build
)

if exist build (
  for /f %%A in ('powershell -NoProfile -Command "$PSStyle.OutputRendering='PlainText'; Write-Host '清理失败：build 目录仍然存在'" ^| find /c /v ""') do rem noop
  popd >nul
  endlocal & exit /b 1
) else (
  for /f %%A in ('powershell -NoProfile -Command "$PSStyle.OutputRendering='PlainText'; Write-Host '清理完成'" ^| find /c /v ""') do rem noop
)

popd >nul
endlocal & exit /b 0

