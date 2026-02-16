package org.example.dao;

import org.example.connect.DatabaseConnect;
import org.example.model.Article;
import org.example.model.ChatMessage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HomeDao {

    public List<Article> getTopArticles() {
        List<Article> list = new ArrayList<>();
        String sql = "SELECT * FROM articles ORDER BY created_at DESC LIMIT 4";

        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Article a = new Article();
                a.setId(rs.getInt("article_id"));
                a.setTitle(rs.getString("title"));
                a.setDescription(rs.getString("description"));
                a.setImageUrl(rs.getString("image_url"));
                a.setTags(rs.getString("tags"));
                a.setCreatedAt(rs.getTimestamp("created_at"));

                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Article> getAllArticles() {
        List<Article> list = new ArrayList<>();
        String sql = "SELECT * FROM articles ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Article a = new Article();
                a.setId(rs.getInt("article_id"));
                a.setTitle(rs.getString("title"));
                a.setDescription(rs.getString("description"));
                a.setImageUrl(rs.getString("image_url"));
                a.setTags(rs.getString("tags"));
                a.setCreatedAt(rs.getTimestamp("created_at"));

                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ChatMessage> getRecentMessages(int currentUserId) {
        List<ChatMessage> list = new ArrayList<>();
        String sql = "SELECT m.msg_id, m.message_text, u.full_name, u.avatar_url, u.user_id " +
                "FROM chat_messages m " +
                "JOIN users u ON m.user_id = u.user_id " +
                "ORDER BY m.created_at ASC";

        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int senderId = rs.getInt("user_id");
                list.add(new ChatMessage(
                        rs.getInt("msg_id"),
                        rs.getString("full_name"),
                        rs.getString("avatar_url"),
                        rs.getString("message_text"),
                        senderId == currentUserId
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void sendMessage(int userId, String message) {
        String sql = "INSERT INTO chat_messages (user_id, message_text) VALUES (?, ?)";
        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, message);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int countOnlineUsers() {
        String sql = "SELECT COUNT(*) FROM users WHERE is_active = 1";
        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}