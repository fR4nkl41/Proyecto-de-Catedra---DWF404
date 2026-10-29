package sv.edu.udb.hotel.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sv.edu.udb.hotel.model.Usuario;
import sv.edu.udb.hotel.model.enums.Rol;
import sv.edu.udb.hotel.repository.UsuarioRepository;

/**
 * Si la tabla de usuarios está vacía (no se ejecutó 02_HotelDB_datos.sql),
 * crea los usuarios predefinidos para poder iniciar sesión.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        crear("Administrador", "admin@hotel.com", "Admin123", Rol.ADMIN);
        crear("María Rodríguez", "recepcion@hotel.com", "Recep123", Rol.RECEPCIONISTA);
        crear("Carlos Pérez", "carlos@mail.com", "Huesped123", Rol.HUESPED);
        log.info("Usuarios predefinidos creados (admin@hotel.com / Admin123).");
    }

    private void crear(String nombre, String email, String password, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }
}
