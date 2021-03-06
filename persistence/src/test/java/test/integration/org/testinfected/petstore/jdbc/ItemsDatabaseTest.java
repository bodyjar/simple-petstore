package test.integration.org.testinfected.petstore.jdbc;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testinfected.petstore.QueryUnitOfWork;
import org.testinfected.petstore.Transactor;
import org.testinfected.petstore.UnitOfWork;
import org.testinfected.petstore.jdbc.ItemsDatabase;
import org.testinfected.petstore.jdbc.JDBCTransactor;
import org.testinfected.petstore.jdbc.ProductsDatabase;
import org.testinfected.petstore.product.*;
import test.support.org.testinfected.petstore.builders.Builder;
import test.support.org.testinfected.petstore.builders.ItemBuilder;
import test.support.org.testinfected.petstore.jdbc.Database;
import test.support.org.testinfected.petstore.jdbc.TestDatabaseEnvironment;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testinfected.petstore.jdbc.Access.idOf;
import static org.testinfected.petstore.jdbc.Access.productOf;
import static test.support.org.testinfected.petstore.builders.Builders.build;
import static test.support.org.testinfected.petstore.builders.ItemBuilder.an;
import static test.support.org.testinfected.petstore.builders.ItemBuilder.anItem;
import static test.support.org.testinfected.petstore.builders.ProductBuilder.aProduct;
import static test.support.org.testinfected.petstore.jdbc.HasFieldWithValue.hasField;

public class ItemsDatabaseTest {

    Database database = Database.in(TestDatabaseEnvironment.load());
    Connection connection = database.connect();
    Transactor transactor = new JDBCTransactor(connection);
    ProductCatalog productCatalog = new ProductsDatabase(connection);

    ItemsDatabase itemsDatabase = new ItemsDatabase(connection);

    @Before public void
    resetDatabase() throws Exception {
        database.reset();
    }

    @After public void
    closeConnection() throws SQLException {
        connection.close();
    }

    @SuppressWarnings("unchecked")
    @Test public void
    findsItemsByNumber() throws Exception {
        given(anItem().of(inCatalog(aProduct())).withNumber("12345678"));

        Item found = itemsDatabase.find(new ItemNumber("12345678"));
        assertThat("item", found, hasNumber("12345678"));
    }

    @SuppressWarnings("unchecked")
    @Test public void
    findsItemsByProductNumber() throws Exception {
        Product product = inCatalog(aProduct().withNumber("LAB-1234"));
        given(anItem().of(product), anItem().of(product));

        List<Item> availableItems = itemsDatabase.findByProductNumber("LAB-1234");
        assertThat("available items", availableItems, hasSize(2));
        assertThat("available items", availableItems, everyItem(hasProductNumber("LAB-1234")));
    }

    @SuppressWarnings("unchecked")
    @Test public void
    findsNothingIfProductHasNoAssociatedItemInInventory() throws Exception {
        given(anItem().of(inCatalog(aProduct().withNumber("DAL-5432"))));

        List<Item> availableItems = itemsDatabase.findByProductNumber(inCatalog(aProduct().withNumber("BOU-6789")).getNumber());
        assertThat("available items", availableItems, Matchers.<Item>empty());
    }

    @SuppressWarnings("unchecked")
    @Test public void
    canRoundTripItemsWithCompleteDetails() throws Exception {
        Collection<Item> sampleItems = build(
                an(inCatalog(aProduct().named("Labrador").describedAs("A fun and friendly dog").withPhoto("labrador.jpg"))).
                        withNumber("12345678").describedAs("Chocolate male").priced("58.00"),
                an(inCatalog(aProduct())).withNumber("87654321"));

        for (final Item item : sampleItems) {
            save(item);
            assertCanBeFoundByNumberWithSameState(item);
            assertCanBeFoundByProductNumberWithSameState(item);
        }
    }

    @SuppressWarnings("unchecked")
    @Test(expected = DuplicateItemException.class) public void
    referenceNumberShouldBeUnique() throws Exception {
        ItemBuilder existingItem = anItem().of(inCatalog(aProduct().withNumber("LAB-1234")));
        given(existingItem);

        save(existingItem.build());
    }

    private void assertCanBeFoundByNumberWithSameState(Item sample) {
        Item found = itemsDatabase.find(new ItemNumber(sample.getNumber()));
        assertThat("found by number", found, sameItemAs(sample));
    }

    private void assertCanBeFoundByProductNumberWithSameState(Item sample) {
        List<Item> found = itemsDatabase.findByProductNumber(sample.getProductNumber());
        assertThat("found by product number", uniqueElement(found), sameItemAs(sample));
    }

    private Item uniqueElement(List<Item> items) {
        if (items.isEmpty()) throw new AssertionError("No item matches");
        if (items.size() > 1) throw new AssertionError("Several items match");
        return items.get(0);
    }

    private Matcher<Item> sameItemAs(Item original) {
        return allOf(hasField("id", equalTo(idOf(original).get())),
                samePropertyValuesAs(original),
                hasField("product", sameProductAs(productOf(original).get())));
    }

    private Matcher<Product> sameProductAs(Product original) {
        return allOf(hasField("id", equalTo(idOf(original).get())),
                samePropertyValuesAs(original));
    }

    private Matcher<Item> hasProductNumber(final String number) {
        return new FeatureMatcher<Item, String>(equalTo(number), "has product number", "product number") {
            @Override protected String featureValueOf(Item actual) {
                return actual.getProductNumber();
            }
        };
    }

    private Product inCatalog(final Builder<Product> builder) throws Exception {
        return transactor.performQuery(new QueryUnitOfWork<Product>() {
            public Product query() throws Exception {
                Product product = builder.build();
                productCatalog.add(product);
                return product;
            }
        });
    }

    private void given(final Builder<Item>... items) throws Exception {
        given(build(items));
    }

    private void given(final List<Item> items) throws Exception {
        for (final Item item : items) given(item);
    }

    private void given(final Item item) throws Exception {
        save(item);
    }

    private void save(final Item item) throws Exception {
        transactor.perform(new UnitOfWork() {
            public void execute() throws Exception {
                itemsDatabase.add(item);
            }
        });
    }

    private Matcher<Item> hasNumber(final String number) {
        return new FeatureMatcher<Item, String>(equalTo(number), "has number", "number") {
            @Override protected String featureValueOf(Item actual) {
                return actual.getNumber();
            }
        };
    }
}
