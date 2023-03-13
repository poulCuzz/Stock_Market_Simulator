package pl.coderslab.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import pl.coderslab.CurrentUser;
import pl.coderslab.User;
import pl.coderslab.repository.UserRepository;
import pl.coderslab.UserService;

import javax.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {



    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/create-user")
    @ResponseBody
    public String createUser() {
        User user = new User();
        user.setMoneyUsd(1000);
        user.setAge(22);
        user.setEnabled(1);
        user.setFirstName("Adrian");
        user.setLastName("Twardowski");
        user.setNick("admin");
        user.setPassword("admin");
        userService.saveUser(user);
        return "admin";
    }

    @GetMapping("/admin")
    @ResponseBody
    public String admin(@AuthenticationPrincipal CurrentUser customUser) {
        User entityUser = customUser.getUser();
        return "Hello " + entityUser.getNick();
    }

    @RequestMapping(value = {"/login"}, method = RequestMethod.GET)
    public String login() {
        return "admin/login";
    }

    @RequestMapping("/add")
    public String addUser(Model model){
        model.addAttribute("user", new User());
        return "/user";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String save(@Valid User user, BindingResult result, Model model) {
        if(result.hasErrors()) {
            model.addAttribute("user", user);
            return "/start";
        }
        userRepository.save(user);
        return "redirect:/";
    }
}
