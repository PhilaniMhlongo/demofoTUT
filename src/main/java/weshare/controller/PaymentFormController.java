package weshare.controller;

import io.javalin.http.Handler;
import weshare.api.WeShareService;
import weshare.api.dto.NewPaymentDTO;
import weshare.model.Person;
import weshare.server.Routes;
import weshare.server.WeShareServer;

import java.util.Objects;

public class PaymentFormController {

    public static final Handler pay = (ctx) -> {
        Person loggedInPerson = WeShareServer.getPersonLoggedIn(ctx);
        int expenseId = Objects.requireNonNull(ctx.formParamAsClass("expense_id", Integer.class)).get();
        int paymentRequestId = Objects.requireNonNull(ctx.formParamAsClass("paymentrequest_id", Integer.class)).get();

        NewPaymentDTO newPaymentDTO = new NewPaymentDTO(expenseId, paymentRequestId, loggedInPerson.getId());
        WeShareService.payPaymentRequest(newPaymentDTO);
        ctx.redirect(Routes.PAYMENT_REQUESTS_RECEIVED);
    };
}
