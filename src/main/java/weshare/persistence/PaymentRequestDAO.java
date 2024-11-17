package weshare.persistence;

import weshare.model.PaymentRequest;
import weshare.model.Person;

import java.util.Collection;

public interface PaymentRequestDAO extends DAO<PaymentRequest> {
    Collection<PaymentRequest> findPaymentRequestsSent(Person person);

    Collection<PaymentRequest> findPaymentRequestsReceived(Person person);
}
