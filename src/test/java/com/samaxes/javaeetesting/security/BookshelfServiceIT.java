/*
 * Java EE security testing sample
 * hhttps://github.com/javaee-testing/security
 *
 * Copyright 2014 samaxes.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.samaxes.javaeetesting.security;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Book service test cases.
 *
 * @author Samuel Santos
 */
@RunWith(Arquillian.class)
public class BookshelfServiceIT {

    @Inject
    private BookshelfService bookshelfService;

    @Inject
    private BookshelfManager manager;

    @Inject
    private BookshelfUser user;

    @Deployment
    public static JavaArchive createDeployment() throws IOException {
        return ShrinkWrap.create(JavaArchive.class, "javaee-testing-security.jar")
                .addClasses(Book.class, BookshelfService.class, BookshelfManager.class, BookshelfUser.class)
                .addAsManifestResource("META-INF/persistence.xml", "persistence.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    public void testAsManager() throws Exception {
        manager.call(new Callable<Book>() {
            @Override
            public Book call() throws Exception {
                bookshelfService.addBook(new Book("978-1-4302-4626-8", "Beginning Java EE 7"));
                bookshelfService.addBook(new Book("978-1-4493-2829-0", "Continuous Enterprise Development in Java"));

                List<Book> books = bookshelfService.getBooks();
                Assert.assertEquals("List.size()", 2, books.size());

                for (Book book : books) {
                    bookshelfService.deleteBook(book);
                }

                Assert.assertEquals("BookshelfService.getBooks()", 0, bookshelfService.getBooks().size());
                return null;
            }
        });
    }

    @Test
    public void testAsUser() throws Exception {
        user.call(new Callable<Book>() {
            @Override
            public Book call() throws Exception {
                bookshelfService.addBook(new Book("978-1-4302-4626-8", "Beginning Java EE 7"));
                bookshelfService.addBook(new Book("978-1-4493-2829-0", "Continuous Enterprise Development in Java"));

                List<Book> books = bookshelfService.getBooks();
                Assert.assertEquals("List.size()", 2, books.size());

                for (Book book : books) {
                    try {
                        bookshelfService.deleteBook(book);
                        Assert.fail("Users should not be allowed to delete");
                    } catch (EJBAccessException e) {
                        // Good, users cannot delete things
                    }
                }

                // The list should not be empty
                Assert.assertEquals("BookshelfService.getBooks()", 2, bookshelfService.getBooks().size());
                return null;
            }
        });
    }

    @Test
    public void testUnauthenticated() throws Exception {
        try {
            bookshelfService.addBook(new Book("978-1-4302-4626-8", "Beginning Java EE 7"));
            Assert.fail("Unauthenticated users should not be able to add books");
        } catch (EJBAccessException e) {
            // Good, unauthenticated users cannot add things
        }

        try {
            bookshelfService.deleteBook(null);
            Assert.fail("Unauthenticated users should not be allowed to delete");
        } catch (EJBAccessException e) {
            // Good, unauthenticated users cannot delete things
        }

        try {
            // Read access should be allowed
            @SuppressWarnings("unused")
            List<Book> books = bookshelfService.getBooks();
        } catch (EJBAccessException e) {
            Assert.fail("Read access should be allowed");
        }
    }
}
