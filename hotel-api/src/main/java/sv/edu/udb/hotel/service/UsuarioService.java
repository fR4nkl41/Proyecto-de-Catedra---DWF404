package sv.edu.udb.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.hotel.dto.request.UsuarioRequest;
import sv.edu.udb.hotel.dto.response.UsuarioResponse;
import sv.edu.udb.hotel.exception.RecursoNoEncontradoException;
import sv.edu.udb.hotel.exception.ReglaNegocioException;
import sv.edu.udb.hotel.model.Usuario;
import sv.edu.udb.hotel.model.enums.Rol;
import sv.edu.udb.hotel.repository.UsuarioRepository;

import java.util.List;

/** Administración de usuarios (solo ADMIN). */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(Rol rol) {
        List<Usuario> usuarios = rol == null
                ? usuarioRepository.findAllByOrderByNombreAsc()
                : usuarioRepository.findByRolOrderByNombreAsc(rol);
        return usuarios.stream().map(UsuarioResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.from(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw ReglaNegocioException.conflicto("Ya existe un usuario con el correo " + email + ".");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new ReglaNegocioException("La contraseña es obligatoria al crear un usuario.");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setRol(request.rol());
        usuario.setActivo(request.activo() == null || request.activo());
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscar(id);
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw ReglaNegocioException.conflicto("El correo " + email + " ya está en uso por otro usuario.");
        }
        Usuario actual = authService.getUsuarioActual();
        if (actual.getId().equals(id) && request.rol() != Rol.ADMIN) {
            throw new ReglaNegocioException("No puedes quitarte el rol de administrador a ti mismo.");
        }
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setRol(request.rol());
        if (request.activo() != null) {
            validarNoEsElMismo(actual, id, request.activo());
            usuario.setActivo(request.activo());
        }
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return UsuarioResponse.from(usuario);
    }

    /** Activa o desactiva una cuenta. Un usuario inactivo no puede iniciar sesión. */
    @Transactional
    public UsuarioResponse cambiarEstado(Long id, boolean activo) {
        Usuario usuario = buscar(id);
        validarNoEsElMismo(authService.getUsuarioActual(), id, activo);
        usuario.setActivo(activo);
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = buscar(id);
        if (authService.getUsuarioActual().getId().equals(id)) {
            throw new ReglaNegocioException("No puedes eliminar tu propia cuenta.");
        }
        try {
            usuarioRepository.delete(usuario);
            usuarioRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw ReglaNegocioException.conflicto("No se puede eliminar el usuario porque tiene reservas o registros "
                    + "asociados. Puedes desactivarlo en su lugar.");
        }
    }

    private void validarNoEsElMismo(Usuario actual, Long id, boolean activo) {
        if (actual.getId().equals(id) && !activo) {
            throw new ReglaNegocioException("No puedes desactivar tu propia cuenta.");
        }
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el usuario", id));
    }
}
