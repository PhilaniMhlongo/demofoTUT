package weshare.controller;

import io.javalin.http.Handler;
import weshare.api.WeShareService;
import weshare.api.dto.NewPaymentRequestDTO;
import weshare.model.Expense;
import weshare.model.Person;
import weshare.server.Routes;
import weshare.server.WeShareServer;

import java.util.Map;
import java.util.Objects;

public class PaymentRequestFormController {
    public static Handler paymentRequestForm = (ctx) -> {
        int expenseId = Objects.requireNonNull(ctx.queryParamAsClass("expenseId", Integer.class)).get();
        Expense expense = WeShareService.getExpense(expenseId).orElseThrow();
        ctx.render("paymentrequest_form.html", Map.of("expense", expense));
    };

    public static Handler capturePaymentRequest = (ctx) -> {
        int expenseId = Objects.requireNonNull(ctx.formParamAsClass("expense_id", Integer.class)).get();
        String emailOfPersonToPay = ctx.formParam("email");
        long amount = Long.parseLong(Objects.requireNonNull(ctx.formParam("amount")));
        String date = Objects.requireNonNull(ctx.formParam("due_date"));

        Person loggedInPerson = WeShareServer.getPersonLoggedIn(ctx);
        Person personToPay = WeShareService.findPersonByEmail(emailOfPersonToPay)
                .orElseGet(() -> WeShareService.savePerson(new Person(emailOfPersonToPay)));
        NewPaymentRequestDTO newPaymentRequestDTO =
                new NewPaymentRequestDTO(expenseId, loggedInPerson.getId(), personToPay.getId(), date, amount);
        WeShareService.createNewPaymentRequest(newPaymentRequestDTO);
        ctx.redirect(Routes.PAYMENT_REQUEST + "?expenseId=" + expenseId);
    };
}
