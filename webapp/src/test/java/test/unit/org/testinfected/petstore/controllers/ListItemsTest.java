package test.unit.org.testinfected.petstore.controllers;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testinfected.petstore.Page;
import org.testinfected.petstore.controllers.ListItems;
import org.testinfected.petstore.product.Item;
import org.testinfected.petstore.product.ItemInventory;
import test.support.org.testinfected.petstore.builders.Builder;
import test.support.org.testinfected.molecule.unit.MockRequest;
import test.support.org.testinfected.molecule.unit.MockResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static test.support.org.testinfected.petstore.builders.Builders.build;
import static test.support.org.testinfected.petstore.builders.ItemBuilder.anItem;
import static test.support.org.testinfected.petstore.builders.ProductBuilder.aProduct;
import static test.support.org.testinfected.molecule.unit.MockRequest.aRequest;
import static test.support.org.testinfected.molecule.unit.MockResponse.aResponse;

@RunWith(JMock.class)
public class ListItemsTest {

    Mockery context = new JUnit4Mockery();
    ItemInventory itemInventory = context.mock(ItemInventory.class);
    Page itemsPage = context.mock(Page.class);
    ListItems listItems = new ListItems(itemInventory, itemsPage);

    MockRequest request = aRequest();
    MockResponse response = aResponse();
    List<Item> items = new ArrayList<Item>();
    String productNumber = "LAB-1234";

    @Before public void
    prepareRequest() {
        request.addParameter("product", productNumber);
    }

    @SuppressWarnings("unchecked")
    @Test public void
    rendersItemsInInventoryMatchingProductNumber() throws Exception {
        searchYields(anItem().of(aProduct().withNumber(productNumber)));

        context.checking(new Expectations() {{
            oneOf(itemsPage).render(with(response), with(allOf(hasEntry("in-stock", true), hasEntry("items", items))));
        }});

        listItems.handle(request, response);
    }

    @SuppressWarnings("unchecked")
    @Test public void
    indicatesNoMatchWhenNoProductItemIsAvailable() throws Exception {
        searchYieldsNothing();

        context.checking(new Expectations() {{
            oneOf(itemsPage).render(with(response), with(hasEntry("in-stock", false)));
        }});

        listItems.handle(request, response);
    }

    @SuppressWarnings("unchecked")
    @Test public void
    makesMatchCountAvailableToView() throws Exception {
        searchYields(anItem(), anItem(), anItem());

        context.checking(new Expectations() {{
            oneOf(itemsPage).render(with(response), with(hasEntry("item-count", 3)));
        }});

        listItems.handle(request, response);
    }

    private Matcher<Map<? extends String, ?>> hasEntry(String name, Object value) {
        return Matchers.hasEntry(name, value);
    }

    @SuppressWarnings("unchecked")
    private void searchYieldsNothing() {
        searchYields();
    }

    private void searchYields(final Builder<Item>... results) {
        this.items.addAll(build(results));

        context.checking(new Expectations() {{
            allowing(itemInventory).findByProductNumber(productNumber); will(returnValue(items));
        }});
    }
}
