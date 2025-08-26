package com.sdmd.taskmanager;

import com.sdmd.taskmanager.model.Task;
import com.sdmd.taskmanager.model.Task.StatusEnum;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("acceptance")
@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ManagerApplicationAcceptTests {

    @Container
    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.33")
            .withDatabaseName("taskdb")
            .withUsername("test")
            .withPassword("test");

    // Dynamically override Spring datasource properties to use Testcontainer DB
    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void crudOperations() throws Exception {
        Task task = new Task()
                .title("Test Task")
                .description("This is a test task")
                .status(StatusEnum.PENDING)
                .deadline(java.time.OffsetDateTime.now().plusDays(1));

        String taskJson = objectMapper.writeValueAsString(task);

        // Create
        String response = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value(StatusEnum.PENDING.getValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Task createdTask = objectMapper.readValue(response, Task.class);

        // Read
        mockMvc.perform(get("/tasks/{id}", createdTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTask.getId()))
                .andExpect(jsonPath("$.status").value(StatusEnum.PENDING.getValue()));

        // Update
        createdTask.setStatus(StatusEnum.IN_PROGRESS);
        String updatedJson = objectMapper.writeValueAsString(createdTask);

        mockMvc.perform(put("/tasks/{id}", createdTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(StatusEnum.IN_PROGRESS.getValue()));

        // Delete
        mockMvc.perform(delete("/tasks/{id}", createdTask.getId()))
                .andExpect(status().isNoContent());
    }
}
