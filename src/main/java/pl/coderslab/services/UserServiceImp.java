package pl.coderslab.services;

import org.springframework.stereotype.Service;
import pl.coderslab.User;

@Service
public class UserServiceImp implements UserService{
    @Override
    public User findByUserName(String name) {
        return null;
    }

    @Override
    public void saveUser(User user) {

    }
}
