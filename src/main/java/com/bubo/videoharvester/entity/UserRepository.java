package com.bubo.videoharvester.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<MyUser, Long> {

//    @Override
//    public List<User> findAll();
}
