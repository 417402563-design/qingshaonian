package cn.edu.sdua.qingshaonian.repository;

import cn.edu.sdua.qingshaonian.entity.KnowledgeItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface KnowledgeItemRepository extends JpaRepository<KnowledgeItem, Long> {

    Page<KnowledgeItem> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    Page<KnowledgeItem> findByCategoryCodeAndStatusOrderByCreatedAtDesc(String categoryCode, String status, Pageable pageable);

    List<KnowledgeItem> findByStatus(String status);

    List<KnowledgeItem> findByCategoryCodeInAndStatus(Collection<String> categoryCodes, String status);
}
