#!/bin/sh
# Chạy chương trình (phải chạy trong thư mục dự án để thư mục data/ được tìm thấy)
cd "$(dirname "$0")" || exit 1
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar LibraryApp.jar
