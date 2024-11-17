package weshare.persistence.collectionbased;

/*
 ** DO NOT CHANGE!!
 */


import weshare.model.Person;
import weshare.persistence.PersonDAO;

import java.util.Optional;

/**
 * {@inheritDoc}
 */
public class PersonDAOImpl extends CollectionBasedDAO<Person> implements PersonDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<weshare.model.Person> findByEmail(String email) {
        return findAll().stream()
                .filter(p -> p.getEmail().equals(email)).findFirst();
    }
}
