/*
 * Java EE security testing sample
 * hhttps://github.com/javaee-testing/security
 *
 * Copyright (c) 2014 samaxes.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.samaxes.javaeetesting.security;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 * Book service.
 *
 * @author Samuel Santos
 */
@Stateless
public class BookshelfService {

    @PersistenceContext(unitName = "bookshelfManager")
    private EntityManager entityManager;

    @RolesAllowed({ "User", "Manager" })
    public void addBook(Book book) {
        entityManager.persist(book);
    }

    @RolesAllowed({ "Manager" })
    public void deleteBook(Book book) {
        entityManager.remove(book);
    }

    @PermitAll
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Book> getBooks() {
        TypedQuery<Book> query = entityManager.createQuery("SELECT b from Book as b", Book.class);
        return query.getResultList();
    }
}
