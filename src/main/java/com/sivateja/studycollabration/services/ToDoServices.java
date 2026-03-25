package com.sivateja.studycollabration.services;

import com.sivateja.studycollabration.dto.todo.ToDoCreateRequestDTO;
import com.sivateja.studycollabration.dto.todo.ToDoResponseDTO;
import com.sivateja.studycollabration.entities.Users;

import java.util.List;

public interface ToDoServices {

  // POST /api/todo/create/{roomId}
  // TasksTab: addTask() calls this
  ToDoResponseDTO createTodo(Long roomId, ToDoCreateRequestDTO req, Users user);

  // GET /api/todo/room/{roomId}
  // TasksTab: load todos on mount instead of localStorage
  List<ToDoResponseDTO> getTodosByRoom(Long roomId, Users user);

  // PUT /api/todo/toggle/{todoId}
  // TasksTab: toggle() — currently local only, make it persist to DB
  ToDoResponseDTO toggleDone(Long todoId, Users user);

  // DELETE /api/todo/delete/{todoId}
  // TasksTab: when you add delete button later
  void deleteTodo(Long todoId, Users user);

  // GET /api/todo/room/{roomId}/pending
  // TasksTab: shows pending count — tasks.filter(t=>!t.done).length
  List<ToDoResponseDTO> getPendingTodos(Long roomId);

  // GET /api/todo/room/{roomId}/overdue
  // Future: highlight overdue tasks in red
  List<ToDoResponseDTO> getOverdueTodos(Long roomId);
}
