#!/bin/sh
# Biên dịch và chạy 2 bộ kiểm thử (135 kiểm tra). Cần đã chạy build.sh trước.
cd "$(dirname "$0")" || exit 1
rm -rf out-test && mkdir out-test
javac -encoding UTF-8 -cp out -d out-test test/ServiceTest.java test/ManagementTest.java || exit 1
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out:out-test ServiceTest || exit 1
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out:out-test ManagementTest || exit 1
