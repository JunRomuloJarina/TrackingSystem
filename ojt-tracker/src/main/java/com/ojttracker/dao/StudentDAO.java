package com.ojttracker.dao;

import com.ojttracker.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * Data access contract for student profiles.
 */
public interface StudentDAO {

    Student save(Student student);

    List<Student> findAll();

    Optional<Student> findFirst();

    Optional<Student> findById(int id);

    void update(Student student);

    void delete(int id);

    boolean exists();
}