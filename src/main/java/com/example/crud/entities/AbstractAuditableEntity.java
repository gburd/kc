package com.example.crud.entities;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
public abstract class AbstractAuditableEntity<ID extends Serializable> extends AbstractEntity<ID> {

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDateTime;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDateTime;

    @CreatedBy
    private String createdByUser;

    @LastModifiedBy
    private String modifiedByUser;

}
