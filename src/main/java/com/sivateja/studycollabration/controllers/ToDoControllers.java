package com.sivateja.studycollabration.controllers;
import com.sivateja.studycollabration.dto.todo.ToDoCreateRequestDTO;
import com.sivateja.studycollabration.dto.todo.ToDoResponseDTO;
import com.sivateja.studycollabration.entities.Users;
import com.sivateja.studycollabration.repository.UserRepository;
import com.sivateja.studycollabration.services.ToDoServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/todo")
@RequiredArgsConstructor
public class ToDoControllers
{
    private final ToDoServices toDoServices;
    private final UserRepository userRepository;
    // ── helper — resolves UserDetails → Users entity ──────────────────────────
    private Users getUser(UserDetails userDetails) {
        return userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    //POST /api/todo/create/{roomId}
    @PostMapping("/create/{roomId}")
    public ResponseEntity<ToDoResponseDTO> createTodo(
            @PathVariable Long roomId,
            @RequestBody ToDoCreateRequestDTO req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(toDoServices.createTodo(roomId, req, getUser(userDetails)));
    }
    // GET /api/todo/room/{roomId}
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ToDoResponseDTO>> getTodosByRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(toDoServices.getTodosByRoom(roomId, getUser(userDetails)));
    }
    // PUT /api/todo/toggle/{todoId}
    @PutMapping("/toggle/{todoId}")
    public ResponseEntity<ToDoResponseDTO> toggleDone(
            @PathVariable Long todoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(toDoServices.toggleDone(todoId, getUser(userDetails)));
    }
    // DELETE /api/todo/delete/{todoId}
    @DeleteMapping("/delete/{todoId}")
    public ResponseEntity<Void> deleteTodo(
            @PathVariable Long todoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        toDoServices.deleteTodo(todoId, getUser(userDetails));
        return ResponseEntity.ok().build();
    }
    // GET /api/todo/room/{roomId}/pending
    @GetMapping("/room/{roomId}/pending")
    public ResponseEntity<List<ToDoResponseDTO>> getPendingTodos(
            @PathVariable Long roomId) {
        return ResponseEntity.ok(toDoServices.getPendingTodos(roomId));
    }
    // GET /api/todo/room/{roomId}/overdue
    @GetMapping("/room/{roomId}/overdue")
    public ResponseEntity<List<ToDoResponseDTO>> getOverdueTodos(
            @PathVariable Long roomId) {
        return ResponseEntity.ok(toDoServices.getOverdueTodos(roomId));
    }

}