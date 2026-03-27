package com.taskmanager.repository.tasks;

import com.taskmanager.model.Tasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TasksRepository extends JpaRepository<Tasks, Long> {
    List<Tasks> findAllByProjectId(Long projectId);
    List<Tasks> findAllByAssigneeId(Long assigneeId);
    List<Tasks> findAllByCreatedById(Long createdById);
    List<Tasks> findAllByStatus(String status);
    List<Tasks> findAllByPriority(String priority);
}
