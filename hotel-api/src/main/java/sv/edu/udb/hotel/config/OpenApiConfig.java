package sv.edu.udb.hotel.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la documentación OpenAPI / Swagger.
 * UI: http://localhost:8080/swagger-ui.html
 * Para probar endpoints protegidos: iniciar sesión en /api/auth/login, copiar el token
 * y pegarlo en el botón "Authorize".
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API REST - Sistema de Gestión de Reservas Hoteleras Añil",
                version = "1.0.0",
                description = "Proyecto de cátedra DWF404 - Universidad Don Bosco. Administración de hoteles, "
                        + "sucursales, habitaciones, huéspedes, reservas, pagos, cancelaciones y reclamos.",
                contact = @Contact(name = "Equipo DWF404")),
        servers = @Server(url = "http://localhost:8080", description = "Servidor local"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token obtenido en POST /api/auth/login")
public class OpenApiConfig {
}
