package weshare.api;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.*;
import weshare.api.dto.LoginDTO;
import weshare.model.Person;

import java.util.Optional;

public class PersonApi {
    @OpenApi(
            summary = "Find all people that use WeShare",
            operationId = "findAllPeople",
            path = "/people",
            methods = HttpMethod.GET,
            tags = {"People"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Person[].class)})
            })
    public static void getAll(Context ctx) {
        ctx.json(WeShareService.findAllPeople());
    }

    @OpenApi(
            summary = "Login to WeShare",
            operationId = "login",
            path = "/api/people",
            methods = HttpMethod.POST,
            tags = {"People"},
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = LoginDTO.class)}),
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Person.class)}),
                    @OpenApiResponse(status = "400", description = "Bad email address")
            }
    )
    public static void create(Context ctx) {
        Optional<LoginDTO> loginDTO = ApiHelper.validLogin(ctx);
        if (loginDTO.isPresent()) {
            Person person = new Person(loginDTO.get().getEmail());
            ctx.json(WeShareService.savePerson(person));
            ctx.status(HttpStatus.OK);
        } else {
            ctx.status(HttpStatus.BAD_REQUEST);
        }
    }

    @OpenApi(
            summary = "Find a person by ID",
            operationId = "findPersonById",
            path = "/api/people/{personId}",
            methods = HttpMethod.GET,
            pathParams = {@OpenApiParam(name = "personId", description = "The ID of the person",
                                        type = Integer.class, required = true )},
            tags = {"People"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Person.class)}),
                    @OpenApiResponse(status = "404", description = "Person not found")
            }
    )
    public static void getOne(Context ctx) {
        Person person = ApiHelper.validPerson(ctx);
        ctx.json(person);
        ctx.status(HttpStatus.OK);
    }
}
