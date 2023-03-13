package pl.coderslab;

public interface UserService {

    User findByUserName(String name);

    void saveUser(User user);
}
