package sg.edu.nus.iss.readingcompanion.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import sg.edu.nus.iss.readingcompanion.model.Book;
import sg.edu.nus.iss.readingcompanion.model.User;
import sg.edu.nus.iss.readingcompanion.service.BookService;
import sg.edu.nus.iss.readingcompanion.service.NotesService;
import sg.edu.nus.iss.readingcompanion.service.QuotesService;
import sg.edu.nus.iss.readingcompanion.service.WordService;
import sg.edu.nus.iss.readingcompanion.utilities.Helper;



@Controller
@RequestMapping("/books")
public class BookController {
    @Autowired
    private BookService bookService;

    @Autowired
    private NotesService notesService;

    @Autowired
    private WordService wordService;

    @Autowired
    private QuotesService quotesService;
    
    @GetMapping(path = {"", "/all"})
    public String bookshelf(@AuthenticationPrincipal User user, @RequestParam(required = false) String filter, Model model) {
        List<Book> books = bookService.getBooksByUser(user.getUsername());
        List<Book> filtered = new ArrayList<>();
        if (filter != null) {
            for (Book b: books) {
                if (b.getStatus().equals(filter)) {
                    filtered.add(b);
                }
            }
        } else {
            filtered = books;
        }
        
        model.addAttribute("books", filtered);
        return "landing";
    }
    
    @PostMapping("/add")
    public String addToBookshelf(@ModelAttribute Book book, @AuthenticationPrincipal User user) {
        bookService.addBookToUserShelf(user.getUsername(), book);
        return "redirect:/books/all";
    }
    
    @PostMapping("/search")
    public String searchResult(@RequestBody MultiValueMap<String,String> map, Model model) {
        String q = Helper.generateQuery(map);
        model.addAttribute("q", q);
        model.addAttribute("searchResult", bookService.searchQuery(q));
        return "search-result";
    }
    
    @GetMapping("/advanced-search")
    public String advancedSearchPage() {
        return "advanced-search";
    }

    @PostMapping("/advanced-search")
    public String advancedSearch(@RequestBody MultiValueMap<String, String> map, Model model) {
        String query = Helper.generateQuery(map);
        model.addAttribute("q", query);
        model.addAttribute("searchResult", bookService.searchQuery(query));
        return "search-result";
    }
    

    @GetMapping("details/{id}")
    public String getBookDetails(@PathVariable String id, @AuthenticationPrincipal User user, Model model) {
        Book book = bookService.getBookDetails(user.getUsername(), id);
        model.addAttribute("book", book); // TODO: Default value for start and end dates
        model.addAttribute("notes", notesService.getNotes(user.getUsername(), book.getId()));
        model.addAttribute("words", wordService.getWordsForBook(user.getUsername(), id));
        model.addAttribute("quotes", quotesService.getQuotesForBook(user.getUsername(), id));
        return "book-details";

    }

    @GetMapping("/edit/{id}")
    public String bookDetailsForm(@PathVariable String id, @AuthenticationPrincipal User user, Model model) {
        Book book = bookService.getBookDetails(user.getUsername(), id);
        model.addAttribute("book", book);
        model.addAttribute("pageAdd", false);
        return "book-form";
    }
    
    @PostMapping("/edit") // TODO: Change URL here to something more generic, e.g. /put
    public String editBookDetails(@Valid @ModelAttribute Book book, BindingResult binding, @AuthenticationPrincipal User user) {
        if (binding.hasErrors()) {
            return "book-form";
        }

        bookService.addBookToUserShelf(user.getUsername(), book);
        return "redirect:/books/details/" + book.getId();
    }

    @GetMapping("/add")
    public String bookForm(Model model) {
        model.addAttribute("book", Book.manualInput());
        model.addAttribute("pageAdd", true);

        return "book-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@AuthenticationPrincipal User user, @PathVariable String id) {
        bookService.deleteBook(user.getUsername(), id);
        return "redirect:/books/all";
    }
    
    
    
}
