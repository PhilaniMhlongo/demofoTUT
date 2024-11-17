package weshare.controller;

import io.javalin.http.Handler;
import org.javamoney.moneta.function.MonetaryFunctions;
import org.jetbrains.annotations.NotNull;
import weshare.api.WeShareService;
import weshare.model.Expense;
import weshare.model.Person;
import weshare.server.WeShareServer;

import javax.money.MonetaryAmount;
import java.util.Collection;
import java.util.Map;

import static weshare.model.MoneyHelper.ZERO_RANDS;

public class ExpensesController {

    public static final Handler view = context -> {
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<Expense> expenses = WeShareService.findExpensesForPerson(personLoggedIn.getId()).stream()
                .filter(e -> !e.isFullyPaidByOthers()).toList();
        MonetaryAmount grandTotal = calculateGrandTotal(expenses);
        Map<String, Object> viewModel = Map.of(
                "expenses", expenses,
                "grandTotal", grandTotal
        );
        context.render("expenses.html", viewModel);
    };

    @NotNull
    private static MonetaryAmount calculateGrandTotal(Collection<Expense> expenses) {
        return expenses.stream()
                .map(Expense::amountLessPaymentsReceived)
                .reduce(MonetaryFunctions.sum())
                .orElse(ZERO_RANDS);
    }

}
