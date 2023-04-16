package sk.tuke.meta.example;

import sk.tuke.meta.persistence.PersistenceManager;
import sk.tuke.meta.persistence.ReflectivePersistenceManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

public class Main {
    public static final String DB_PATH = "test.db";

    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);

        PersistenceManager manager = new ReflectivePersistenceManager(
                conn);

        manager.createTables();
        Optional<Person> p1 = manager.get(Person.class, 2);
        p1.ifPresentOrElse(person -> {
            System.out.println(person.getName());
        }, () -> {
            System.out.println("asd");
        });
       /* Optional<Person> marketing = manager.get(Person.class, 2);
        marketing.ifPresent(department -> {
            Person department1 = marketing.get();
            department1.setName("sdsdsd");
            try {
                manager.save(department1);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });*/
    }
}
