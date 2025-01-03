package sg.edu.nus.iss.readingcompanion.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;

import sg.edu.nus.iss.readingcompanion.model.Quote;
import sg.edu.nus.iss.readingcompanion.model.User;
import sg.edu.nus.iss.readingcompanion.service.QuotesService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/quotes")
public class QuoteController {
    @Autowired
    private QuotesService quotesService;

    @PostMapping("/add")
    public String addQuote(@AuthenticationPrincipal User user, @RequestBody MultiValueMap<String, String> form) {
        String bookId = form.getFirst("bookId");
        quotesService.saveQuote(user.getUsername(), bookId, form.getFirst("newQuote"));

        return "redirect:/books/details/" + bookId + "?view=quotes";
    }

    @PostMapping("/delete")
    public String deleteQuote(@AuthenticationPrincipal User user, @RequestBody MultiValueMap<String, String> form) {
        String bookId = form.getFirst("bookId");
        String quoteText = form.getFirst("quoteText");
        quotesService.deleteQuote(user.getUsername(), bookId, new Quote(quoteText));
        return "redirect:/books/details/" + bookId + "?view=quotes";
    }
    

}
