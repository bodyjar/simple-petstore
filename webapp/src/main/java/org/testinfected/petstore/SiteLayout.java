package org.testinfected.petstore;

import org.testinfected.petstore.order.Cart;
import org.testinfected.molecule.decoration.Layout;
import org.testinfected.molecule.Request;
import org.testinfected.molecule.Response;
import org.testinfected.molecule.middlewares.AbstractMiddleware;
import org.testinfected.molecule.middlewares.FilterMap;
import org.testinfected.molecule.middlewares.SiteMesh;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import static org.testinfected.petstore.util.SessionScope.sessionScopeOf;

public class SiteLayout extends AbstractMiddleware {

    private RenderingEngine renderer;

    public SiteLayout(RenderingEngine renderer) {
        this.renderer = renderer;
    }

    public void handle(Request request, Response response) throws Exception {
        Cart cart = sessionScopeOf(request).cart();
        FilterMap filtering = new FilterMap();
        filtering.map("/", SiteMesh.html(new MainLayout("main", renderer, cart)));
        filtering.connectTo(successor);
        filtering.handle(request, response);
    }

    public static class MainLayout implements Layout {
        private final String template;
        private final RenderingEngine renderer;
        private final Cart cart;

        public MainLayout(String template, RenderingEngine renderer, Cart cart) {
            this.template = template;
            this.renderer = renderer;
            this.cart = cart;
        }

        public void render(Writer out, Map<String, Object> fragments) throws IOException {
            fragments.put("cart", cart);
            renderer.render(out, template, fragments);
        }
    }
}
