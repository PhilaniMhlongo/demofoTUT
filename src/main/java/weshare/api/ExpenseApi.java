package weshare.api;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.*;
import weshare.api.dto.ExpenseDTO;
import weshare.api.dto.NewExpenseDTO;
import weshare.model.Expense;
import weshare.model.Person;

import java.util.Collection;
import java.util.List;

public class ExpenseApi {

    @OpenApi(
            summary = "Create a new expense for a person",
            tags = {"Expenses"},
            operationId = "createExpense",
            path = "/api/expenses",
            methods = HttpMethod.POST,
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NewExpenseDTO.class)}),
            responses = {
                    @OpenApiResponse(status = "201", content = {@OpenApiContent(from = ExpenseDTO.class)}),
                    @OpenApiResponse(status = "404", description = "Person not found")
            }
    )
    public static void create(Context ctx) {
        NewExpenseDTO unsaved = ApiHelper.validNewExpenseDTO(ctx);
        ExpenseDTO saved = WeShareService.createNewExpense(unsaved);
        ctx.json(saved);
        ctx.status(HttpStatus.CREATED);
    }

    @OpenApi(
            summary = "Find all expenses",
            operationId = "findAllExpenses",
            path = "/api/expenses",
            methods = HttpMethod.GET,
            tags = {"Expenses"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = ExpenseDTO[].class)})
            })
    public static void getAll(Context ctx) {
        ctx.json(mapExpenses(WeShareService.findAllExpenses()));
        ctx.status(HttpStatus.OK);
    }

    @OpenApi(
            summary = "Find an expense by ID",
            operationId = "findExpenseById",
            path = "/api/expenses/{expenseId}",
            methods = HttpMethod.GET,
            pathParams = {@OpenApiParam(name = "expenseId", description = "The expense ID",
                                        type = Integer.class, required = true)},
            tags = {"Expenses"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = ExpenseDTO.class)}),
                    @OpenApiResponse(status = "404", description = "Expense not found")
            }
    )
    public static void getOne(Context ctx) {
        Expense expense = ApiHelper.validExpense(ctx);
        ctx.json(ExpenseDTO.fromExpense(expense));
        ctx.status(HttpStatus.OK);
    }

    @OpenApi(
            summary = "Find expenses for a person",
            operationId = "findExpensesByPerson",
            path = "/api/expenses/person/{personId}",
            methods = HttpMethod.GET,
            pathParams = {@OpenApiParam(name = "personId",
                    type = Integer.class,
                    description = "The ID of the person",
                    required = true)},
            tags = {"Expenses"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = ExpenseDTO[].class)})
            })
    public static void findByPersonId(Context ctx) {
        Person person = ApiHelper.validPerson(ctx);
        Collection<Expense> expenses = WeShareService.findExpensesForPerson(person.getId());
        ctx.json(mapExpenses(expenses));
        ctx.status(HttpStatus.OK);
    }

    private static List<ExpenseDTO> mapExpenses(Collection<Expense> all) {
        return all.stream().map(ExpenseDTO::fromExpense).toList();
    }
}
