package sk.tuke.meta.example;

import sk.tuke.meta.persistence.PersistenceManager;
import sk.tuke.meta.persistence.ReflectivePersistenceManager;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.Optional;

public class Main {
    public static final String DB_PATH = "test.db";

    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);

        PersistenceManager manager = new ReflectivePersistenceManager(
                conn, Department.class, Person.class);

        manager.createTables();

        /*Department development = new Department("Development", "DVLP");
        Department marketing = new Department("Marketing", "MARK");
        Department operations = new Department("Operations", "OPRS");

        Person hrasko = new Person("Janko", "Hrasko", 30);
        hrasko.setDepartment(development);
        Person mrkvicka = new Person("Jozko", "Mrkvicka", 25);
        mrkvicka.setDepartment(marketing);
        Person novak = new Person("Jan", "Novak", 45);
        novak.setDepartment(operations);


        manager.save(hrasko);
        manager.save(mrkvicka);
        manager.save(novak);

        List<Department> persons = manager.getAll(Department.class);
        for (Department person : persons) {
            manager.delete(person);
        }*/
        List<Person> result = manager.getBy(Person.class, "surname", "Janko");
        System.out.println(result.size());
    }
}
