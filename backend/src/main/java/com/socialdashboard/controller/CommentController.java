
package com.socialdashboard.controller;

import com.socialdashboard.model.Comment;
import com.socialdashboard.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:5173")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{postId}/comments")
    public Comment addComment(
            @PathVariable Long postId,
            @RequestBody CommentRequest request,
            Principal principal) {

        return commentService.addComment(
                postId,
                request.getContent(),
                principal.getName()
        );
    }

    @GetMapping("/{postId}/comments")
    public List<Comment> getComments(@PathVariable Long postId) {
        return commentService.getComments(postId);
    }

    public static class CommentRequest {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
