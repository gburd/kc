package com.example.crud.entities;

import javax.persistence.*;
import java.util.List;

/**
 * The Class Person.
 */
@Entity
@Cacheable
@Table(name = "PERSON",
        indexes = {
                @Index(name = "last_name_idx", columnList = "PERSON_LAST_NAME", unique = false)
            /*, @Index(name = "email_idx",      columnList="EMAIL",            unique = false)*/ })
public class Person extends AbstractAuditableEntity {
    /**
     * The person id.
     */
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private String personId;

    /**
     * The person first name.
     */
    @Column(name = "PERSON_FIRST_NAME")
    private String personFirstName;

    /**
     * The person last name.
     */
    @Column(name = "PERSON_LAST_NAME", nullable = false)
    private String personLastName;

    /**
     * The age.
     */
    @Column(name = "AGE")
    private int age;

    /**
     * Email addresses.
     *
     * @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     * @JoinColumn(name = "EMAIL")
     * @Transient private Set<String> email = new HashSet<>();
     * @Column(name = "UPDATED")
     * @Temporal(TemporalType.DATE) private Date updated;
     */

    @OneToMany(mappedBy = "seller")
    private List<Product> products;

    /**
     * Gets the person id.
     *
     * @return the person id
     */
    public String getPersonId() {
        return personId;
    }

    /**
     * Sets the person id.
     *
     * @param personId the new person id
     */
    public void setPersonId(String personId) {
        this.personId = personId;
    }

    /**
     * Gets the person name.
     *
     * @return the person name
     */
    public String getPersonName() {
        return personFirstName + " " + personLastName;
    }

    /**
     * Gets the person first name.
     *
     * @return the person first name
     */
    public String getPersonFirstName() {
        return personFirstName;
    }

    /**
     * Sets the person first name.
     *
     * @param personFirstName the new person first name
     */
    public void setPersonFirstName(String personFirstName) {
        this.personFirstName = personFirstName;
    }

    /**
     * Gets the person last name.
     *
     * @return the person last name
     */
    public String getPersonLastName() {
        return personLastName;
    }

    /**
     * Sets the person last name.
     *
     * @param personLastName the new person last name
     */
    public void setPersonLastName(String personLastName) {
        this.personLastName = personLastName;
    }

    /**
     * Gets the age.
     *
     * @return the age
     */
    public int getAge() {
        return age;
    }

    /**
     * Sets the age.
     *
     * @param age the new age
     */
    public void setAge(int age) {
        this.age = age;
    }

    //    public void addEmail(String email) { this.email.add(email); }

}
