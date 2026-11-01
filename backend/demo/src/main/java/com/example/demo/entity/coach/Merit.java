package com.example.demo.entity.coach;

import jakarta.persistence.*;

@Entity
@Table(name = "merits")
public class Merit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String list;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coache_id", nullable = false)
    private Coache coache;

    public Merit() {
    }

    public Merit(Long id, String list, Coache coache) {
        this.id = id;
        this.list = list;
        this.coache = coache;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
    }

    public Coache getCoache() {
        return coache;
    }

    public void setCoache(Coache coache) {
        this.coache = coache;
    }
}
