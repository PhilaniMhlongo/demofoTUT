package weshare.controller;

import io.javalin.http.Handler;
import weshare.api.WeShareService;
import weshare.model.Expense;
import weshare.model.Person;
import weshare.server.Routes;
import weshare.server.WeShareServer;

import java.time.LocalDate;
import java.util.Objects;

import static weshare.model.DateHelper.DD_MM_YYYY;
import static weshare.model.MoneyHelper.amountOf;

public class ExpenseFormController {
    public static final Handler expensePage = (ctx) -> {
        WeShareServer.getPersonLoggedIn(ctx);
        ctx.render("expense_form.html");
    };

    public static final Handler captureExpense = (ctx) -> {
        Person loggedInPerson = WeShareServer.getPersonLoggedIn(ctx);
        String description = Objects.requireNonNull(ctx.formParam("description"));
        long amount = Long.parseLong(Objects.requireNonNull(ctx.formParam("amount")));
        LocalDate date = LocalDate.parse(Objects.requireNonNull(ctx.formParam("date")), DD_MM_YYYY);

        Expense expense = new Expense(loggedInPerson, description, amountOf(amount), date);
        WeShareService.saveExpense(expense);
        ctx.redirect(Routes.EXPENSES);
    };
}
