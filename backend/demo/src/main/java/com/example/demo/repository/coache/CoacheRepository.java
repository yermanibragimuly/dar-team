package com.example.demo.repository.coache;

import com.example.demo.entity.coach.Coache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoacheRepository extends JpaRepository<Coache,Long> {
}
