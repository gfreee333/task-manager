package com.taskmanager.repository.projects;

import com.taskmanager.model.Projects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectsRepository extends JpaRepository<Projects, Long> {
    List<Projects> findByStatus(String status);
    List<Projects> findByOwnerId(Long id);
    @Query("SELECT p FROM Projects p JOIN p.users u WHERE u.id = :userId")
    List<Projects> findProjectsByParticipantWithRole(@Param("userId") Long userId);
}
