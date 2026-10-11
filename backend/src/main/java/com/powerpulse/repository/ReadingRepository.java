package com.powerpulse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.powerpulse.model.Reading;

public interface ReadingRepository extends JpaRepository<Reading, Long> {

    List<Reading> findByUserIdOrderByReadingDateDescIdDesc(Long userId);

    Optional<Reading> findByIdAndUserId(Long id, Long userId);
}
