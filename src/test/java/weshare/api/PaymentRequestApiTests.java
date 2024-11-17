package weshare.api;

import kong.unirest.HttpResponse;
import kong.unirest.HttpStatus;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import weshare.api.dto.NewPaymentRequestDTO;
import weshare.api.dto.PaymentRequestDTO;
import weshare.model.Expense;
import weshare.model.PaymentRequest;
import weshare.model.Person;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static weshare.model.DateHelper.DD_MM_YYYY;
import static weshare.model.DateHelper.TOMORROW;

public class PaymentRequestApiTests extends ApiTestRunner {

    @Test
    @DisplayName("GET /api/paymentrequests")
    public void getAll() {
        List<PaymentRequestDTO> actual = List.of(Unirest.get("/api/paymentrequests").asObject(PaymentRequestDTO[].class).getBody());
        assertThat(actual).isNotEmpty();
    }

    @Test
    @DisplayName("GET /api/paymentrequests/{paymentRequestId}")
    public void getPaymentRequest() {
        PaymentRequest expected = scenario.somePaymentRequest();
        PaymentRequestDTO actual = Unirest.get("/api/paymentrequests/{paymentRequestId}")
                .routeParam("paymentRequestId", expected.getId().toString())
                .asObject(PaymentRequestDTO.class)
                .getBody();
        assertThat(actual).isEqualTo(PaymentRequestDTO.fromPaymentRequest(expected));
    }

    @Test
    @DisplayName("404 GET /api/paymentrequests/{paymentRequestId}")
    public void getPaymentRequest404() {
        int id = scenario.getUnusedPaymentRequestId();
        HttpResponse<JsonNode> response = Unirest.get("/api/paymentrequests/{paymentRequestId}")
                .routeParam("paymentRequestId", Integer.toString(id))
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("POST /api/paymentrequests")
    public void post() {
        Expense expense = scenario.someExpense();
        Person person = scenario.personThatIsNot(expense.getPerson());
        NewPaymentRequestDTO newPaymentRequestDTO = new NewPaymentRequestDTO(expense.getId(),
                expense.getPerson().getId(), person.getId(), TOMORROW.format(DD_MM_YYYY), 100);
        PaymentRequestDTO actual = Unirest.post("/api/paymentrequests")
                .body(newPaymentRequestDTO)
                .asObject(PaymentRequestDTO.class)
                .getBody();
        assertThat(actual.getExpenseId()).isNotNull();
        assertThat(actual.getFromPersonId()).isEqualTo(newPaymentRequestDTO.getFromPersonId());
        assertThat(actual.getToPersonId()).isEqualTo(newPaymentRequestDTO.getToPersonId());
        assertThat(actual.getDate()).isEqualTo(newPaymentRequestDTO.getDate());
        assertThat(actual.getAmount()).isEqualTo(newPaymentRequestDTO.getAmount());
        assertThat(actual.isPaid()).isFalse();
    }

    @Test
    @DisplayName("404 POST /api/paymentrequests - expense")
    public void post404expense() {
        Integer id = scenario.getUnusedExpenseId();
        Person fromPerson = scenario.somePerson();
        Person toPerson = scenario.personThatIsNot(fromPerson);
        NewPaymentRequestDTO newPaymentRequestDTO = new NewPaymentRequestDTO(id,
                fromPerson.getId(), toPerson.getId(), TOMORROW.format(DD_MM_YYYY), 100);
        HttpResponse<JsonNode> response = Unirest.post("/api/paymentrequests")
                .body(newPaymentRequestDTO)
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("404 POST /api/paymentrequests - from")
    public void post404fromPerson() {
        Expense expense = scenario.someExpense();
        int id = scenario.getUnusedPersonId();
        Person person = scenario.somePerson();
        NewPaymentRequestDTO newPaymentRequestDTO = new NewPaymentRequestDTO(expense.getId(), id,
                person.getId(), TOMORROW.format(DD_MM_YYYY), 100);
        HttpResponse<JsonNode> response = Unirest.post("/api/paymentrequests")
                .body(newPaymentRequestDTO)
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("404 POST /api/paymentrequests - to")
    public void post404toPerson() {
        Expense expense = scenario.someExpense();
        int id = scenario.getUnusedPersonId();
        NewPaymentRequestDTO newPaymentRequestDTO = new NewPaymentRequestDTO(expense.getId(),
                expense.getPerson().getId(), id, TOMORROW.format(DD_MM_YYYY), 100);
        HttpResponse<JsonNode> response = Unirest.post("/api/paymentrequests")
                .body(newPaymentRequestDTO)
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /api/paymentrequests/sent/{personId}")
    public void getSent() {
        PaymentRequest paymentRequest = scenario.somePaymentRequest();
        List<PaymentRequestDTO> actual = List.of(Unirest.get("/api/paymentrequests/sent/{personId}")
                        .routeParam("personId", Integer.toString(paymentRequest.getPersonRequestingPayment().getId()))
                .asObject(PaymentRequestDTO[].class).getBody());
        assertThat(actual).isNotEmpty();
    }

    @Test
    @DisplayName("404 GET /api/paymentrequests/sent/{personId}")
    public void getSent404() {
        int id = scenario.getUnusedPersonId();
        HttpResponse<JsonNode> response = Unirest.post("/api/paymentrequests/sent/{personId}")
                .routeParam("personId", Integer.toString(id))
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /api/paymentrequests/received/{personId}")
    public void getReceived() {
        PaymentRequest paymentRequest = scenario.somePaymentRequest();
        List<PaymentRequestDTO> actual = List.of(Unirest.get("/api/paymentrequests/received/{personId}")
                .routeParam("personId", Integer.toString(paymentRequest.getPersonWhoShouldPayBack().getId()))
                .asObject(PaymentRequestDTO[].class).getBody());
        assertThat(actual).isNotEmpty();
    }

    @Test
    @DisplayName("404 GET /api/paymentrequests/received/{personId}")
    public void getReceived404() {
        int id = scenario.getUnusedPersonId();
        HttpResponse<JsonNode> response = Unirest.post("/api/paymentrequests/received/{personId}")
                .routeParam("personId", Integer.toString(id))
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("DELETE /api/paymentrequests/{paymentRequestId}")
    public void delete() {
        PaymentRequest expected = scenario.someUnpaidPaymentRequest();
        HttpResponse<JsonNode> response = Unirest.delete("/api/paymentrequests/{paymentRequestId}")
                .routeParam("paymentRequestId", expected.getId().toString())
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("404 DELETE /api/paymentrequests/{paymentRequestId}")
    public void delete404() {
        int id = scenario.getUnusedPaymentRequestId();
        HttpResponse<JsonNode> response = Unirest.delete("/api/paymentrequests/{paymentRequestId}")
                .routeParam("paymentRequestId", Integer.toString(id))
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("400 DELETE /api/paymentrequests/{paymentRequestId}")
    public void delete400() {
        PaymentRequest paymentRequest = scenario.somePaidPaymentRequest();
        HttpResponse<JsonNode> response = Unirest.delete("/api/paymentrequests/{paymentRequestId}")
                .routeParam("paymentRequestId", Integer.toString(paymentRequest.getId()))
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
