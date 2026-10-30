package sv.edu.udb.hotel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.hotel.dto.request.LoginRequest;
import sv.edu.udb.hotel.dto.request.RegistroRequest;
import sv.edu.udb.hotel.dto.response.AuthResponse;
import sv.edu.udb.hotel.dto.response.UsuarioResponse;
import sv.edu.udb.hotel.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "1. Autenticación", description = "Inicio de sesión, registro de huéspedes y perfil")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Devuelve un token JWT que debe enviarse en el header Authorization: Bearer <token>")
    @ApiResponse(responseCode = "200", description = "Credenciales correctas")
    @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos")
    @ApiResponse(responseCode = "403", description = "Cuenta desactivada")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrarse como huésped", description = "Crea una cuenta con rol HUESPED e inicia sesión automáticamente")
    @ApiResponse(responseCode = "201", description = "Cuenta creada")
    @ApiResponse(responseCode = "409", description = "El correo ya está registrado")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @GetMapping("/perfil")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Datos del usuario que inició sesión")
    public UsuarioResponse perfil() {
        return authService.perfil();
    }
}
