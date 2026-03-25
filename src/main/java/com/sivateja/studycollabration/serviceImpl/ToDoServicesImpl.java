package com.sivateja.studycollabration.serviceImpl;
import com.sivateja.studycollabration.dto.todo.ToDoCreateRequestDTO;
import com.sivateja.studycollabration.dto.todo.ToDoResponseDTO;
import com.sivateja.studycollabration.entities.Room;
import com.sivateja.studycollabration.entities.ToDos;
import com.sivateja.studycollabration.entities.Users;
import com.sivateja.studycollabration.model.ToDoPriority;
import com.sivateja.studycollabration.repository.RoomRepository;
import com.sivateja.studycollabration.repository.ToDosRepository;
import com.sivateja.studycollabration.services.ToDoServices;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ToDoServicesImpl implements ToDoServices
{
    private final ToDosRepository toDosRepository;
    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private ToDoResponseDTO toResponse(ToDos todo)
    {
        return ToDoResponseDTO.builder()
                .id(todo.getId())
                .text(todo.getText())
                .priority(todo.getPriority())
                .done(todo.isDone())
                .dueDate(todo.getDueDate())
                .roomId(todo.getRoom().getId())
                .createdById(todo.getCreatedBy().getId())
                .createdByName(todo.getCreatedBy().getUserName())
                .createdAt(todo.getCreatedAt())
                .build();
    }

    public ToDos toDos(ToDoCreateRequestDTO req,Long roomId,Users user)
    {
        Room room=roomRepository.findById(roomId)
                .orElseThrow(()->new RuntimeException("room not found by this roomId"));
        ToDos todo = ToDos.builder()
                .text(req.getText())
                .priority(req.getPriority() != null ? req.getPriority() : ToDoPriority.MEDIUM)
                .done(false)
                .dueDate(req.getDueDate())
                .room(room)
                .createdBy(user)
                .build();
        return todo;
    }

    @Override
    public ToDoResponseDTO createTodo(Long roomId, ToDoCreateRequestDTO req, Users user)
    {
        ToDos todo = toDos(req, roomId, user);
        ToDos savedTodo = toDosRepository.save(todo);
        ToDoResponseDTO response = toResponse(savedTodo);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/todos", response);
        return response;
    }

    @Override
    public List<ToDoResponseDTO> getTodosByRoom(Long roomId, Users user) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        // only members can see todos
//        boolean isMember = room.getMembers().stream()
//                .anyMatch(m -> m.getId().equals(user.getId()));
//        if (!isMember)
//            throw new RuntimeException("Only room members can view tasks");
        return toDosRepository.findByRoomId(roomId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    // ── 3. Toggle Done ────────────────────────────────────────────────────────
    // PUT /api/todo/toggle/{todoId}
    // Makes toggle persist to DB instead of just localStorage
    @Override
    public ToDoResponseDTO toggleDone(Long todoId, Users user) {
        ToDos todo = toDosRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        // any room member can toggle — not just creator
        Long roomId = todo.getRoom().getId();
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        // ✅ Fix — filter out nulls first
//        boolean isMember = room.getMembers().stream()
//                .filter(m -> m != null)
//                .anyMatch(m -> m.getId().equals(user.getId()));
//        if (!isMember)
//            throw new RuntimeException("Only room members can update tasks");
        // flip the done status
        todo.setDone(!todo.isDone());
        ToDos saved = toDosRepository.save(todo);
        ToDoResponseDTO response = toResponse(saved);
        // broadcast toggle to all members in real time
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/todos/toggle", response
        );
        return response;
    }

    @Override
    public void deleteTodo(Long todoId, Users user) {
        ToDos todo = toDosRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        // only creator can delete their own todo
        if (!todo.getCreatedBy().getId().equals(user.getId()))
            throw new RuntimeException("Only the creator can delete this task");

        Long roomId = todo.getRoom().getId();
        toDosRepository.delete(todo);

        // broadcast deletion so all members' UI removes it
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/todos/delete",
                todoId  // just send the id — frontend removes by id
        );
    }
    // ── 5. Get Pending Todos ──────────────────────────────────────────────────
    // GET /api/todo/room/{roomId}/pending
    // Frontend shows: tasks.filter(t=>!t.done).length
    @Override
    public List<ToDoResponseDTO> getPendingTodos(Long roomId) {
        return toDosRepository.findByRoomIdAndDone(roomId, false)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── 6. Get Overdue Todos ──────────────────────────────────────────────────
    // GET /api/todo/room/{roomId}/overdue
    // Tasks where dueDate has passed and not done yet
    @Override
    public List<ToDoResponseDTO> getOverdueTodos(Long roomId) {
        return toDosRepository
                .findByRoomIdAndDoneFalseAndDueDateBefore(roomId, LocalDateTime.now())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }



}
