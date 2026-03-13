package org.example.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RecommendationService {

    // Hàm lấy danh sách khóa học thịnh hành (Trending) từ file popularity_ranking
    public List<Integer> getTrendingCourses() {
        List<Integer> trendingCourses = new ArrayList<>();

        try {
            InputStream is = getClass().getResourceAsStream("/data/model_popularity_ranking.csv");
            if (is == null) {
                System.out.println("Không tìm thấy file /data/model_popularity_ranking.csv!");
                return trendingCourses;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(",");
                if (values.length > 0) {
                    try {
                        int courseId = Integer.parseInt(values[0].trim());
                        trendingCourses.add(courseId);
                    } catch (NumberFormatException e) {
                        // Bỏ qua dòng lỗi
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return trendingCourses;
    }

    // Hàm lấy đề xuất cá nhân hóa cho User
    public List<Integer> getCoursesForUser(int userId) {
        List<Integer> forYouCourses = new ArrayList<>();

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

                    int firstComma = line.indexOf(',');
                    if (firstComma > 0) {
                        try {
                            int rowUserId = Integer.parseInt(line.substring(0, firstComma).trim());
                            if (rowUserId == userId) {
                                String coursesStr = line.substring(firstComma + 1).replace("\"", "").trim();
                                String[] ids = coursesStr.split(",");
                                for (String idStr : ids) {
                                    forYouCourses.add(Integer.parseInt(idStr.trim()));
                                }
                                break; // Tìm thấy user rồi thì dừng lại cho nhanh
                            }
                        } catch (NumberFormatException e) {
                            // Ignored
                        }
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

    // Hàm lấy các khóa học liên quan dựa trên độ tương đồng (Cosine Similarity)
    public List<Integer> getRelatedCourses(int courseId, int limit) {
        List<Integer> relatedCourses = new ArrayList<>();

        try {
            InputStream is = getClass().getResourceAsStream("/data/model_course_similarity.csv");
            if (is == null) {
                return relatedCourses; // Return rỗng nếu không có file
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;

            // Đọc header (dòng 1)
            line = br.readLine();
            if (line == null) {
                br.close();
                return relatedCourses;
            }

            String[] headers = line.split(",");
            List<Integer> headerCourseIds = new ArrayList<>();
            for (int i = 1; i < headers.length; i++) {
                try {
                    headerCourseIds.add(Integer.parseInt(headers[i].trim()));
                } catch (NumberFormatException e) {
                    headerCourseIds.add(-1);
                }
            }

            // Tìm row tương ứng với courseId
            String targetRow = null;
            while ((line = br.readLine()) != null) {
                int firstComma = line.indexOf(',');
                if (firstComma > 0) {
                    try {
                        int rowSourceCourseId = Integer.parseInt(line.substring(0, firstComma).trim());
                        if (rowSourceCourseId == courseId) {
                            targetRow = line;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        // Ignored
                    }
                }
            }
            br.close();

            if (targetRow != null) {
                String[] values = targetRow.split(",");
                // Danh sách chứa (score, targetCourseId)
                List<CourseScore> scores = new ArrayList<>();

                for (int i = 1; i < values.length && i - 1 < headerCourseIds.size(); i++) {
                    try {
                        int targetCourseId = headerCourseIds.get(i - 1);
                        double score = Double.parseDouble(values[i].trim());

                        // Bỏ qua chính nó và các khóa học không hợp lệ (-1)
                        if (targetCourseId != -1 && targetCourseId != courseId && score > 0) {
                            scores.add(new CourseScore(targetCourseId, score));
                        }
                    } catch (NumberFormatException e) {
                        // Bỏ qua lỗi parse
                    }
                }

                // Sắp xếp giảm dần theo score
                scores.sort((a, b) -> Double.compare(b.score, a.score));

                // Lấy top limit
                for (int i = 0; i < Math.min(limit, scores.size()); i++) {
                    relatedCourses.add(scores.get(i).courseId);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return relatedCourses;
    }

    // Lớp phụ trợ để lưu điểm tương đồng
    private static class CourseScore {
        int courseId;
        double score;

        public CourseScore(int courseId, double score) {
            this.courseId = courseId;
            this.score = score;
        }
    }
}
