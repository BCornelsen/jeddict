/**
 * 2.4.1.3 Examples of Derived Identities
 * Example 1 : Case (b)
 *
 */
package io.github.jeddict.jpa.derived.identities.example1.b;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Employee {

    @Id
    private Long empId;

    @Basic
    private String empName;

    public Long getEmpId() {
        return empId;
    }

    public void setEmpId(Long empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

}