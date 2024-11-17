package weshare.controller;

import io.javalin.http.Handler;
import org.javamoney.moneta.function.MonetaryFunctions;
import weshare.api.WeShareService;
import weshare.model.PaymentRequest;
import weshare.model.Person;
import weshare.server.WeShareServer;

import javax.money.MonetaryAmount;
import java.util.Collection;
import java.util.Map;

import static weshare.model.MoneyHelper.ZERO_RANDS;

public class PaymentRequestsSentController {
    public static final Handler view = (ctx) -> {
        Person loggedInPerson = WeShareServer.getPersonLoggedIn(ctx);
        Collection<PaymentRequest> paymentRequests = WeShareService.findPaymentRequestsSentBy(loggedInPerson.getId());
        ctx.render("paymentrequests_sent.html", Map.of(
                "paymentrequests", paymentRequests,
                "unpaid_total", getUnpaidPaymentRequests(paymentRequests)));
    };

    private static MonetaryAmount getUnpaidPaymentRequests(Collection<PaymentRequest> paymentRequests) {
        return paymentRequests.stream()
                .filter(pr -> !pr.isPaid())
                .map(PaymentRequest::getAmountToPay)
                .reduce(MonetaryFunctions.sum())
                .orElse(ZERO_RANDS);
    }
}
