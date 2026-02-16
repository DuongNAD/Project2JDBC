package org.example.dao;

import org.example.connect.DatabaseConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatisticsDao {

    // 1. Đếm tổng số bài học đã hoàn thành (Dùng làm "Giờ học" hoặc "Bài học")
    // Dựa vào bảng `lesson_progress` anh gửi
    public int getTotalLessonsCompleted(int userId) {
        String sql = "SELECT COUNT(*) FROM lesson_progress WHERE user_id = ?";
        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 2. Đếm số khóa học ĐÃ HOÀN THÀNH
    // Dựa vào bảng `enrollments`, cột `status` = 'completed'
    public int getCompletedCoursesCount(int userId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE user_id = ? AND status = 'completed'";
        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 3. Đếm số khóa ĐANG HỌC
    // Dựa vào bảng `enrollments`, cột `status` = 'active'
    public int getInProgressCoursesCount(int userId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE user_id = ? AND status = 'active'";
        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 4. Tính điểm trung bình bài tập
    // (Giả sử bảng exercise_completions có cột score và user_id)
    public double getAverageScore(int userId) {
        String sql = "SELECT AVG(score) FROM exercise_completions WHERE user_id = ?";
        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double avg = rs.getDouble(1);
                // Làm tròn 1 chữ số thập phân (VD: 8.5)
                return Math.round(avg * 10.0) / 10.0;
            }
        } catch (Exception e) {
            // Nếu chưa có bảng này hoặc lỗi thì trả về 0.0
            System.err.println("Lỗi tính điểm trung bình (có thể chưa có dữ liệu): " + e.getMessage());
        }
        return 0.0;
    }

    // 5. BIỂU ĐỒ CỘT: Hoạt động học tập 7 ngày qua
    // Lấy từ bảng `lesson_progress`, cột `completed_at`
    public Map<String, Integer> getWeeklyActivity(int userId) {
        Map<String, Integer> data = new LinkedHashMap<>();

        // Query: Lấy ngày (định dạng dd/MM) và đếm số bài học xong trong ngày đó
        // Chỉ lấy trong 7 ngày gần nhất
        String sql = "SELECT DATE_FORMAT(completed_at, '%d/%m') as date_str, COUNT(*) as count " +
                "FROM lesson_progress " +
                "WHERE user_id = ? " +
                "AND completed_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                "GROUP BY date_str " +
                "ORDER BY completed_at ASC";

        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                data.put(rs.getString("date_str"), rs.getInt("count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    // 6. BIỂU ĐỒ TRÒN: Phân bố theo Danh mục
    // Join: enrollments -> courses -> categories
    public Map<String, Integer> getCourseCategoriesDistribution(int userId) {
        Map<String, Integer> data = new HashMap<>();

        // Lưu ý: Cần chắc chắn bảng `courses` có cột `category_id` và `course_id`
        // Bảng `enrollments` có `course_id`
        // Bảng `categories` có `category_id` và `name` (như trong ảnh anh gửi)
        String sql = "SELECT c.name, COUNT(e.course_id) as count " +
                "FROM enrollments e " +
                "JOIN courses co ON e.course_id = co.course_id " +
                "JOIN categories c ON co.category_id = c.category_id " +
                "WHERE e.user_id = ? " +
                "GROUP BY c.name";

        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                data.put(rs.getString("name"), rs.getInt("count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}