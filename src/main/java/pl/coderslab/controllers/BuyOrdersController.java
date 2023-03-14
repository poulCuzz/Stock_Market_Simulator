package pl.coderslab.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pl.coderslab.BuyOrders;
import pl.coderslab.User;
import pl.coderslab.repository.BuyOrdersRepository;
import pl.coderslab.repository.SalesOrdersRepository;
import pl.coderslab.repository.SharesHeldRepository;
import pl.coderslab.repository.UserRepository;
import pl.coderslab.services.BuyOrdersService;
import pl.coderslab.services.MarketService;

import javax.validation.Valid;

@Controller
@RequestMapping("/buyorder")
public class BuyOrdersController {

    private final BuyOrdersRepository buyOrdersRepository;
    private final BuyOrdersService buyOrdersService;
    private final SharesHeldRepository sharesHeldRepository;
    private final MarketService marketService;
    private final SalesOrdersRepository salesOrdersRepository;
    private final UserRepository userRepository;


    public BuyOrdersController(BuyOrdersRepository buyOrdersRepository, BuyOrdersService buyOrdersService, SharesHeldRepository sharesHeldRepository, MarketService marketService, SalesOrdersRepository salesOrdersRepository, UserRepository userRepository) {
        this.buyOrdersRepository = buyOrdersRepository;
        this.buyOrdersService = buyOrdersService;
        this.sharesHeldRepository = sharesHeldRepository;
        this.marketService = marketService;
        this.salesOrdersRepository = salesOrdersRepository;
        this.userRepository = userRepository;
    }

    @RequestMapping("/edit/{userId}/{companyId}")
    public String editBuyOrder (@PathVariable Long userId, @PathVariable Long companyId, Model model) {
        model.addAttribute("buyOrder", buyOrdersRepository.findFirstByUser_IdAndCompany_Id(userId, companyId));
        return "buyOrder/add";
    }

    @RequestMapping (value = "/edit/{userId}/{companyId}", method = RequestMethod.POST)
    public String editBuyOrder (@Valid BuyOrders buyOrder, BindingResult result, Model model) {
        return buyOrdersService.editBuyOrder(buyOrder, result, model);
    }

    @RequestMapping("/delete/{userId}/{companyId}")
    public String delete (@PathVariable Long userId, @PathVariable Long companyId) {
        return buyOrdersService.delete(userId, companyId);
    }
}

