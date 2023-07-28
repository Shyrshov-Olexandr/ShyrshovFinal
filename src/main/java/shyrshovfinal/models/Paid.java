package shyrshovfinal.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
public class Paid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course")
    private String course;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "age")
    private Integer age;

    @Column(name = "course_format")
    private String courseFormat;

    @Column(name = "course_type")
    private String courseType;

    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "status")
    private Status status;
    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "group_id")
    private Group group;
    @Column(name = "utm")
    private String utm;

    @Column(name = "msg")
    private String message;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "paid_id")
    private List<Comment> comments;

    @Column(name = "sum")
    private Integer sum;

    @Column(name = "alreadyPaid")
    private Integer alreadyPaid;

    @OneToOne
    @JoinColumn(name = "manager_id")
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Paid paid = (Paid) o;
        return Objects.equals(id, paid.id) && Objects.equals(course, paid.course) && Objects.equals(name, paid.name) && Objects.equals(surname, paid.surname) && Objects.equals(email, paid.email) && Objects.equals(phone, paid.phone) && Objects.equals(age, paid.age) && Objects.equals(courseFormat, paid.courseFormat) && Objects.equals(courseType, paid.courseType) && Objects.equals(createdAt, paid.createdAt) && status == paid.status && Objects.equals(group, paid.group) && Objects.equals(utm, paid.utm) && Objects.equals(message, paid.message) && Objects.equals(comments, paid.comments) && Objects.equals(sum, paid.sum) && Objects.equals(alreadyPaid, paid.alreadyPaid) && Objects.equals(user, paid.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, course, name, surname, email, phone, age, courseFormat, courseType, createdAt, status, group, utm, message, comments, sum, alreadyPaid, user);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getCourseFormat() {
        return courseFormat;
    }

    public void setCourseFormat(String courseFormat) {
        this.courseFormat = courseFormat;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getUtm() {
        return utm;
    }

    public void setUtm(String utm) {
        this.utm = utm;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }



    public Integer getSum() {
        return sum;
    }

    public void setSum(Integer sum) {
        this.sum = sum;
    }

    public Integer getAlreadyPaid() {
        return alreadyPaid;
    }

    public void setAlreadyPaid(Integer alreadyPaid) {
        this.alreadyPaid = alreadyPaid;
    }

    public Paid(Long id, String course, String name, String surname, String email, String phone, Integer age, String courseFormat, String courseType, Date createdAt, String utm, String message, Status status, Group group, List<Comment> comments, Integer sum, Integer alreadyPaid, User user) {
        this.id = id;
        this.course = course;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.age = age;
        this.courseFormat = courseFormat;
        this.courseType = courseType;
        this.createdAt = createdAt;
        this.utm = utm;
        this.message = message;
        this.status = status;
        this.group = group;
        this.comments = comments;
        this.sum = sum;
        this.alreadyPaid = alreadyPaid;
        this.user = user;
    }

    public Paid() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
