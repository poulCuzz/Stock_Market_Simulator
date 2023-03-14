package pl.coderslab.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import pl.coderslab.Companies;
import pl.coderslab.SharesHeld;
import pl.coderslab.User;
import pl.coderslab.repository.CompaniesRepository;
import pl.coderslab.repository.SharesHeldRepository;
import pl.coderslab.repository.UserRepository;

@Controller
@RequestMapping("/held")
public class SharesHeldController {

    private final CompaniesRepository companiesRepository;
    private final UserRepository userRepository;
    private final SharesHeldRepository sharesHeldRepository;

    public SharesHeldController(CompaniesRepository companiesRepository, UserRepository userRepository, SharesHeldRepository sharesHeldRepository) {
        this.companiesRepository = companiesRepository;
        this.userRepository = userRepository;
        this.sharesHeldRepository = sharesHeldRepository;
    }
    @GetMapping("/portfolio")
    public ModelAndView showPortfolio(@RequestParam Long userId) {
        ModelAndView mav = new ModelAndView("portfolio");
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        mav.addObject("user", user);
        return mav;
    }
    //kupowanie pierwszych akcji. Nie z rynku tylko bezpośrednio od spółek. Czyli z listy 20 zaznaczam i wybieram ile akcji danej firmy kupuje i dodają się one do sharesHeld

    @RequestMapping(value= "/add", method = RequestMethod.GET)
    public String purchaseFirstStocks(Model model) {
        model.addAttribute("companies", companiesRepository.findAll());
        model.addAttribute("user", userRepository.findById(4L));
        model.addAttribute("sharesHeld", new SharesHeld());
        return "first";
    }

    @RequestMapping(value= "/add", method = RequestMethod.POST)
    public String purchaseFirstStocks(Companies companies, User user, @RequestParam(name = "howMany") String howMany, Model model) {
        int howManyInt = Integer.parseInt(howMany);
        SharesHeld sharesHeld = new SharesHeld();
        sharesHeld.setCompany(companies);
        sharesHeld.setUser(user);
        sharesHeld.setPurchasePrice(companies.getPricePerStock());
        sharesHeld.setVolume(howManyInt);
        sharesHeld.setDateAndTime("???");
        sharesHeldRepository.save(sharesHeld);
        return "/";
    }

    @RequestMapping("/list")
    public String showSharesHeld(Model model, @RequestParam String userId){
        Long userIdLong = Long.parseLong(userId);
        User user = userRepository.findById(userIdLong).get();
        model.addAttribute("user", user);
        model.addAttribute("sharesHeld", sharesHeldRepository.findAllByUserId(userIdLong));
        return "sharesHeld/list";
    }
}
