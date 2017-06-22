package com.example.crud.entities;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.io.Serializable;

@Data
@MappedSuperclass
public abstract class AbstractEntity<ID extends Serializable> {

    @Version
    protected long version;

    /*
    // SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(inv);
    // SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(product);
    //System.out.println("Product and Book have been persisted, inventory: " + inv.getPrimaryKey().toString() + ", product: " + product.getPrimaryKey().toString());

    @Transient
    @Autowired
    EntityManagerFactory emf;

    public ID getPrimaryKey() {
        final PersistenceUnitUtil util = emf.getPersistenceUnitUtil();
        Object id = util.getIdentifier(this);
        return (ID)id;
    }
    */

}
