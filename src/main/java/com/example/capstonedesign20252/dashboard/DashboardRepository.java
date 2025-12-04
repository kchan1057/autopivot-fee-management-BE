package com.example.capstonedesign20252.dashboard;

import com.example.capstonedesign20252.dashboard.domain.Dashboard;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, Long> {

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("DELETE FROM Dashboard d WHERE d.group.id = :groupId")
  void deleteAllByGroupId(@Param("groupId") Long groupId);
}
