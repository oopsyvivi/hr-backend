package com.hrms.hr_backend.repository;

import com.hrms.hr_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
}
//```

//**What this does:**

//By extending `JpaRepository` you automatically get:
//```
//save()        //← insert or update
//findById()    //← find by ID
//findAll()     //← get all users
//delete()      //← delete user
//```

//And we added two custom methods:
//```
//findByEmail()     //← find user by email for login
//existsByEmail()   //← check if email already registered
