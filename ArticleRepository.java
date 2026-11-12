package cn.edu.sdua.qingshaonian.repository;

import cn.edu.sdua.qingshaonian.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findByStatus(String status, Pageable pageable);

    Page<Article> findByCategoryAndStatus(String category, String status, Pageable pageable);
}
