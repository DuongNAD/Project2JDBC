package org.example.dao;

import org.example.connect.DatabaseConnect;
import org.example.model.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao {

    /**
     * Get all reviews for a specific course, including user details
     */
    public List<Review> getReviewsByCourseId(int courseId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name, u.avatar_url " +
                "FROM reviews r " +
                "JOIN users u ON r.user_id = u.user_id " +
                "WHERE r.course_id = ? " +
                "ORDER BY r.created_at DESC";

        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Review r = new Review();
                r.setReviewId(rs.getInt("review_id"));
                r.setUserId(rs.getInt("user_id"));
                r.setCourseId(rs.getInt("course_id"));
                r.setRating(rs.getInt("rating"));
                r.setComment(rs.getString("comment"));
                r.setCreatedAt(rs.getTimestamp("created_at"));

                // Set joined user info
                r.setUserName(rs.getString("full_name"));
                String avatarUrl = rs.getString("avatar_url");
                r.setUserAvatar(avatarUrl != null && !avatarUrl.isEmpty() ? avatarUrl : "https://i.pravatar.cc/150");

                reviews.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    /**
     * Check if a user has enrolled in a course to allow writing a review
     */
    public boolean hasUserEnrolled(int userId, int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE user_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get a specific user's review for a course
     */
    public Review getReviewByUser(int userId, int courseId) {
        String sql = "SELECT * FROM reviews WHERE user_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Review r = new Review();
                r.setReviewId(rs.getInt("review_id"));
                r.setUserId(rs.getInt("user_id"));
                r.setCourseId(rs.getInt("course_id"));
                r.setRating(rs.getInt("rating"));
                r.setComment(rs.getString("comment"));
                r.setCreatedAt(rs.getTimestamp("created_at"));
                return r;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Submits a review. If exist, it updates; otherwise, it inserts a new review.
     * In MySQL, we can simulate an UPSERT with an initial SELECT + INSERT/UPDATE
     * or using DUPLICATE KEY UPDATE if constraints allow.
     * Here we just do a manual check.
     */
    public boolean submitReview(int userId, int courseId, int rating, String comment) {
        String checkSql = "SELECT review_id FROM reviews WHERE user_id = ? AND course_id = ?";
        String updateSql = "UPDATE reviews SET rating = ?, comment = ? WHERE user_id = ? AND course_id = ?";
        String insertSql = "INSERT INTO reviews (user_id, course_id, rating, comment) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnect.getConnection()) {
            // First check if a review exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                checkStmt.setInt(2, courseId);
                ResultSet rs = checkStmt.executeQuery();

                boolean exists = rs.next();

                if (exists) {
                    // Update
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, rating);
                        updateStmt.setString(2, comment);
                        updateStmt.setInt(3, userId);
                        updateStmt.setInt(4, courseId);
                        int rowsAffected = updateStmt.executeUpdate();
                        if (rowsAffected > 0) {
                            // Also try to sync the enrollments table for legacy support
                            syncEnrollmentRating(conn, userId, courseId, rating);
                            return true;
                        }
                    }
                } else {
                    // Insert
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setInt(1, userId);
                        insertStmt.setInt(2, courseId);
                        insertStmt.setInt(3, rating);
                        insertStmt.setString(4, comment);
                        int rowsAffected = insertStmt.executeUpdate();
                        if (rowsAffected > 0) {
                            syncEnrollmentRating(conn, userId, courseId, rating);
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Calculates the average rating for a course based on its reviews
     */
    public double getAverageRating(int courseId) {
        String sql = "SELECT AVG(rating) FROM reviews WHERE course_id = ?";
        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Gets the total number of reviews for a course
     */
    public int getTotalReviews(int courseId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE course_id = ?";
        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Helper to keep rating in sync with enrollments table
     * as LearningController uses enrollments.rating
     */
    private void syncEnrollmentRating(Connection conn, int userId, int courseId, int rating) {
        String syncSql = "UPDATE enrollments SET rating = ? WHERE user_id = ? AND course_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(syncSql)) {
            stmt.setInt(1, rating);
            stmt.setInt(2, userId);
            stmt.setInt(3, courseId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
