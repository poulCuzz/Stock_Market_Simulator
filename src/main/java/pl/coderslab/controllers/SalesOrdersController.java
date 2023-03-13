package pl.coderslab.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.BuyOrders;
import pl.coderslab.SalesOrders;
import pl.coderslab.SharesHeld;
import pl.coderslab.User;
import pl.coderslab.repository.CompaniesRepository;
import pl.coderslab.repository.SalesOrdersRepository;
import pl.coderslab.repository.SharesHeldRepository;
import pl.coderslab.repository.UserRepository;
import pl.coderslab.services.MarketService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/sellorder")
public class SalesOrdersController {

    private final SalesOrdersRepository salesOrdersRepository;
    private final CompaniesRepository companiesRepository;
    private final UserRepository userRepository;
    private final SharesHeldRepository sharesHeldRepository;
    private final MarketService marketService;

    public SalesOrdersController(SalesOrdersRepository salesOrdersRepository, CompaniesRepository companiesRepository, UserRepository userRepository, SharesHeldRepository sharesHeldRepository, MarketService marketService) {
        this.salesOrdersRepository = salesOrdersRepository;
        this.companiesRepository = companiesRepository;
        this.userRepository = userRepository;
        this.sharesHeldRepository = sharesHeldRepository;
        this.marketService = marketService;
    }

    // logika po wcisnięciu przycisku 'issue a sales order'

    @RequestMapping("/add/{userId}/{companyId}")
    public String addSaleOrder(@PathVariable Long userId,
                               @PathVariable Long companyId, Model model){
        SharesHeld sharesHeld = sharesHeldRepository.findFirstByUserIdAndCompanyId(userId, companyId);
        BuyOrders buyOrder = new BuyOrders();
        List<BuyOrders> list = marketService.getBuyOrders();
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).getCompany().equals(sharesHeld.getCompany())) {
                buyOrder = list.get(i);
            }
        }

        if(companiesRepository.findById(companyId).get().equals(buyOrder.getCompany())) {
            BuyOrders buyOrders = new BuyOrders();
            buyOrders.setCompany(companiesRepository.findById(companyId).get());
            buyOrders.setUser(buyOrder.getUser());
            buyOrders.setPriceLimit(buyOrder.getPriceLimit());
            buyOrders.setVolumen(buyOrder.getVolumen());
            model.addAttribute("buyOrders", buyOrders);
            return "redirect:/sell/" + buyOrder.getUser().getId() + "/" + buyOrder.getCompany().getId() + "/" + buyOrder.getVolumen() + "/" + buyOrder.getPriceLimit();
        }

        SalesOrders salesOrders = new SalesOrders();
        salesOrders.setCompany(companiesRepository.findById(companyId).get());
        salesOrders.setUser(userRepository.findById(userId).get());
        model.addAttribute("salesOrder", salesOrders);
        return "salesOrder/add";
    }

    @RequestMapping(value= ("/add/{userId}/{companyId}"), method = RequestMethod.POST)
    public String addSaleOrder(@Valid SalesOrders salesOrders, BindingResult result, @RequestParam int volumen, Model model){
        if(result.hasErrors()) {
            model.addAttribute("salesOrder", salesOrders);
            System.out.println("!!Error!!");
            return "salesOrder/add";
        }
        Long userId = salesOrders.getUser().getId();
        Long companyId = salesOrders.getCompany().getId();
        User user = userRepository.findById(userId).get();
        SharesHeld sharesHeld = sharesHeldRepository.findFirstByUserIdAndCompanyId(userId, companyId);
        if (volumen > sharesHeld.getVolume()) {

            model.addAttribute("errorMessage", "You have set the volume too high, you do not have that many shares!");
            model.addAttribute("salesOrder", salesOrders);
            return "salesOrder/add";
        }else if(volumen == sharesHeld.getVolume()) {
            sharesHeldRepository.delete(sharesHeld);
            salesOrdersRepository.save(salesOrders);
            return "redirect:/held/list?userId=" + userId;
        }else if(volumen < sharesHeld.getVolume()){
            sharesHeld.setVolume(sharesHeld.getVolume()-volumen);
            sharesHeld.setValueAll(companiesRepository.findById(companyId).get().getPricePerStock()*(sharesHeld.getVolume()-volumen));
            sharesHeldRepository.save(sharesHeld);
            salesOrdersRepository.save(salesOrders);
            return "redirect:/market";
        }else {
            model.addAttribute("errorMessage", "we're sorry, something went wrong");
            System.out.println("we're sorry, something went wrong");
            return "redirect:/held/list?userId=" + userId;
        }

    }


    @RequestMapping("/edit/{userId}/{companyId}")
    public String editSalesOrder (@PathVariable Long userId, @PathVariable Long companyId, Model model) {
        model.addAttribute("salesOrder", salesOrdersRepository.findFirstByUser_IdAndCompany_Id(userId, companyId));
        return "salesOrder/add";
    }

    @RequestMapping (value = "/edit/{userId}/{companyId}", method = RequestMethod.POST)
    public String editSalesOrder (@Valid SalesOrders salesOrder, BindingResult result, Model model) {
        if(result.hasErrors()){
            model.addAttribute("salesOrder", salesOrder);
            return "salesOrder/add";
        }
        LocalDateTime localDateTime = LocalDateTime.now();
        String dateAndTime = localDateTime.toString().replaceAll("\\..*", "").replaceFirst("T", "  ");
        SharesHeld sharesHeld = sharesHeldRepository.findFirstByUserIdAndCompanyId(salesOrder.getUser().getId(), salesOrder.getCompany().getId());
        SalesOrders salesOrderMain = salesOrdersRepository.findFirstByUser_IdAndCompany_Id(salesOrder.getUser().getId(), salesOrder.getCompany().getId());
        BuyOrders buyOrder = new BuyOrders();
        List<BuyOrders> list = marketService.getBuyOrders();
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).getCompany().equals(salesOrder.getCompany())) {
                buyOrder = list.get(i);
            }else {
                buyOrder = null;
            }
        }

        try{
            if(buyOrder.getUser() != null) {
                if(salesOrder.getPriceLimit() <= buyOrder.getPriceLimit()) {
                    model.addAttribute("errorMessage", "There is already an order on the stock exchange that matches your price limit.");
                    return "redirect:/market";
                }
            }
        }
        catch (NullPointerException e) {
            model.addAttribute("errorMessage", "There is no matching announcement on the stock exchange!");
            e.printStackTrace();
        }



        if(salesOrder.getVolumen() > (sharesHeld.getVolume() + salesOrderMain.getVolumen())) {
            model.addAttribute("errorMessage", "You do not have that many shares!");
            return "redirect:/market";
        }

        Long userId = salesOrder.getUser().getId();
        Long companyId = salesOrder.getCompany().getId();
        SalesOrders editedSalesOrder = salesOrdersRepository.findFirstByUser_IdAndCompany_Id(userId, companyId);
        editedSalesOrder.setVolumen(salesOrder.getVolumen());
        editedSalesOrder.setCompany(salesOrder.getCompany());
        editedSalesOrder.setUser(salesOrder.getUser());
        editedSalesOrder.setPriceLimit(salesOrder.getPriceLimit());
        salesOrdersRepository.save(editedSalesOrder);

        if(salesOrder.getVolumen() < salesOrderMain.getVolumen()) {
            SharesHeld sharesHeld1 = new SharesHeld();
            sharesHeld1.setDateAndTime(dateAndTime);
            sharesHeld1.setPurchasePrice(salesOrderMain.getPriceLimit());
            sharesHeld1.setCompany(salesOrder.getCompany());
            sharesHeld1.setUser(salesOrder.getUser());
            sharesHeld1.setVolume(salesOrderMain.getVolumen() - salesOrder.getVolumen());
            sharesHeld1.setValueAll((salesOrderMain.getVolumen() - salesOrder.getVolumen()) * salesOrder.getCompany().getPricePerStock());
            sharesHeld1.setPurchasePriceAll();
            sharesHeld1.setProfitOrLoss();
            sharesHeldRepository.save(sharesHeld1);
        }
        if(salesOrder.getVolumen() > salesOrderMain.getVolumen()){
            int volumen = sharesHeld.getVolume() - (salesOrder.getVolumen() - salesOrderMain.getVolumen());
            sharesHeld.setVolume(volumen);
            sharesHeld.setValueAll(volumen * salesOrder.getCompany().getPricePerStock());
            sharesHeld.setPurchasePrice(sharesHeld.getPurchasePrice());
            sharesHeld.setPurchasePriceAll();
            sharesHeld.setProfitOrLoss();
            sharesHeldRepository.save(sharesHeld);
        }

        return "redirect:/market";
    }

    @RequestMapping("/delete/{userId}/{companyId}")
    public String delete (@PathVariable Long userId, @PathVariable Long companyId) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String dateAndTime = localDateTime.toString().replaceAll("\\..*", "").replaceFirst("T", "  ");
        SharesHeld sharesHeld1 = sharesHeldRepository.findFirstByUserIdAndCompanyId(userId, companyId);
        SalesOrders salesOrders = salesOrdersRepository.findFirstByUser_IdAndCompany_Id(userId, companyId);
        if(sharesHeld1 == null) {
            SharesHeld sharesHeld = new SharesHeld();
            sharesHeld.setVolume(salesOrders.getVolumen());
            sharesHeld.setUser(salesOrders.getUser());
            sharesHeld.setCompany(salesOrders.getCompany());
            sharesHeld.setPurchasePrice(salesOrders.getPriceLimit());
            sharesHeld.setDateAndTime(dateAndTime);
            sharesHeld.setValueAll(salesOrders.getCompany().getPricePerStock() * salesOrders.getVolumen());
            sharesHeld.setPurchasePriceAll();
            sharesHeld.setProfitOrLoss();
            sharesHeldRepository.save(sharesHeld);

            salesOrdersRepository.delete(salesOrders);
            return "redirect:/market";
        }else {
            sharesHeld1.setVolume(sharesHeld1.getVolume() + salesOrders.getVolumen());
            sharesHeld1.setValueAll(sharesHeld1.getValueAll() + sharesHeld1.getCompany().getPricePerStock() * salesOrders.getVolumen());
            sharesHeld1.setPurchasePriceAll();
            sharesHeld1.setProfitOrLoss();
            sharesHeldRepository.save(sharesHeld1);
            salesOrdersRepository.delete(salesOrders);
            return "redirect:/market";
        }

    }

}

