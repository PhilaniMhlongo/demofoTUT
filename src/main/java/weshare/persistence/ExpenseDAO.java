package weshare.persistence;

/*
 ** DO NOT CHANGE!!
 */


import weshare.model.Expense;
import weshare.model.Person;

import java.util.Collection;

public interface ExpenseDAO extends DAO<Expense> {
    Collection<Expense> findExpensesForPerson(Person person);
}
