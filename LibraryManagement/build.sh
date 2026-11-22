#!/bin/sh
# Biên dịch toàn bộ mã nguồn và đóng gói LibraryApp.jar (cần JDK có lệnh javac, jar)
# -source 8 -target 8: tạo bytecode Java 8 để jar chạy được trên mọi máy có Java 8 trở lên.
cd "$(dirname "$0")" || exit 1
rm -rf out && mkdir out
javac -source 8 -target 8 -encoding UTF-8 -d out $(find src -name "*.java") || exit 1
printf 'Main-Class: main.Main\n' > manifest.txt
jar cfm LibraryApp.jar manifest.txt -C out . && rm -f manifest.txt
echo "Build xong: LibraryApp.jar"
