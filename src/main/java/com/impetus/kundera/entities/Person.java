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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The Class Person.
 */
@Entity
@Table(name = "PERSON")
public class Person
{

    /** The person id. */
    @Id
    @Column(name = "PERSON_ID")
    private String personId;

    /** The person first name. */
    @Column(name = "PERSON_FIRST_NAME")
    private String personFirstName;

    /** The person last name. */
    @Column(name = "PERSON_LAST_NAME")
    private String personLastName;

    /** The age. */
    @Column(name = "AGE")
    private int age;

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

}
