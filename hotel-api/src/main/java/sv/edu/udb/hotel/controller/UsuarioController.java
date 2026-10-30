package sv.edu.udb.hotel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.hotel.dto.request.EstadoUsuarioRequest;
import sv.edu.udb.hotel.dto.request.UsuarioRequest;
import sv.edu.udb.hotel.dto.response.MensajeResponse;
import sv.edu.udb.hotel.dto.response.UsuarioResponse;
import sv.edu.udb.hotel.model.enums.Rol;
import sv.edu.udb.hotel.service.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "2. Usuarios", description = "Administración de usuarios (solo ADMIN)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Filtro opcional por rol")
    public List<UsuarioResponse> listar(@Parameter(example = "RECEPCIONISTA") @RequestParam(required = false) Rol rol) {
        return usuarioService.listar(rol);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un usuario")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Crear usuario (cualquier rol)")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Si la contraseña viene vacía se conserva la actual")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return usuarioService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar usuario")
    public UsuarioResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoUsuarioRequest request) {
        return usuarioService.cambiarEstado(id, request.activo());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public MensajeResponse eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return new MensajeResponse("Usuario eliminado correctamente.");
    }
}
