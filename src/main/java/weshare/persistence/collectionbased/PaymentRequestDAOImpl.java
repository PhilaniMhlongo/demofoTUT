package weshare.persistence.collectionbased;

import weshare.model.PaymentRequest;
import weshare.model.Person;
import weshare.persistence.PaymentRequestDAO;

import java.util.Collection;
import java.util.stream.Collectors;

public class PaymentRequestDAOImpl extends CollectionBasedDAO<PaymentRequest> implements PaymentRequestDAO {
    @Override
    public Collection<PaymentRequest> findPaymentRequestsSent(Person person) {
        return findAll().stream()
                .filter(pr -> pr.getPersonRequestingPayment().equals(person))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Collection<PaymentRequest> findPaymentRequestsReceived(Person person) {
        return findAll().stream()
                .filter(pr -> pr.getPersonWhoShouldPayBack().equals(person))
                .collect(Collectors.toUnmodifiableList());
    }
}
