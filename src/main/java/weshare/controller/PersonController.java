package weshare.controller;

import io.javalin.http.Handler;
import weshare.api.WeShareService;
import weshare.model.Person;
import weshare.server.Routes;
import weshare.server.WeShareServer;

import java.util.Objects;

public class PersonController {

    public static final Handler logout = ctx -> {
        ctx.sessionAttribute(WeShareServer.SESSION_USER_KEY, null);
        ctx.redirect(Routes.LOGIN_PAGE);
    };

    public static final Handler login = context -> {
        String email = context.formParamAsClass("email", String.class)
                .check(Objects::nonNull, "Email is required")
                .get();
        Person person = WeShareService.findPersonByEmail(email)
                .orElseGet(() -> WeShareService.savePerson(new Person(email)));
        context.sessionAttribute(WeShareServer.SESSION_USER_KEY, person);
        context.redirect(Routes.EXPENSES);
    };
}
