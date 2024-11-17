package weshare.persistence.collectionbased;

/*
 ** DO NOT CHANGE!!
 */


import weshare.model.Expense;
import weshare.model.Person;
import weshare.persistence.ExpenseDAO;

import java.util.Collection;
import java.util.stream.Collectors;

public class ExpenseDAOImpl extends CollectionBasedDAO<Expense> implements ExpenseDAO {

    @Override
    public Collection<Expense> findExpensesForPerson(Person person) {
        return findAll().stream()
                .filter(e -> e.getPerson().equals(person))
                .collect(Collectors.toUnmodifiableList());
    }
}
