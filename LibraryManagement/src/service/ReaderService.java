package service;

import exception.InvalidDataException;
import exception.ItemInUseException;
import exception.LibraryException;
import exception.ReaderNotFoundException;
import java.util.ArrayList;
import java.util.List;
import model.LecturerReader;
import model.PriorityStudentReader;
import model.Reader;
import model.StudentReader;
import repository.ReaderRepository;
import repository.TicketRepository;
import utils.IdGenerator;
import utils.TextUtils;
import utils.Validator;

/** Nghiệp vụ quản lý bạn đọc: thêm, sửa (kể cả đổi loại), xóa, tìm kiếm, sắp xếp. */
public class ReaderService {

    public static final int TYPE_STUDENT = 1;
    public static final int TYPE_PRIORITY = 2;
    public static final int TYPE_LECTURER = 3;

    private final ReaderRepository readerRepo;
    private final TicketRepository ticketRepo;

    public ReaderService(ReaderRepository readerRepo, TicketRepository ticketRepo) {
        this.readerRepo = readerRepo;
        this.ticketRepo = ticketRepo;
    }

    /** Thêm bạn đọc mới, mã tự sinh theo loại (SV01, UT01, GV01...). */
    public Reader addReader(int type, String fullName, String phone) throws LibraryException {
        Validator.requireNotBlank(fullName, "Họ tên");
        Validator.requireValidPhone(phone);
        String id = IdGenerator.nextReaderId(readerRepo.getAll(), prefixOf(type));
        Reader reader = create(type, id, fullName.trim(), phone.trim());
        readerRepo.add(reader);
        readerRepo.save();
        return reader;
    }

    /**
     * Sửa họ tên, SĐT và loại bạn đọc. Đổi loại nghĩa là tạo đối tượng thuộc lớp con khác
     * (cùng mã) rồi thay vào danh sách; không cho đổi khi bạn đọc đang mượn sách.
     */
    public Reader updateReader(String readerId, int newType, String fullName, String phone)
            throws LibraryException {
        Reader old = getById(readerId);
        Validator.requireNotBlank(fullName, "Họ tên");
        Validator.requireValidPhone(phone);

        if (typeOf(old) != newType) {
            if (ticketRepo.hasActiveTicketOfReader(old.getId())) {
                throw new ItemInUseException("Bạn đọc " + old.getId()
                        + " đang mượn sách, không thể đổi loại bạn đọc");
            }
            Reader replacement = create(newType, old.getId(), fullName.trim(), phone.trim());
            readerRepo.replace(old, replacement);
            readerRepo.save();
            return replacement;
        }
        old.setFullName(fullName);
        old.setPhone(phone);
        readerRepo.save();
        return old;
    }

    /** Xóa bạn đọc; không cho xóa khi còn phiếu "Đang mượn". */
    public void deleteReader(String readerId) throws LibraryException {
        Reader reader = getById(readerId);
        if (ticketRepo.hasActiveTicketOfReader(reader.getId())) {
            throw new ItemInUseException("Không thể xóa bạn đọc " + reader.getId()
                    + " vì đang mượn sách chưa trả");
        }
        readerRepo.remove(reader);
        readerRepo.save();
    }

    public Reader getById(String readerId) throws ReaderNotFoundException {
        Reader reader = readerRepo.findById(readerId);
        if (reader == null) {
            throw new ReaderNotFoundException(readerId);
        }
        return reader;
    }

    public List<Reader> getAll() {
        return new ArrayList<Reader>(readerRepo.getAll());
    }

    /** Tìm theo mã hoặc họ tên, không phân biệt hoa thường và dấu. */
    public List<Reader> search(String keyword) {
        List<Reader> result = new ArrayList<Reader>();
        if (Validator.isBlank(keyword)) {
            return result;
        }
        for (Reader r : readerRepo.getAll()) {
            if (TextUtils.containsIgnoreAccent(r.getId(), keyword)
                    || TextUtils.containsIgnoreAccent(r.getFullName(), keyword)) {
                result.add(r);
            }
        }
        return result;
    }

    /** Sắp xếp theo tên gọi (từ cuối của họ tên), trùng thì theo họ tên đầy đủ: dùng Comparator. */
    public List<Reader> sortByName() {
        List<Reader> list = getAll();
        list.sort((a, b) -> {
            int c = TextUtils.removeAccents(TextUtils.lastWord(a.getFullName()))
                    .compareToIgnoreCase(TextUtils.removeAccents(TextUtils.lastWord(b.getFullName())));
            if (c != 0) {
                return c;
            }
            return TextUtils.removeAccents(a.getFullName())
                    .compareToIgnoreCase(TextUtils.removeAccents(b.getFullName()));
        });
        return list;
    }

    /** Sắp xếp theo mã bạn đọc: dùng Comparator. */
    public List<Reader> sortById() {
        List<Reader> list = getAll();
        list.sort((a, b) -> a.getId().compareToIgnoreCase(b.getId()));
        return list;
    }

    /** @return mã loại (1/2/3) của một bạn đọc, xác định bằng instanceof. */
    public int typeOf(Reader reader) {
        if (reader instanceof LecturerReader) {
            return TYPE_LECTURER;
        }
        if (reader instanceof PriorityStudentReader) {
            return TYPE_PRIORITY;
        }
        return TYPE_STUDENT;
    }

    /** Factory: tạo đúng lớp con theo loại; kiểu trả về là Reader (upcasting). */
    private Reader create(int type, String id, String fullName, String phone) throws InvalidDataException {
        switch (type) {
            case TYPE_STUDENT:
                return new StudentReader(id, fullName, phone);
            case TYPE_PRIORITY:
                return new PriorityStudentReader(id, fullName, phone);
            case TYPE_LECTURER:
                return new LecturerReader(id, fullName, phone);
            default:
                throw new InvalidDataException("Loại bạn đọc không hợp lệ: " + type);
        }
    }

    private String prefixOf(int type) throws InvalidDataException {
        switch (type) {
            case TYPE_STUDENT:
                return "SV";
            case TYPE_PRIORITY:
                return "UT";
            case TYPE_LECTURER:
                return "GV";
            default:
                throw new InvalidDataException("Loại bạn đọc không hợp lệ: " + type);
        }
    }
}
