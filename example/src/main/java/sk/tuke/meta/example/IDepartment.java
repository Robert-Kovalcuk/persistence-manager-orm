package sk.tuke.meta.example;

import javax.persistence.Id;


public interface IDepartment {
    @Id
    long pk = 0;

    String getName();
    void setName(String name);
    String getCode();
    void setCode(String code);
}
