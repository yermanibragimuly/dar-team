package com.example.demo.dto.coache;

import java.util.List;

public class CoacheDTO {
    private Long id;
    private String name;
    private String status;
    private String img;
    private List<MeritDTO> merit;

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

    public List<MeritDTO> getMerit() {
        return merit;
    }

    public void setMerit(List<MeritDTO> merit) {
        this.merit = merit;
    }
}
