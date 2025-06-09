package com.authService.repositories;

import com.authService.entities.UserInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<UserInfo, String> {
    public UserInfo findByUsername(String username);
}
