HỆ THỐNG QUẢN LÝ MƯỢN SÁCH THƯ VIỆN SINH VIÊN  (Đề tài nhóm 04 - Bài tập lớn OOP, Java)
=========================================================================================

1. YÊU CẦU
----------
- JDK 8 trở lên (đã biên dịch và kiểm thử với JDK 21, mức ngôn ngữ -source 8 -target 8).
- Chạy chương trình chỉ cần JRE; muốn biên dịch lại cần JDK có lệnh javac và jar.
- Console cần hiển thị UTF-8 để thấy tiếng Việt có dấu
  (Windows: dùng Windows Terminal, hoặc run.bat đã tự gõ "chcp 65001").

2. CHẠY NHANH
-------------
Linux / macOS :  ./run.sh
Windows       :  nhấp đúp run.bat (hoặc gõ run.bat trong CMD)
Hoặc thủ công, TRONG thư mục dự án:  java -jar LibraryApp.jar
(Chương trình đọc/ghi thư mục data/ ở thư mục hiện tại, nên phải chạy trong thư mục dự án.)

Tài khoản đăng nhập mẫu (mật khẩu được lưu dạng băm SHA-256):
    admin   / admin123      (Quản trị thư viện)
    thuthu  / 123456        (Thủ thư)
Đăng nhập sai quá 3 lần thì chương trình tự thoát.
Màn hình đầu tiên có 3 lựa chọn: 1. Đăng nhập, 2. Tạo tài khoản mới, 0. Thoát chương trình.
Tạo tài khoản thủ thư mới: nhập họ tên, số điện thoại (bắt đầu bằng 0, 10-11 chữ số), tên đăng nhập
(ít nhất 3 ký tự, không có khoảng trắng, không trùng tài khoản đã có - không phân biệt hoa/thường),
mật khẩu (ít nhất 6 ký tự, nhập lại để xác nhận). Tài khoản được lưu vào data/accounts.dat.

Dữ liệu mẫu có sẵn: 12 đầu sách (có 1 sách hết hàng S012), 7 bạn đọc (3 SV thường SV01-SV03,
2 SV ưu tiên UT01-UT02, 2 giảng viên GV01-GV02), 4 phiếu mượn (1 phiếu đang quá hạn PM0001,
1 phiếu còn hạn PM0002, 2 phiếu đã trả trễ có phí phạt PM0003 và PM0004).
Ngày trong dữ liệu mẫu tính theo ngày tạo (15/11/2026) nên phiếu PM0001 sẽ càng quá hạn nhiều
khi bạn chạy vào những ngày sau. Muốn tạo lại dữ liệu mẫu: xóa các file *.dat trong data/ rồi chạy lại.

3. BIÊN DỊCH LẠI VÀ CHẠY KIỂM THỬ
---------------------------------
Linux / macOS :  ./build.sh   rồi   ./run-tests.sh
Windows       :  build.bat    rồi   run-tests.bat
(Kết quả chạy thật đã lưu trong test/ket-qua-test.txt)

4. CẤU TRÚC THƯ MỤC
-------------------
LibraryManagement/
 |- src/            58 file .java (khoảng 2811 dòng)
 |   |- model/       User*, Reader*, StudentReader, PriorityStudentReader, LecturerReader, Librarian,
 |   |               Book, BorrowTicket, BorrowTicketDetail, ReturnRecord, TicketStatus
 |   |- service/     Borrowable, FinePolicy (interface) + 3 FinePolicy, BorrowService, ReturnService,
 |   |               BookService, ReaderService, StatisticService, AuthService, ReaderInfoService,
 |   |               SampleDataService
 |   |- exception/   LibraryException + 12 lớp lỗi nghiệp vụ / lưu trữ
 |   |- repository/  BaseRepository (đọc/ghi .dat), Book/Reader/Ticket/AccountRepository
 |   |- utils/       Validator, IdGenerator, InputHelper, TextUtils, DateFormatter, MoneyFormatter, PasswordUtil
 |   `- main/        Main, AppContext, BaseMenu*, LoginMenu, MainMenu, BookMenu, ReaderMenu,
 |                   BorrowMenu, StatisticMenu          (* = abstract)
 |- test/           ServiceTest.java (54 kiểm tra), ManagementTest.java (81 kiểm tra), ket-qua-test.txt
 |- data/           books.dat, readers.dat, tickets.dat, accounts.dat  (file nhị phân)
 |- docs/           class-diagram.mmd (sơ đồ lớp), kich-ban-nhap-demo.txt, ket-qua-chay-thu-console.txt
 |- LibraryApp.jar  bản chạy nhanh
 |- build.sh / build.bat, run.sh / run.bat, run-tests.sh / run-tests.bat
 `- README.txt

5. CHỨC NĂNG (menu console)
---------------------------
Màn hình đầu (Đăng nhập / Tạo tài khoản mới / Thoát) -> Menu chính:
 1. Quản lý sách     : hiển thị, thêm, sửa, xóa, tìm kiếm (mã hoặc tên, không phân biệt dấu),
                       sắp xếp (tên / số lượng giảm dần / giá tăng dần), kiểm tra còn đủ số lượng
 2. Quản lý bạn đọc  : hiển thị, thêm, sửa (kể cả đổi loại), xóa, tìm kiếm, sắp xếp (tên / mã),
                       xem quy định mượn - trả của từng loại bạn đọc
 3. Mượn sách        : một phiếu có thể gồm nhiều sách; kiểm tra đủ 5 điều kiện của đề, báo lý do lỗi cụ thể
 4. Trả sách         : kiểm tra phiếu, tăng lại kho, tính phí phạt theo loại bạn đọc, cho nhập ngày trả
                       thực tế để demo trả quá hạn
 5. Phiếu mượn       : tất cả / đang mượn / đã trả / quá hạn / tra cứu theo mã
 6. Thống kê         : tổng quan kho, top sách mượn nhiều, tổng phí phạt, phiếu quá hạn, bạn đọc theo loại

Quy định mượn sách (theo loại bạn đọc; đề không nêu số cụ thể ngoài mức phạt nên nhóm tự quy định):
                        Tối đa    Hạn mượn   Phạt / ngày trễ
    Sinh viên thường    3 cuốn    14 ngày    5.000đ
    Sinh viên ưu tiên   5 cuốn    21 ngày    3.000đ
    Giảng viên         10 cuốn    30 ngày    2.000đ
  - "Tối đa" tính theo TỔNG số cuốn đang mượn trên mọi phiếu "Đang mượn" cộng với số cuốn muốn mượn
    thêm (không phải riêng từng phiếu). Ví dụ: sinh viên thường đang giữ 2 cuốn thì chỉ mượn thêm
    được 1 cuốn; trả sách xong thì số đang giữ giảm tương ứng.
  - Ngày hẹn trả = ngày mượn + số ngày mượn của loại bạn đọc.
  - Người mượn có trách nhiệm bảo quản sách và hoàn trả đúng thời hạn.
  Muốn đổi quy định: sửa hằng số MAX_BOOKS và BORROW_DAYS trong 3 lớp StudentReader,
  PriorityStudentReader, LecturerReader (hoặc RATE_PER_DAY trong 3 lớp FinePolicy).

6. ĐỐI CHIẾU YÊU CẦU KỸ THUẬT CỦA ĐỀ
------------------------------------
- Abstract class     : User, Reader, BaseRepository, BaseMenu
- Interface          : FinePolicy, Borrowable
- Kế thừa            : Reader -> 3 loại bạn đọc; User -> Librarian; BaseRepository -> 4 repository
- Đa hình / override : getMaxBooks, getBorrowDays, getFinePolicy, getReaderType, toString, calculateFine
- Encapsulation      : mọi thuộc tính private, có getter/setter, kiểm tra dữ liệu trong constructor/setter
- ArrayList          : BaseRepository lưu ArrayList<T>
- Thêm/sửa/xóa/tìm/hiển thị : BookService, ReaderService + BookMenu, ReaderMenu
- File nhị phân      : BaseRepository (ObjectOutputStream / ObjectInputStream) -> data/*.dat
- Comparable / Comparator : Book.compareTo, BookService.sort*, ReaderService.sort*
- Upcasting / downcasting / instanceof : ReaderInfoService.describe, ReaderService.typeOf/create,
                       ReaderMenu.showPolicy
- Exception handling : LibraryException (checked) + các lớp con, menu bắt và in thông báo rõ nguyên nhân
- Collections        : ArrayList, LinkedHashMap, HashMap, List, Map.Entry
- Chia package       : model, service, repository, utils, main (+ exception)
- Giao diện          : đăng nhập + menu console nhiều cấp

7. NHỮNG GÌ ĐÃ KIỂM THỬ VÀ CHƯA KIỂM THỬ
----------------------------------------
Đã chạy thật (JDK 21): 135/135 kiểm tra tự động đạt; chạy trọn kịch bản menu từ đầu đến cuối bằng
dữ liệu nhập giả lập (docs/kich-ban-nhap-demo.txt -> docs/ket-qua-chay-thu-console.txt), kể cả
chạy từ LibraryApp.jar, không có lỗi ngoại lệ nào rò ra màn hình.
Chưa kiểm thử: chạy trên Windows thật và JDK 8 thật (script .bat chưa chạy thử), nhập tiếng Việt có
dấu trực tiếp trên console Windows cũ (nếu lỗi hãy dùng Windows Terminal hoặc nhập không dấu).

8. HẠN CHẾ ĐÃ BIẾT / HƯỚNG MỞ RỘNG
----------------------------------
- Chưa rollback nếu ghi file thất bại giữa chừng (dữ liệu trong bộ nhớ đã đổi, file chưa kịp ghi).
- SHA-256 không muối chỉ ở mức demo; hệ thống thật nên dùng bcrypt/PBKDF2.
- Chưa có giao diện Swing, chưa gia hạn sách, chưa phạt làm mất/hỏng sách (có sẵn trường giá trị sách).
- Chỉ một người dùng tại một thời điểm (không xử lý đa luồng / nhiều máy cùng ghi file).
- Báo cáo .docx và slide chưa nằm trong gói này; sơ đồ lớp ở docs/class-diagram.mmd.
