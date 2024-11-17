package weshare.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JsonMapper;
import io.javalin.openapi.plugin.OpenApiConfiguration;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.rendering.template.JavalinThymeleaf;
import io.javalin.security.AccessManager;
import io.javalin.security.RouteRole;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.NullSessionDataStore;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import weshare.WeShareException;
import weshare.api.*;
import weshare.api.dto.NewPaymentDTO;
import weshare.model.Expense;
import weshare.model.PaymentRequest;
import weshare.model.Person;
import weshare.persistence.ExpenseDAO;
import weshare.persistence.PaymentDAO;
import weshare.persistence.PaymentRequestDAO;
import weshare.persistence.PersonDAO;
import weshare.persistence.collectionbased.ExpenseDAOImpl;
import weshare.persistence.collectionbased.PaymentDAOImpl;
import weshare.persistence.collectionbased.PaymentRequestDAOImpl;
import weshare.persistence.collectionbased.PersonDAOImpl;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static io.javalin.apibuilder.ApiBuilder.*;
import static weshare.model.DateHelper.*;
import static weshare.model.MoneyHelper.amountOf;

public class WeShareServer {
    private static final String PUBLIC_DIR = "/public";
    public static final String SESSION_USER_KEY = "user";

    private static final String TEMPLATES_DIR = "/public/";
    private final Javalin appServer;

    public static void main(String[] args) {
        WeShareServer server = new WeShareServer();
        try {
            seedDemoData();
        } catch (WeShareException e) {
            throw new RuntimeException(e);
        }
        server.start(5050);
    }

    public WeShareServer() {
        ServiceRegistry.configure(PersonDAO.class, new PersonDAOImpl());
        ServiceRegistry.configure(ExpenseDAO.class, new ExpenseDAOImpl());
        ServiceRegistry.configure(PaymentRequestDAO.class, new PaymentRequestDAOImpl());
        ServiceRegistry.configure(PaymentDAO.class, new PaymentDAOImpl());


        JavalinThymeleaf.init(templateEngine());

        appServer = Javalin.create(config -> {
            String deprecatedDocsPath = "/swagger-docs";
            config.plugins.register(new OpenApiPlugin(getOpenApiConfiguration(deprecatedDocsPath)));
            config.plugins.register(new SwaggerPlugin(getSwaggerConfiguration(deprecatedDocsPath)));

            config.accessManager(accessManager());
            config.jetty.sessionHandler(sessionHandler());
            config.jsonMapper(createGsonMapper());

            config.staticFiles.add(PUBLIC_DIR, Location.CLASSPATH);
        });

        appServer.routes(configureApi());
        Routes.configure(this);

        appServer.exception(WeShareException.class, (exception, ctx) -> {
            ctx.json(exception.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST);
        });
    }

    @NotNull
    private static SwaggerConfiguration getSwaggerConfiguration(String deprecatedDocsPath) {
        SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();
        swaggerConfiguration.setUiPath("/swagger");
        swaggerConfiguration.setDocumentationPath(deprecatedDocsPath);
        return swaggerConfiguration;
    }

    @NotNull
    private static OpenApiConfiguration getOpenApiConfiguration(String deprecatedDocsPath) {
        OpenApiConfiguration openApiConfiguration = new OpenApiConfiguration();
        openApiConfiguration.getInfo().setTitle("WeShare API");
        openApiConfiguration.getInfo().setVersion("1.0");
        openApiConfiguration.setDocumentationPath(deprecatedDocsPath);
        return openApiConfiguration;
    }

    @NotNull
    private EndpointGroup configureApi() {
        return () -> {
            path("api", () -> {

                path("people", () -> {
                    get(PersonApi::getAll);
                    post(PersonApi::create);
                    path("/{personId}", () -> get(PersonApi::getOne));
                });
                path("expenses", () -> {
                    get(ExpenseApi::getAll);
                    post(ExpenseApi::create);
                    path("/person/{personId}", () -> get(ExpenseApi::findByPersonId));
                    path("/{expenseId}", () -> get(ExpenseApi::getOne));
                });
                path("paymentrequests", () -> {
                    get(PaymentRequestApi::getAll);
                    post(PaymentRequestApi::create);
                    path("/sent/{personId}", () -> get(PaymentRequestApi::findPaymentRequestsSent));
                    path("/received/{personId}", () -> get(PaymentRequestApi::findPaymentRequestsReceived));
                    path("{paymentRequestId}", () -> {
                        get(PaymentRequestApi::getOne);
                        delete(PaymentRequestApi::recall);
                    });
                });
                path("payments", () -> {
                    post(PaymentApi::pay);
                    path("/madeby/{personId}", () -> get(PaymentApi::getPaymentsMadeBy));
                });
            });
        };
    }

    private static void seedDemoData() throws WeShareException {
        Person student1 = new Person("student1@wethinkcode.co.za");
        Person student2 = new Person("student2@wethinkcode.co.za");
        Person student3 = new Person("student3@wethinkcode.co.za");
        for (Person person : Arrays.asList(student1, student2, student3)) {
            WeShareService.savePerson(person);
        }

        Expense expense1 = new Expense(student1, "Lunch", amountOf(300), TODAY.minusDays(5));
        Expense expense2 = new Expense(student1, "Airtime", amountOf(100), YESTERDAY);
        Expense expense3 = new Expense(student2, "Movies", amountOf(150), TODAY.minusWeeks(1));
        Expense expense4 = new Expense(student3, "Ice cream", amountOf(50), TODAY.minusDays(3));
        for (Expense expense : Arrays.asList(expense1, expense2, expense3, expense4)) {
            WeShareService.saveExpense(expense);
        }

        PaymentRequest paymentRequest1 = expense1.requestPayment(student2, amountOf(100), YESTERDAY);
        PaymentRequest paymentRequest2 = expense1.requestPayment(student3, amountOf(100), TOMORROW);
        for (PaymentRequest paymentRequest : Arrays.asList(paymentRequest1, paymentRequest2)) {
            WeShareService.savePaymentRequest(paymentRequest);
        }

        NewPaymentDTO newPaymentDTO = new NewPaymentDTO(paymentRequest1.getExpenseId(),
                paymentRequest1.getId(), paymentRequest1.getPersonWhoShouldPayBack().getId());
        WeShareService.payPaymentRequest(newPaymentDTO);
    }

    /**
     * Use GSON for serialisation instead of Jackson
     * because GSON allows for serialisation of objects without noargs constructors.
     *
     * @return A JsonMapper for Javalin
     */
    private static JsonMapper createGsonMapper() {
        Gson gson = new GsonBuilder().create();
        return new JsonMapper() {
            @NotNull
            @Override
            public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
                return gson.fromJson(json, targetType);
            }

            @NotNull
            @Override
            public String toJsonString(@NotNull Object obj, @NotNull Type type) {
                return gson.toJson(obj, type);
            }
        };
    }

    public void start(int port) {
        this.appServer.start(port);
    }

    public void stop() {
        this.appServer.stop();
    }

    public int port() {
        return appServer.port();
    }

    public void routes(EndpointGroup group) {
        appServer.routes(group);
    }

    private TemplateEngine templateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix(TEMPLATES_DIR);
        templateEngine.setTemplateResolver(resolver);
        templateEngine.addDialect(new LayoutDialect());
        return templateEngine;
    }


    @Nullable
    public static Person getPersonLoggedIn(Context context) {
        return context.sessionAttribute(SESSION_USER_KEY);
    }

    private static Supplier<SessionHandler> sessionHandler() {
        SessionHandler sessionHandler = new SessionHandler();
        SessionCache sessionCache = new DefaultSessionCache(sessionHandler);
        sessionCache.setSessionDataStore(new NullSessionDataStore());
        sessionHandler.setSessionCache(sessionCache);
        sessionHandler.setHttpOnly(true);
        return () -> sessionHandler;
    }

    private AccessManager accessManager() {

        return new AccessManager() {
            @Override
            public void manage(@NotNull Handler handler, @NotNull Context context, @NotNull Set<? extends RouteRole> set) throws Exception {
                if (hasNoSession(context)) {
                    context.redirect(Routes.LOGIN_PAGE);
                } else {
                    handler.handle(context);
                }
            }

            private boolean hasNoSession(@NotNull Context context) {
                Person loggedInPerson = context.sessionAttribute(SESSION_USER_KEY);
                return Objects.isNull(loggedInPerson) && context.path().startsWith(Routes.APP);
            }

        };
    }
}
