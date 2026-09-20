package yu.spring.gyeongsanlog.place.config;

import org.springframework.util.StringUtils;

// TourAPI 응답 텍스트의 <br> 같은 태그와 HTML 엔티티 제거

public final class TourApiTextCleaner {

    private TourApiTextCleaner() {
    }

    public static String clean(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String cleaned = value
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("<[^>]+>", "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replaceAll("[ \t]+", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();

        return StringUtils.hasText(cleaned) ? cleaned : null;
    }
}
