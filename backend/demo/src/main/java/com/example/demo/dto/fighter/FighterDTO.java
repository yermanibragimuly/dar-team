package com.example.demo.dto.fighter;

import java.util.List;

public class FighterDTO {
    private Long id;
    private Integer positionIndex;
    private String name;
    private String nickname;
    private String birthplace;
    private String weightClass;
    private String record;
    private String tko;
    private String solution;
    private String submissive;
    private String other;
    private String country;
    private String sport;
    private String rank;
    private String instagram;
    private String img;
    private List<AchievementDTO> achievements;

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getBirthplace() {
        return birthplace;
    }

    public void setBirthplace(String birthplace) {
        this.birthplace = birthplace;
    }

    public String getWeightClass() {
        return weightClass;
    }

    public void setWeightClass(String weightClass) {
        this.weightClass = weightClass;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public String getTko() {
        return tko;
    }

    public void setTko(String tko) {
        this.tko = tko;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getSubmissive() {
        return submissive;
    }

    public void setSubmissive(String submissive) {
        this.submissive = submissive;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Integer getPositionIndex() {return positionIndex;}
    public void setPositionIndex(Integer positionIndex) {this.positionIndex = positionIndex;}

    public List<AchievementDTO> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<AchievementDTO> achievements) {
        this.achievements = achievements;
    }
}
