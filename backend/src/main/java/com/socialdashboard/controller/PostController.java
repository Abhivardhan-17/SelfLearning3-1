package com.socialdashboard.controller;

import com.socialdashboard.dto.PostRequest;
import com.socialdashboard.service.PostService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService service;
    public PostController(PostService service) { this.service = service; }
    @GetMapping public List<Map<String,Object>> feed(Authentication auth) {
        return service.feed(auth.getName());
    }
    @PostMapping public Map<String,Object> create(@Valid @RequestBody PostRequest request, Authentication auth) {
        return service.create(request, auth.getName());
    }
    @DeleteMapping("/{id}") public Map<String,String> delete(@PathVariable Long id, Authentication auth) {
        service.delete(id, auth.getName()); return Map.of("message", "Post deleted");
    }
    @PostMapping("/{id}/like") public Map<String,Object> like(@PathVariable Long id, Authentication auth) {
        return service.toggleLike(id, auth.getName());
    }
}
