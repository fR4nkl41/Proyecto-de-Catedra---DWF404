package sv.edu.udb.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.hotel.dto.request.LoginRequest;
import sv.edu.udb.hotel.dto.request.RegistroRequest;
import sv.edu.udb.hotel.dto.response.AuthResponse;
import sv.edu.udb.hotel.dto.response.UsuarioResponse;
import sv.edu.udb.hotel.exception.ReglaNegocioException;
import sv.edu.udb.hotel.model.Usuario;
import sv.edu.udb.hotel.model.enums.Rol;
import sv.edu.udb.hotel.repository.UsuarioRepository;
import sv.edu.udb.hotel.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        // Lanza BadCredentialsException o DisabledException si algo falla
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
        return construirRespuesta(usuario, "Bienvenido, " + usuario.getNombre());
    }

    /** Registro público: siempre crea cuentas con rol HUESPED. */
    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw ReglaNegocioException.conflicto("Ya existe una cuenta registrada con el correo " + email + ".");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setRol(Rol.HUESPED);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        return construirRespuesta(usuario, "Cuenta creada correctamente. ¡Bienvenido a Añil!");
    }

    @Transactional(readOnly = true)
    public UsuarioResponse perfil() {
        return UsuarioResponse.from(getUsuarioActual());
    }

    /** Devuelve el usuario que hizo la petición (según el token JWT). */
    @Transactional(readOnly = true)
    public Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw ReglaNegocioException.prohibido("Debes iniciar sesión para realizar esta acción.");
        }
        return usuarioRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> ReglaNegocioException.prohibido("El usuario de la sesión ya no existe."));
    }

    /** true si el usuario es parte del personal del hotel (ADMIN o RECEPCIONISTA). */
    public static boolean esPersonal(Usuario usuario) {
        return usuario.getRol() == Rol.ADMIN || usuario.getRol() == Rol.RECEPCIONISTA;
    }

    private AuthResponse construirRespuesta(Usuario usuario, String mensaje) {
        String token = jwtService.generarToken(usuario);
        return new AuthResponse(token, "Bearer", jwtService.getExpiracionSegundos(),
                UsuarioResponse.from(usuario), mensaje);
    }
}
