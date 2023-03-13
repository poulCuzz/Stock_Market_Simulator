package pl.coderslab.services;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import pl.coderslab.User;
@Service
public interface UserService {

    User findByUserName(String name);

    void saveUser(User user);


}