package org.entcore.common.user;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.service.impl.BasicQuotaServiceTest;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.Neo4jContainer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The discipline group fm.discipline has no users property and fm.math, fm.physics as members ; fm.other belongs to
 * another group. The communication module is replaced by a bus consumer that only sees fm.math and fm.other.
 */
@RunWith(VertxUnitRunner.class)
public class FindVisibleMembersOfGroupsTest {
    @ClassRule
    public static Neo4jContainer<?> neo4jContainer = BasicQuotaServiceTest.createNeo4jContainer();
    private static final Vertx vertx = Vertx.vertx();
    private static final List<String> VISIBLE_IDS = Arrays.asList("fm.math", "fm.other");

    @BeforeClass
    public static void setUp(TestContext context) {
        final String base = neo4jContainer.getHttpUrl() + "/db/data/";
        Neo4j.getInstance().init(vertx, new JsonObject().put("server-uri", base).put("poolSize", 1));
        // Fake communication module : answers the visible identities among the expected ones
        vertx.eventBus().<JsonObject>consumer("wse.communication.users", message -> {
            final JsonArray expected = message.body().getJsonObject("request").getJsonArray("expectedVisiblesIds", new JsonArray());
            message.reply(new JsonArray(expected.stream()
                    .filter(VISIBLE_IDS::contains)
                    .map(id -> new JsonObject().put("id", id).put("isUser", true))
                    .collect(Collectors.toList())));
        });
        final String fixture =
                "CREATE (d:Group:FunctionGroup:DisciplineGroup:Visible {id: 'fm.discipline', name: 'Maths'}), " +
                "(o:Group:ProfileGroup {id: 'fm.other-group', users: 'BOTH'}), " +
                "(:User {id: 'fm.math', displayName: 'Math Teacher', lastName: 'Teacher', profiles: ['Teacher']})-[:IN]->(d), " +
                "(:User {id: 'fm.physics', displayName: 'Physics Teacher', lastName: 'Physics', profiles: ['Teacher']})-[:IN]->(d), " +
                "(:User {id: 'fm.other', displayName: 'Other', lastName: 'Other', profiles: ['Student']})-[:IN]->(o)";
        final Promise<Void> promise = Promise.promise();
        Neo4j.getInstance().execute(fixture, new JsonObject(), e -> {
            if ("ok".equals(e.body().getString("status"))) {
                promise.complete();
            } else {
                promise.fail(e.body().encode());
            }
        });
        promise.future().onComplete(context.asyncAssertSuccess());
    }

    @Test
    public void testReturnsOnlyVisibleMembersOfGroupWithoutUsersProperty(TestContext context) {
        UserUtils.findVisibleMembersOfGroups(vertx.eventBus(), "fm.requester", Collections.singletonList("fm.discipline"), true)
                .onComplete(context.asyncAssertSuccess(members -> {
                    context.assertEquals(Collections.singleton("fm.math"), ids(members),
                            "Visible member kept, invisible member and member of another group left out");
                    final JsonObject member = members.getJsonObject(0);
                    context.assertEquals("Math Teacher", member.getString("displayName"));
                    context.assertEquals("Teacher", member.getString("lastName"));
                    context.assertEquals(new JsonArray().add("Teacher"), member.getJsonArray("profiles"));
                }));
    }

    @Test
    public void testSeveralGroups(TestContext context) {
        UserUtils.findVisibleMembersOfGroups(vertx.eventBus(), "fm.requester", Arrays.asList("fm.discipline", "fm.other-group"), true)
                .onComplete(context.asyncAssertSuccess(members ->
                        context.assertEquals(new HashSet<>(Arrays.asList("fm.math", "fm.other")), ids(members))));
    }

    @Test
    public void testNoGroup(TestContext context) {
        UserUtils.findVisibleMembersOfGroups(vertx.eventBus(), "fm.requester", Collections.emptyList(), true)
                .onComplete(context.asyncAssertSuccess(members -> context.assertTrue(members.isEmpty())));
    }

    private static Set<String> ids(JsonArray members) {
        return members.stream().map(o -> ((JsonObject) o).getString("id")).collect(Collectors.toSet());
    }
}
