package ru.gb.timesheet.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import ru.gb.timesheet.model.Project;
import ru.gb.timesheet.model.Timesheet;
import ru.gb.timesheet.repository.ProjectRepository;
import ru.gb.timesheet.repository.TimesheetRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TimesheetControllerTest {

    @Autowired
    TimesheetRepository timesheetRepository;
    @Autowired
    ProjectRepository projectRepository;

    @LocalServerPort
    private int port;
    private RestClient restClient;

    private RestTemplate restTemplate;

    @BeforeEach
    void beforeEach() {
        restClient = RestClient.create("http://localhost:" + port);
        restTemplate = new RestTemplate();
        timesheetRepository.deleteAll();
    }

    @Test
    void getTest() {
        Timesheet timesheet = new Timesheet();
        timesheet.setMinutes(120);
        Timesheet expected = timesheetRepository.save(timesheet);

        ResponseEntity<Timesheet> actual = restClient.get()
                .uri("/timesheets/" + expected.getId())
                .retrieve()
                .toEntity(Timesheet.class);
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        Timesheet responseBody = actual.getBody();
        assertNotNull(responseBody);
        assertEquals(timesheet.getId(), responseBody.getId());
        assertEquals(timesheet.getMinutes(), responseBody.getMinutes());
    }

    @Test
    void getAllTest() {

        Timesheet timesheet1 = new Timesheet();
        timesheet1.setMinutes(120);
        timesheetRepository.save(timesheet1);

        Timesheet timesheet2 = new Timesheet();
        timesheet2.setMinutes(130);
        timesheetRepository.save(timesheet2);

        Timesheet timesheet3 = new Timesheet();
        timesheet3.setMinutes(140);
        timesheetRepository.save(timesheet3);

        List<Timesheet> expectedList = new ArrayList<>();
        expectedList.add(timesheet1);
        expectedList.add(timesheet2);
        expectedList.add(timesheet3);

        ResponseEntity<List<Timesheet>> actual = restTemplate
                .exchange("http://localhost:" + port + "/timesheets",
                        HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                        });
        List<Timesheet> timesheets = actual.getBody();

        assertEquals(timesheets, expectedList);
    }

    @Test
    void createTest() {
        Project project = new Project();
        project.setId(1L);
        project.setName("NewName");
        projectRepository.save(project);

        Timesheet timesheet = new Timesheet();
        timesheet.setMinutes(130);
        timesheet.setProjectId(1L);
        timesheetRepository.save(timesheet);

        ResponseEntity<Timesheet> response = restClient.post()
                .uri("/timesheets")
                .body(timesheet)
                .retrieve()
                .toEntity(Timesheet.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Timesheet responseBody = response.getBody();
        assertNotNull(responseBody);
        assertNotNull(responseBody.getId());
        assertEquals(responseBody.getMinutes(), timesheet.getMinutes());

        assertTrue(timesheetRepository.existsById(responseBody.getId()));
    }

    @Test
    void deleteTest() {
        Project project = new Project();
        project.setId(1L);
        project.setName("NewName");
        projectRepository.save(project);

        Timesheet timesheet = new Timesheet();
        timesheet.setMinutes(130);
        timesheet.setProjectId(1L);
        timesheet = timesheetRepository.save(timesheet);

        ResponseEntity<Void> response = restClient.delete()
                .uri("/timesheets/" + timesheet.getId())
                .retrieve()
                .toBodilessEntity();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        assertFalse(timesheetRepository.existsById(timesheet.getId()));
    }

    @Test
    void handleNoSuchElementExceptionTest() {
    }
}