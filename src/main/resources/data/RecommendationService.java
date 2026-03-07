package com.elearning.admin.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RecommendationService {

    // Hàm lấy danh sách khóa học thịnh hành (Trending) từ file popularity_ranking
    public List<String> getTrendingCourses() {
        List<String> trendingCourses = new ArrayList<>();

        try {
            // Load file CSV từ thư mục resources
            InputStream is = getClass().getResourceAsStream("/data/model_popularity_ranking.csv");
            if (is == null) {
                System.out.println("Không tìm thấy file data!");
                return trendingCourses;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            boolean isFirstLine = true;

            // Đọc từng dòng (row) trong file
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Bỏ qua dòng tiêu đề (Header)
                }

                String[] values = line.split(","); // Tách dữ liệu bằng dấu phẩy
                if (values.length > 0) {
                    // Giả sử cột đầu tiên (index 0) là course_id
                    trendingCourses.add(values[0]);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return trendingCourses; // Trả về list các ID khóa học
    }

    // Hàm lấy đề xuất cá nhân hóa cho User
    public List<String> getCoursesForUser(String userId) {
        List<String> forYouCourses = new ArrayList<>();

        try {
            InputStream is = getClass().getResourceAsStream("/data/model_user_recommendations.csv");
            if (is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                boolean isFirstLine = true;

                while ((line = br.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    String[] values = line.split(",");
                    // Giả sử cột 0 là user_id, cột 1 là recommended_course_id
                    if (values.length > 1 && values[0].equals(userId)) {
                        forYouCourses.add(values[1]);
                    }
                }
                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Cold Start Fallback: Nếu user chưa có data, trả về danh sách trending
        if (forYouCourses.isEmpty()) {
            return getTrendingCourses();
        }

        return forYouCourses;
    }
}