package cn.edu.sdua.qingshaonian.repository;

import cn.edu.sdua.qingshaonian.entity.KBCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KBCategoryRepository extends JpaRepository<KBCategory, Long> {

    List<KBCategory> findAllByOrderBySortOrderAsc();

    Optional<KBCategory> findByName(String name);
}
