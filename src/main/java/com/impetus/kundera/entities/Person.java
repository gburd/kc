/*******************************************************************************
 * * Copyright 2016 Impetus Infotech.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 ******************************************************************************/
package com.impetus.kundera.entities;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * The Class Person.
 */
@Entity
@Cacheable
@Table( name = "PERSON",
        indexes = {
                @Index(name = "last_name_idx",  columnList="PERSON_LAST_NAME", unique = true),
                @Index(name = "email_idx", columnList="EMAIL",     unique = false)})
public class Person
{
    /** The person id. */
    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "PERSON_ID")
    private String personId;

    /** The person first name. */
    @Column(name = "PERSON_FIRST_NAME")
    private String personFirstName;

    /** The person last name. */
    @Column(name = "PERSON_LAST_NAME", nullable = false)
    private String personLastName;

    /** The age. */
    @Column(name = "AGE")
    private int age;

    /** Email addresses.
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "EMAIL")
    @Transient
    private Set<String> email = new HashSet<>();

    @Column(name = "UPDATED")
    @Temporal(TemporalType.DATE)
    private Date updated;
     */


    @Column(name = "VERSION")
    @Version
    private long version;


    /**
     * Gets the person id.
     *
     * @return the person id
     */
    public String getPersonId()
    {
        return personId;
    }

    /**
     * Sets the person id.
     *
     * @param personId
     *            the new person id
     */
    public void setPersonId(String personId)
    {
        this.personId = personId;
    }

    /**
     * Gets the person name.
     *
     * @return the person name
     */
    public String getPersonName()
    {
        return personFirstName + " " + personLastName;
    }

    /**
     * Gets the person first name.
     *
     * @return the person first name
     */
    public String getPersonFirstName()
    {
        return personFirstName;
    }

    /**
     * Sets the person first name.
     *
     * @param personFirstName
     *            the new person first name
     */
    public void setPersonFirstName(String personFirstName)
    {
        this.personFirstName = personFirstName;
    }

    /**
     * Gets the person last name.
     *
     * @return the person last name
     */
    public String getPersonLastName()
    {
        return personLastName;
    }

    /**
     * Sets the person last name.
     *
     * @param personLastName
     *            the new person last name
     */
    public void setPersonLastName(String personLastName)
    {
        this.personLastName = personLastName;
    }

    /**
     * Gets the age.
     *
     * @return the age
     */
    public int getAge()
    {
        return age;
    }

    /**
     * Sets the age.
     *
     * @param age
     *            the new age
     */
    public void setAge(int age)
    {
        this.age = age;
    }

//    public void addEmail(String email) { this.email.add(email); }

}
