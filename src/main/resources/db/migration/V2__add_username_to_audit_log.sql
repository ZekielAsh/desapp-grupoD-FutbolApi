-- Migration script para agregar columna username a api_audit_log
-- Fecha: 2025-12-01
-- Descripción: Agrega el campo username para registrar el usuario autenticado en cada petición

-- Agregar columna username con valor por defecto 'anonymous'
ALTER TABLE api_audit_log
ADD COLUMN IF NOT EXISTS username VARCHAR(255) NOT NULL DEFAULT 'anonymous';

-- Crear índice para mejorar consultas por usuario
CREATE INDEX IF NOT EXISTS idx_api_audit_log_username
ON api_audit_log(username);

-- Crear índice compuesto para consultas de actividad por usuario y fecha
CREATE INDEX IF NOT EXISTS idx_api_audit_log_username_timestamp
ON api_audit_log(username, timestamp DESC);

-- Crear índice para consultas por path
CREATE INDEX IF NOT EXISTS idx_api_audit_log_path
ON api_audit_log(path);

-- Crear índice para consultas de errores
CREATE INDEX IF NOT EXISTS idx_api_audit_log_was_success
ON api_audit_log(was_success, timestamp DESC);

-- Comentarios en las columnas (PostgreSQL)
COMMENT ON COLUMN api_audit_log.username IS 'Usuario autenticado que realizó la petición. "anonymous" si no hay sesión activa.';
COMMENT ON COLUMN api_audit_log.timestamp IS 'Fecha y hora de la petición al endpoint';
COMMENT ON COLUMN api_audit_log.execution_time_ms IS 'Tiempo de ejecución en milisegundos';
COMMENT ON COLUMN api_audit_log.was_success IS 'Indica si la petición fue exitosa (true) o falló (false)';

