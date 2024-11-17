package weshare.api;

import kong.unirest.HttpResponse;
import kong.unirest.HttpStatus;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import weshare.api.dto.NewPaymentDTO;
import weshare.api.dto.PaymentDTO;
import weshare.model.Payment;
import weshare.model.PaymentRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentApiTests extends ApiTestRunner {

    @Test
    @DisplayName("GET /api/payments/madeby/{personId}")
    public void getPaymentsMadeBy() {
        Payment expected = scenario.somePayment();
        List<PaymentDTO> dtos = List.of(Unirest.get("/api/payments/madeby/{personId}")
                .routeParam("personId", Integer.toString(expected.getPersonPaying().getId()))
                .asObject(PaymentDTO[].class)
                .getBody());
        assertThat(dtos).isNotEmpty();
    }

    @Test
    @DisplayName("404 GET /api/payments/madeby/{personId}")
    public void getPaymentsMadeBy404() {
        int id = scenario.getUnusedPersonId();
        HttpResponse<JsonNode> response = Unirest.get("/api/payments/madeby/{personId}")
                .routeParam("personId", Integer.toString(id))
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("POST /api/payment")
    public void post() {
        PaymentRequest paymentRequest = scenario.someUnpaidPaymentRequest();
        NewPaymentDTO newPaymentDTO = new NewPaymentDTO(paymentRequest.getExpenseId(),
                paymentRequest.getId(), paymentRequest.getPersonWhoShouldPayBack().getId());
        HttpResponse<JsonNode> response = Unirest.post("/api/payments")
                .body(newPaymentDTO)
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("404 POST /api/payment - expense")
    public void post400expense() {
        PaymentRequest paymentRequest = scenario.newPaymentRequest();
        NewPaymentDTO newPaymentDTO = new NewPaymentDTO(scenario.getUnusedExpenseId(),
                paymentRequest.getId(), paymentRequest.getPersonWhoShouldPayBack().getId());
        HttpResponse<JsonNode> response = Unirest.post("/api/payments")
                .body(newPaymentDTO)
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("404 POST /api/payment - payment request")
    public void post400PaymentRequest() {
        PaymentRequest paymentRequest = scenario.newPaymentRequest();
        int id = scenario.getUnusedPaymentRequestId();
        NewPaymentDTO newPaymentDTO = new NewPaymentDTO(paymentRequest.getExpenseId(),
                id, paymentRequest.getPersonWhoShouldPayBack().getId());
        HttpResponse<JsonNode> response = Unirest.post("/api/payments")
                .body(newPaymentDTO)
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("404 POST /api/payment - person")
    public void post400Person() {
        PaymentRequest paymentRequest = scenario.newPaymentRequest();
        int id = scenario.getUnusedPersonId();
        NewPaymentDTO newPaymentDTO = new NewPaymentDTO(paymentRequest.getExpenseId(),
                paymentRequest.getId(), id);
        HttpResponse<JsonNode> response = Unirest.post("/api/payments")
                .body(newPaymentDTO)
                .asJson();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}