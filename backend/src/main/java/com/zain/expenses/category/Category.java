package com.zain.expenses.category;

import com.zain.expenses.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryKind kind;

    protected Category() {
        // required by JPA
    }

    public Category(User user, String name) {
        this(user, name, CategoryKind.SPENDING);
    }

    public Category(User user, String name, CategoryKind kind) {
        this.user = user;
        this.name = name;
        this.kind = kind;
    }

    public void rename(String newName) {
        this.name = newName;
    }

    public void changeKind(CategoryKind newKind) {
        this.kind = newKind;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public CategoryKind getKind() {
        return kind;
    }
}
