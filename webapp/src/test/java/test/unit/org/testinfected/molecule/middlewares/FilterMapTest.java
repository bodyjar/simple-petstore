package test.unit.org.testinfected.molecule.middlewares;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testinfected.molecule.Application;
import org.testinfected.molecule.Middleware;
import org.testinfected.molecule.Request;
import org.testinfected.molecule.matchers.Matchers;
import org.testinfected.molecule.middlewares.FilterMap;
import org.testinfected.molecule.util.Matcher;
import test.support.org.testinfected.molecule.unit.MockRequest;
import test.support.org.testinfected.molecule.unit.MockResponse;

import static test.support.org.testinfected.molecule.unit.MockRequest.aRequest;
import static test.support.org.testinfected.molecule.unit.MockResponse.aResponse;

@RunWith(JMock.class)
public class FilterMapTest {

    Mockery context = new JUnit4Mockery();
    Application successor = context.mock(Application.class, "successor");
    Sequence filtering = context.sequence("filtering");

    FilterMap filters = new FilterMap();

    MockRequest request = aRequest();
    MockResponse response = aResponse();

    @Before public void
    chainWithSuccessor() {
        filters.connectTo(successor);
    }

    @Test public void
    immediatelyForwardsRequestWhenNoFilterIsRegistered() throws Exception {
        expectForward();
        filters.handle(request, response);
    }

    @Test public void
    runsRequestThroughFilterThatMatches() throws Exception {
        final Middleware ignored = context.mock(Middleware.class, "ignored");
        filters.map(noRequest(), ignored);
        final Middleware applies = context.mock(Middleware.class, "applies");
        filters.map(anyRequest(), applies);

        context.checking(new Expectations() {{
            oneOf(applies).connectTo(successor); inSequence(filtering);
            oneOf(applies).handle(request, response); inSequence(filtering);
        }});

        filters.handle(request, response);
    }

    @Test public void
    forwardsRequestIfNoFilterMatches() throws Exception {
        final Middleware filter = context.mock(Middleware.class);
        filters.map(noRequest(), filter);

        expectForward();
        filters.handle(request, response);
    }

    @Test public void
    runsRequestThroughFilterMappedToPrefixOfRequestPath() throws Exception {
        request.withPath("/filtered/path");

        final Middleware filter = context.mock(Middleware.class);
        filters.map("/filtered", filter);

        context.checking(new Expectations() {{
            allowing(filter).connectTo(successor);
            oneOf(filter).handle(request, response);
        }});

        filters.handle(request, response);
    }

    @Test public void
    appliesLastRegisteredOfMatchingFilters() throws Exception {
        final Middleware ignored = context.mock(Middleware.class, "ignored");
        filters.map(anyRequest(), ignored);
        final Middleware applies = context.mock(Middleware.class, "applies");
        filters.map(anyRequest(), applies);

        context.checking(new Expectations() {{
            allowing(applies).connectTo(successor);
            oneOf(applies).handle(request, response);
        }});

        filters.handle(request, response);
    }

    private void expectForward() throws Exception {
        context.checking(new Expectations() {{
            oneOf(successor).handle(with(same(request)), with(same(response)));
        }});
    }

    private Matcher<Request> anyRequest() {
        return Matchers.anything();
    }

    private Matcher<Request> noRequest() {
        return Matchers.nothing();
    }
}
