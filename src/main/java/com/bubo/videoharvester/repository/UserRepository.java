package com.bubo.videoharvester.repository;

import com.bubo.videoharvester.entity.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<MyUser, Long> {

//    @Override
//    public List<User> findAll();
}
