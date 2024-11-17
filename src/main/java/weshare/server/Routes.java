package weshare.server;

import weshare.controller.*;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes {
    public static final String APP = "/app";
    public static final String LOGIN_PAGE = "/index.html";
    public static final String LOGIN_ACTION = "/login.action";
    public static final String LOGOUT = APP + "/logout";

    public static final String EXPENSES = APP + "/expenses";
    public static final String NEW_EXPENSE = APP + "/newexpense";
    public static final String EXPENSE_ACTION = APP + "/expense.action";

    public static final String PAYMENT_REQUEST = APP + "/paymentrequest";
    public static final String PAYMENT_REQUEST_ACTION = APP + "/paymentrequest.action";
    public static final String PAYMENT_REQUESTS_SENT = APP + "/paymentrequests_sent";
    public static final String PAYMENT_REQUESTS_RECEIVED = APP + "/paymentrequests_received";

    private static final String PAYMENT_ACTION = APP + "/payment.action";

    public static void configure(WeShareServer server) {
        server.routes(() -> {
            post(LOGIN_ACTION, PersonController.login);
            get(LOGOUT, PersonController.logout);

            get(EXPENSES, ExpensesController.view);

            get(NEW_EXPENSE, ExpenseFormController.expensePage);
            post(EXPENSE_ACTION, ExpenseFormController.captureExpense);

            get(PAYMENT_REQUEST, PaymentRequestFormController.paymentRequestForm);
            post(PAYMENT_REQUEST_ACTION, PaymentRequestFormController.capturePaymentRequest);

            get(PAYMENT_REQUESTS_SENT, PaymentRequestsSentController.view);
            get(PAYMENT_REQUESTS_RECEIVED, PaymentRequestsReceivedController.view);

            post(PAYMENT_ACTION, PaymentFormController.pay);
        });
    }
}
