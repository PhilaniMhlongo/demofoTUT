package weshare.api;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.*;
import weshare.api.dto.NewPaymentDTO;
import weshare.api.dto.PaymentDTO;
import weshare.model.Payment;
import weshare.model.Person;

import java.util.Collection;

public class PaymentApi {
    @OpenApi(
            summary = "Pay a payment request",
            operationId = "payPaymentRequest",
            path = "/api/payments",
            methods = HttpMethod.POST,
            tags = {"Payments"},
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NewPaymentDTO.class)}),
            responses = {
                    @OpenApiResponse(status = "201", content = {@OpenApiContent(from = PaymentDTO.class)}),
                    @OpenApiResponse(status = "400", description = "Could not make payment"),
                    @OpenApiResponse(status = "404", description = "Person, Expense or Payment Request not found")
            }
    )
    public static void pay(Context ctx) {
        NewPaymentDTO newPaymentDTO = ApiHelper.validNewPaymentDTO(ctx);
        Payment payment = WeShareService.payPaymentRequest(newPaymentDTO);
        ctx.json(PaymentDTO.fromPayment(payment));
        ctx.status(HttpStatus.CREATED);
    }

    @OpenApi(
            summary = "Find all payments made by a person",
            operationId = "findPaymentsMadeByPerson",
            path = "/api/payments/madeby/{personId}",
            pathParams = {@OpenApiParam(name = "personId", description = "The ID of the person",
                    type = Integer.class, required = true )},
            methods = HttpMethod.GET,
            tags = {"Payments"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = PaymentDTO[].class)})
            })
    public static void getPaymentsMadeBy(Context ctx) {
        Person person = ApiHelper.validPerson(ctx);
        Collection<Payment> payments = WeShareService.findAllPaymentsMadeBy(person.getId());
        Collection<PaymentDTO> dtos = mapToDTO(payments);
        ctx.json(dtos);
    }

    private static Collection<PaymentDTO> mapToDTO(Collection<Payment> all) {
        return all.stream().map(PaymentDTO::fromPayment).toList();
    }
}
