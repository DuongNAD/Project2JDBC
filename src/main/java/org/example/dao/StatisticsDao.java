package org.example.dao;

import org.example.connect.DatabaseConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatisticsDao {

    
    
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

    
    
    public double getAverageScore(int userId) {
        String sql = "SELECT AVG(score) FROM exercise_completions WHERE user_id = ?";
        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double avg = rs.getDouble(1);
                
                return Math.round(avg * 10.0) / 10.0;
            }
        } catch (Exception e) {
            
            System.err.println("Lỗi tính điểm trung bình (có thể chưa có dữ liệu): " + e.getMessage());
        }
        return 0.0;
    }

    
    
    public Map<String, Integer> getWeeklyActivity(int userId) {
        Map<String, Integer> data = new LinkedHashMap<>();

        
        
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

    
    
    public Map<String, Integer> getCourseCategoriesDistribution(int userId) {
        Map<String, Integer> data = new HashMap<>();

        
        
        
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