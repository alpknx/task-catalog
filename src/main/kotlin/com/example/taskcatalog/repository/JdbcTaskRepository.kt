package com.example.taskcatalog.repository

import com.example.taskcatalog.model.Task
import com.example.taskcatalog.model.TaskStatus
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDateTime

@Repository
class JdbcTaskRepository(private val jdbcClient: JdbcClient) : TaskRepository {

    override fun save(task: Task): Task {
        val keyHolder = GeneratedKeyHolder()
        jdbcClient.sql(
            """
            INSERT INTO tasks (title, description, status, created_at, updated_at)
            VALUES (:title, :description, :status, :createdAt, :updatedAt)
            """
        )
            .param("title", task.title)
            .param("description", task.description)
            .param("status", task.status.name)
            .param("createdAt", task.createdAt)
            .param("updatedAt", task.updatedAt)
            .update(keyHolder, "id")

        return task.copy(id = keyHolder.key!!.toLong())
    }

    override fun findById(id: Long): Task? {
        return jdbcClient.sql("SELECT * FROM tasks WHERE id = :id")
            .param("id", id)
            .query { rs, _ -> mapRow(rs) }
            .optional()
            .orElse(null)
    }

    override fun findAll(page: Int, size: Int, status: TaskStatus?): List<Task> {
        val offset = page * size
        return if (status != null) {
            jdbcClient.sql(
                "SELECT * FROM tasks WHERE status = :status ORDER BY created_at DESC LIMIT :size OFFSET :offset"
            )
                .param("status", status.name)
                .param("size", size)
                .param("offset", offset)
                .query { rs, _ -> mapRow(rs) }
                .list()
        } else {
            jdbcClient.sql(
                "SELECT * FROM tasks ORDER BY created_at DESC LIMIT :size OFFSET :offset"
            )
                .param("size", size)
                .param("offset", offset)
                .query { rs, _ -> mapRow(rs) }
                .list()
        }
    }

    override fun countAll(status: TaskStatus?): Long {
        return if (status != null) {
            jdbcClient.sql("SELECT COUNT(*) FROM tasks WHERE status = :status")
                .param("status", status.name)
                .query(Long::class.java)
                .single()
        } else {
            jdbcClient.sql("SELECT COUNT(*) FROM tasks")
                .query(Long::class.java)
                .single()
        }
    }

    override fun updateStatus(id: Long, status: TaskStatus): Boolean {
        val updated = jdbcClient.sql(
            "UPDATE tasks SET status = :status, updated_at = :updatedAt WHERE id = :id"
        )
            .param("status", status.name)
            .param("updatedAt", LocalDateTime.now())
            .param("id", id)
            .update()
        return updated > 0
    }

    override fun deleteById(id: Long): Boolean {
        val deleted = jdbcClient.sql("DELETE FROM tasks WHERE id = :id")
            .param("id", id)
            .update()
        return deleted > 0
    }

    private fun mapRow(rs: ResultSet): Task = Task(
        id = rs.getLong("id"),
        title = rs.getString("title"),
        description = rs.getString("description"),
        status = TaskStatus.valueOf(rs.getString("status")),
        createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
        updatedAt = rs.getTimestamp("updated_at").toLocalDateTime()
    )
}
