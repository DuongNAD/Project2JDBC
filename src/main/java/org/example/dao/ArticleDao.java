package org.example.dao;

import org.example.connect.DatabaseConnect;
import org.example.model.Article;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ArticleDao {

    public List<Article> getAllArticles() {
        List<Article> list = new ArrayList<>();
        String sql = "SELECT * FROM articles ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Article a = new Article();
                // Các hàm này giờ đã có trong Article.java nên sẽ không báo đỏ nữa
                a.setId(rs.getInt("article_id"));
                a.setTitle(rs.getString("title"));
                a.setDescription(rs.getString("description"));
                a.setContent(rs.getString("content"));
                a.setImageUrl(rs.getString("image_url"));
                a.setTags(rs.getString("tags"));
                a.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}