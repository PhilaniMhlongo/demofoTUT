package weshare.api;

import kong.unirest.HttpResponse;
import kong.unirest.HttpStatus;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import weshare.api.dto.LoginDTO;
import weshare.model.Person;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonApiTests extends ApiTestRunner {

    @Test
    @DisplayName("GET /api/people")
    public void getAll() {
        Collection<Person> expected = scenario.somePeople();
        Collection<Person> actual = List.of(Unirest.get("/api/people").asObject(Person[].class).getBody());
        assertThat(actual).containsAll(expected);
    }

    @Test
    @DisplayName("GET /api/people/{personId}")
    public void getPerson() {
        Person expected = scenario.somePerson();
        Person actual = Unirest.get("/api/people/{personId}")
                .routeParam("personId", expected.getId().toString())
                .asObject(Person.class)
                .getBody();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("404 GET /api/people/{personId}")
    public void getPerson404() {
        int id = scenario.getUnusedPersonId();
        HttpResponse<JsonNode> response = Unirest.get("/api/people/{personId}")
                .routeParam("personId", Integer.toString(id))
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("POST /api/people")
    public void post() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("student" + scenario.getUnusedPersonId() + "@wethinkcode.co.za");
        HttpResponse<JsonNode> response = Unirest.post("/api/people")
                .body(dto)
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("400 POST /api/people")
    public void post400() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("not an email");
        HttpResponse<JsonNode> response = Unirest.post("/api/people")
                .body(dto)
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }}
