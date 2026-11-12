package cn.edu.sdua.qingshaonian.repository;

import cn.edu.sdua.qingshaonian.entity.RoleModelStory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleModelStoryRepository extends JpaRepository<RoleModelStory, Long> {

    Page<RoleModelStory> findByStatus(String status, Pageable pageable);
}
