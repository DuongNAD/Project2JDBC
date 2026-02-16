package org.example.dao;

import org.example.connect.DatabaseConnect;

import org.example.model.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories";

        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Category cat = new Category(
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("description")
                );
                categories.add(cat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public boolean registerCourse(int userId, int courseId) {
        String checkSql = "SELECT COUNT(*) FROM enrollments WHERE user_id = ? AND course_id = ?";
        String insertSql = "INSERT INTO enrollments (user_id, course_id, enrolled_at, progress_percent, status) VALUES (?, ?, NOW(), 0, 'active')";

        try (java.sql.Connection conn = org.example.connect.DatabaseConnect.getConnection()) {

            java.sql.PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setInt(1, userId);
            psCheck.setInt(2, courseId);
            java.sql.ResultSet rs = psCheck.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false;
            }

            java.sql.PreparedStatement psInsert = conn.prepareStatement(insertSql);
            psInsert.setInt(1, userId);
            psInsert.setInt(2, courseId);

            int rows = psInsert.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
