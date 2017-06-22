package com.example.crud.entities;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.io.Serializable;

@Data
@MappedSuperclass
public abstract class AbstractEntity<ID extends Serializable> {

    @Transient
    @Autowired
    EntityManagerFactory emf;

    @Version
    protected long version;

    public ID getPrimaryKey() {
        final PersistenceUnitUtil util = emf.getPersistenceUnitUtil();
        Object id = util.getIdentifier(this);
        return (ID)id;
    }

}
