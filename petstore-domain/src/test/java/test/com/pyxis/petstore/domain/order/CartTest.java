package test.com.pyxis.petstore.domain.order;

import com.pyxis.petstore.domain.order.Cart;
import com.pyxis.petstore.domain.order.CartItem;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static test.support.com.pyxis.petstore.builders.ItemBuilder.anItem;

public class CartTest {
    Cart cart = new Cart();

    @Test public void
    isEmptyByDefault() {
        assertTrue("contains item(s)", cart.isEmpty());
        assertThat(cart.getGrandTotal(), equalTo(BigDecimal.ZERO));
        assertThat(cart.getTotalQuantity(), equalTo(0));
    }

    @Test public void
    containsCartItemsInBuyingOrder() {
        String[] itemNumbers = { "11111111", "22222222", "33333333" };
        for (String itemNumber : itemNumbers) {
            cart.add(anItem().withNumber(itemNumber).build());
        }
        assertTrue("cart is empty", !cart.isEmpty());
        assertThat(cart.getItems(), containsItems(
                number("11111111"),
                number("22222222"),
                number("33333333")));
        assertThat(cart.getTotalQuantity(), equalTo(3));
    }

    @Test(expected = UnsupportedOperationException.class) public void
    listOfItemsCannotBeModified() {
        cart.getItems().clear();
    }

    @Test public void
    calculatesGrandTotal() {
        String[] prices = { "50", "75.50", "12.75" };
        BigDecimal expectedSubTotal = new BigDecimal("138.25");

        for (String price : prices) {
            cart.add(anItem().priced(price).build());
        }
        assertThat(cart.getGrandTotal(), equalTo(expectedSubTotal));
    }

    @Test public void
    groupsItemsByNumber() {
        String[] itemNumbers = { "11111111", "11111111", "22222222" };

        for (String number : itemNumbers) {
            cart.add(anItem().withNumber(number).build());
        }
        assertThat(cart.getItems(), containsItems(
                with(number("11111111"), quantity(2)),
                with(number("22222222"), quantity(1))));
        assertThat(cart.getTotalQuantity(), equalTo(3));
    }

    private Matcher<Iterable<CartItem>> containsItems(Matcher<CartItem>... cartItemMatchers) {
        return contains(cartItemMatchers);
    }

    private Matcher<CartItem> with(Matcher<CartItem>... cartItemMatchers) {
        return allOf(cartItemMatchers);
    }

    private Matcher<CartItem> quantity(int count) {
        return hasProperty("quantity", equalTo(count));
    }

    private Matcher<CartItem> number(String number) {
        return hasProperty("itemNumber", equalTo(number));
    }
}