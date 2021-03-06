package com.bat.controller;

import com.bat.model.Book;
import com.bat.model.BookForm;
import com.bat.model.Category;
import com.bat.service.book.IBookService;
import com.bat.service.categories.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@PropertySource("classpath:upload_file.properties")
@RequestMapping("/book")
public class BookController {
    @Value("${file-upload}")
    private String fileUpload;
    @Autowired
    private IBookService bookService;
    @Autowired
    private ICategoryService categoryService;

    @GetMapping("/list")
    public ModelAndView showList(){
        ModelAndView modelAndView = new ModelAndView("list");
        modelAndView.addObject("books",bookService.findAll());
        modelAndView.addObject("categories",categoryService.findAll());
        return modelAndView;
    }
    @GetMapping
    public ResponseEntity<Iterable<Book>> showAllBook(){
        return new ResponseEntity<>(bookService.findAll(), HttpStatus.OK);
    }
    @PostMapping
    public ResponseEntity<Book> saveBook(@ModelAttribute("bookForm") BookForm bookForm){
        MultipartFile multipartFile = bookForm.getImage();
        String fileName = multipartFile.getOriginalFilename();
        fileName = System.currentTimeMillis() + fileName;
        try {
            FileCopyUtils.copy(multipartFile.getBytes(),new File(fileUpload + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Book book = new Book(bookForm.getName(),bookForm.getPrice(),bookForm.getAuthor(),fileName,bookForm.getCategory());
        bookService.save(book);
        return new ResponseEntity<>(book,HttpStatus.CREATED);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Book> deleteBook(@PathVariable Long id){
        Optional<Book> optionalBook = bookService.findById(id);
        if (!optionalBook.isPresent()){
            new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        bookService.remove(id);
        return new ResponseEntity<>(optionalBook.get(),HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Book> findOne(@PathVariable Long id){
        return new ResponseEntity<>(bookService.findById(id).get(),HttpStatus.OK);
    }
    @PutMapping ("/{id}")
    public ResponseEntity<Book> editBook(@PathVariable Long id,@RequestBody Book book){
        Optional<Book> bookOptional = bookService.findById(id);
        book.setId(bookOptional.get().getId());
        if (!bookOptional.isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        bookService.save(book);
        return new ResponseEntity<>(bookOptional.get(),HttpStatus.OK);
    }
}
