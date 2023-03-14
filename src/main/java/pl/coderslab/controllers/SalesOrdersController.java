package pl.coderslab.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.SalesOrders;
import pl.coderslab.repository.CompaniesRepository;
import pl.coderslab.repository.SalesOrdersRepository;
import pl.coderslab.repository.SharesHeldRepository;
import pl.coderslab.repository.UserRepository;
import pl.coderslab.services.MarketService;
import pl.coderslab.services.SalesOrdersService;
import javax.validation.Valid;

@Controller
@RequestMapping("/sellorder")
public class SalesOrdersController {

    private final SalesOrdersRepository salesOrdersRepository;
    private final CompaniesRepository companiesRepository;
    private final UserRepository userRepository;
    private final SharesHeldRepository sharesHeldRepository;
    private final MarketService marketService;
    private final SalesOrdersService salesOrdersService;

    public SalesOrdersController(SalesOrdersRepository salesOrdersRepository, CompaniesRepository companiesRepository, UserRepository userRepository, SharesHeldRepository sharesHeldRepository, MarketService marketService, SalesOrdersService salesOrdersService) {
        this.salesOrdersRepository = salesOrdersRepository;
        this.companiesRepository = companiesRepository;
        this.userRepository = userRepository;
        this.sharesHeldRepository = sharesHeldRepository;
        this.marketService = marketService;
        this.salesOrdersService = salesOrdersService;
    }


    @RequestMapping("/add/{userId}/{companyId}")
    public String addSaleOrder(@PathVariable Long userId,
                               @PathVariable Long companyId, Model model){
       return salesOrdersService.addSaleOrder(userId, companyId, model);
    }

    @RequestMapping(value= ("/add/{userId}/{companyId}"), method = RequestMethod.POST)
    public String addSaleOrder(@Valid SalesOrders salesOrders, BindingResult result, @RequestParam int volumen, Model model){
       return salesOrdersService.addSaleOrderPost(salesOrders, result, volumen, model);
    }


    @RequestMapping("/edit/{userId}/{companyId}")
    public String editSalesOrder (@PathVariable Long userId, @PathVariable Long companyId, Model model) {
        model.addAttribute("salesOrder", salesOrdersRepository.findFirstByUser_IdAndCompany_Id(userId, companyId));
        return "salesOrder/add";
    }

    @RequestMapping (value = "/edit/{userId}/{companyId}", method = RequestMethod.POST)
    public String editSalesOrder (@Valid SalesOrders salesOrder, BindingResult result, Model model) {
        return salesOrdersService.editSalesOrder(salesOrder, result, model);
    }

    @RequestMapping("/delete/{userId}/{companyId}")
    public String delete (@PathVariable Long userId, @PathVariable Long companyId) {
       return salesOrdersService.delete(userId, companyId);
    }
}


