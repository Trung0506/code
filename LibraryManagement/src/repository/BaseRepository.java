package repository;

import exception.DataStorageException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Repository dùng chung: giữ ArrayList trong bộ nhớ, đọc/ghi file nhị phân (.dat)
 * bằng ObjectOutputStream / ObjectInputStream.
 * Lớp cha dùng chung cho các repository cụ thể (sách, bạn đọc, phiếu mượn, tài khoản).
 */
public abstract class BaseRepository<T extends Serializable> {

    private final String filePath;
    private ArrayList<T> items = new ArrayList<>();

    protected BaseRepository(String filePath) {
        this.filePath = filePath;
    }

    /** Đọc dữ liệu từ file; nếu file chưa tồn tại thì danh sách rỗng. */
    @SuppressWarnings("unchecked")
    public void load() throws DataStorageException {
        File file = new File(filePath);
        if (!file.exists()) {
            items = new ArrayList<>();
            return;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            items = (ArrayList<T>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new DataStorageException("Không đọc được file dữ liệu: " + filePath, e);
        }
    }

    /** Ghi toàn bộ danh sách xuống file. */
    public void save() throws DataStorageException {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(items);
        } catch (IOException e) {
            throw new DataStorageException("Không ghi được file dữ liệu: " + filePath, e);
        }
    }

    public void add(T item) {
        items.add(item);
    }

    /** Xóa một phần tử khỏi danh sách. @return true nếu xóa được. */
    public boolean remove(T item) {
        return items.remove(item);
    }

    public ArrayList<T> getAll() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
