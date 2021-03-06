package test.support.org.testinfected.petstore.builders;

import org.testinfected.petstore.billing.Address;
import org.testinfected.petstore.billing.CreditCardDetails;
import org.testinfected.petstore.billing.CreditCardType;

import static test.support.org.testinfected.petstore.builders.AddressBuilder.aValidAddress;
import static test.support.org.testinfected.petstore.builders.AddressBuilder.anAddress;

public class CreditCardBuilder implements Builder<CreditCardDetails> {

    private CreditCardType cardType = CreditCardType.visa;
    private String cardNumber;
    private String cardExpiryDate;
    private Address billingAddress = anAddress().build();

    public static CreditCardBuilder aVisa() {
        return aCreditCard().ofType(CreditCardType.visa);
    }

    public static CreditCardBuilder aCreditCard() {
        return new CreditCardBuilder();
    }

    public static CreditCardBuilder validVisaDetails() {
        return aVisa().
                withNumber("9999 9999 9999").
                withExpiryDate("12/12").
                billedTo(aValidAddress());
    }

    public CreditCardBuilder ofType(CreditCardType type) {
        this.cardType = type;
        return this;
    }

    public CreditCardBuilder withNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    public CreditCardBuilder withExpiryDate(String cardExpiryDate) {
        this.cardExpiryDate = cardExpiryDate;
        return this;
    }

    public CreditCardBuilder billedTo(AddressBuilder addressBuilder) {
        this.billingAddress = addressBuilder.build();
        return this;
    }

    public CreditCardDetails build() {
        return new CreditCardDetails(cardType, cardNumber, cardExpiryDate, billingAddress);
    }
}
