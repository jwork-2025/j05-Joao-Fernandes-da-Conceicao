#!/bin/bash

# 清理构建产物 (Linux/macOS)
echo "开始清理构建产物..."

# 检查build目录是否存在
if [ -d "build" ]; then
    # 删除build目录及其所有内容
    rm -rf build
    
    # 再次检查是否删除成功
    if [ -d "build" ]; then
        echo "清理失败：build 目录仍然存在"
        exit 1
    else
        echo "清理完成"
    fi
else
    echo "build 目录不存在，无需清理"
fi

exit 0
