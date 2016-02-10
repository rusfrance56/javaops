package ru.javaops.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

/**
 * User: gkislin
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true)
    @Email
    @NotEmpty
    private String email;

    @Size(min = 2, max = 50)
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Size(min = 2, max = 50)
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "password", nullable = false)
    @Length(min = 5)
    private String password;

    @Email
    @Size(max = 100)
    @Column(length = 100, unique = true)
    @Pattern(regexp = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@gmail.com")
    private String gmail;

    @Size(min = 3, max = 50)
    private String location;

    @Size(max = 100)
    @Column(name = "info_source", length = 100)
    private String infoSource;

    @Size(max = 500)
    @Column(name = "about_me", length = 500)
    private String aboutMe;

    @Size(max = 100)
    private String website;

    @Size(max = 50)
    private String company;

    @Size(max = 50)
    private String skype;

    @Size(max = 100)
    private String github;

    @Size(max = 100)
    private String vk;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active = true;

    @Column(name = "registered_date", columnDefinition = "TIMESTAMP DEFAULT NOW()")
    private Date registeredDate = new Date();

    @Column(name = "activated_date")
    private Date activatedDate;

    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;

    @OneToMany(mappedBy = "group")
    private Set<UserGroup> userGroups;

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName == null ? "" : StringUtils.capitalize(firstName);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setActive(boolean activated) {
        this.active = activated;
    }

    public void setActivatedDate(Date activatedDate) {
        this.activatedDate = activatedDate;
    }

    public boolean isActive() {
        return active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public String getPassword() {
        return password;
    }

    public void setRoles(Collection<Role> roles) {
        this.roles = EnumSet.copyOf(roles);
    }

    public Set<UserGroup> getUserGroups() {
        return userGroups;
    }

    @Override
    public String toString() {
        return "User (" +
                "id=" + getId() +
                ", email=" + email +
                ", firstName=" + firstName +
                ", lastName=" + lastName +
                ", active=" + active +
                ", roles=" + roles +
                ')';
    }
}
