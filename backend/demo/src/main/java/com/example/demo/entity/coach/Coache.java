package com.example.demo.entity.coach;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coaches")
public class Coache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String status;
    private String img;

    @OneToMany(mappedBy = "coache", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Merit> merit = new ArrayList<>();

    public Coache() {
    }

    public Coache(Long id, String name, String status, String img, List<Merit> merit) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.img = img;
        setMerit(merit);
    }

    public void setMerit(List<Merit> merits) {
        this.merit.clear();
        if (merits != null) {
            for (Merit m : merits) {
                m.setCoache(this);
                this.merit.add(m);
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public List<Merit> getMerit() {
        return merit;
    }
}
