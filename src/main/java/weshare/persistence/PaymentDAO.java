package weshare.persistence;

import weshare.model.Payment;
import weshare.model.Person;

import java.util.Collection;

public interface PaymentDAO extends DAO<Payment> {
    Collection<Payment> findByPerson(Person person);
}
