package repository;

import model.Reader;

/** Lưu danh sách bạn đọc (readers.dat). */
public class ReaderRepository extends BaseRepository<Reader> {

    public ReaderRepository(String filePath) {
        super(filePath);
    }

    /** @return bạn đọc theo mã, hoặc null nếu không có. */
    public Reader findById(String readerId) {
        for (Reader r : getAll()) {
            if (r.getId().equalsIgnoreCase(readerId)) {
                return r;
            }
        }
        return null;
    }

    /** Thay một bạn đọc bằng đối tượng mới ở cùng vị trí (dùng khi đổi loại bạn đọc). */
    public void replace(Reader oldReader, Reader newReader) {
        int index = getAll().indexOf(oldReader);
        if (index >= 0) {
            getAll().set(index, newReader);
        }
    }
}
