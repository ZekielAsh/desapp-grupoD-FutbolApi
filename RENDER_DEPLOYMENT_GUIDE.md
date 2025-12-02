# 🚀 Guía de Deployment en Render con Docker

## 📋 Pre-requisitos

- [ ] Cuenta en [Render](https://render.com)
- [ ] Código en GitHub/GitLab
- [ ] Dockerfile configurado ✅
- [ ] Variables de entorno preparadas ✅

## 🔧 Paso 1: Preparar el Repositorio

### 1.1 Verificar archivos necesarios

Asegúrate de tener estos archivos en tu repositorio:

```
tu-proyecto/
├── Dockerfile               ✅ Ya creado
├── .dockerignore           ✅ Ya creado
├── render.yaml             ✅ Ya creado
├── src/
│   └── main/
│       └── resources/
│           ├── application.properties      ✅ Configurado
│           └── application-prod.yml        ✅ Ya creado
├── build.gradle.kts
└── gradlew
```

### 1.2 Commitear y pushear cambios

```bash
git add .
git commit -m "feat: Add Render deployment configuration with Docker"
git push origin main
```

## 🌐 Paso 2: Crear Base de Datos en Render

### 2.1 Crear PostgreSQL Database

1. Ve a [Render Dashboard](https://dashboard.render.com)
2. Click en **"New +"** → **"PostgreSQL"**
3. Configura:
   - **Name**: `futbol-db`
   - **Database**: `futboldb`
   - **User**: `futboluser`
   - **Region**: Elige el más cercano (Oregon, Frankfurt, Singapore)
   - **Plan**: **Free**
4. Click **"Create Database"**

### 2.2 Obtener credenciales

Después de crear, verás:
- **Internal Database URL**: Para uso interno de Render
- **External Database URL**: Para conexiones externas
- **Connection String**: Usa este para `DATABASE_URL`

Ejemplo:
```
postgres://futboluser:password@dpg-xxxxx.oregon-postgres.render.com/futboldb
```

## 🐳 Paso 3: Crear Web Service en Render

### 3.1 Crear servicio

1. Click **"New +"** → **"Web Service"**
2. Conecta tu repositorio de GitHub/GitLab
3. Configura:

#### Basic Settings
- **Name**: `futbol-api` (o el nombre que prefieras)
- **Region**: Mismo que la base de datos
- **Branch**: `main`
- **Root Directory**: `.` (raíz del proyecto)
- **Environment**: **Docker**
- **Plan**: **Free**

#### Build Settings (Docker detectará automáticamente)
- **Dockerfile Path**: `./Dockerfile`
- **Docker Command**: (Render lo detecta del ENTRYPOINT)

### 3.2 Configurar Variables de Entorno

En la sección **"Environment Variables"**, agrega:

#### Variables Obligatorias

| Key | Value | Notas |
|-----|-------|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Activa perfil de producción |
| `PORT` | `8080` | Puerto interno |
| `JWT_SECRET` | `[GENERAR SECRETO]` | Ver instrucciones abajo |
| `JWT_EXPIRATION` | `86400000` | 24 horas en ms |
| `DATABASE_URL` | `[CONNECTION STRING]` | De la BD creada |
| `DATABASE_USERNAME` | `futboluser` | Usuario de PostgreSQL |
| `DATABASE_PASSWORD` | `[PASSWORD]` | De la BD creada |
| `DATABASE_DRIVER` | `org.postgresql.Driver` | Driver PostgreSQL |
| `DDL_AUTO` | `validate` | ⚠️ Usar `update` solo primera vez |

#### Generar JWT_SECRET seguro

```bash
# Opción 1: Con OpenSSL
openssl rand -base64 64

# Opción 2: Con Python
python -c "import secrets; print(secrets.token_urlsafe(64))"

# Opción 3: Online
# Usar: https://www.grc.com/passwords.htm
```

Ejemplo de secreto generado:
```
Xy9Kp2Mn8Qw3Rt5Yu7Io0Pl4Km6Jn2Bv9Cx1Za3Sw5De8Fg7Hj4Kl6Po9Iu8Yt3Qw2Er
```

### 3.3 Configurar Health Check

En **"Advanced"**:
- **Health Check Path**: `/actuator/health`
- **Auto-Deploy**: `Yes` (opcional)

### 3.4 Deploy

Click **"Create Web Service"**

Render comenzará a:
1. ✅ Clonar el repositorio
2. ✅ Construir la imagen Docker
3. ✅ Ejecutar el contenedor
4. ✅ Health check

## 📊 Paso 4: Verificar el Deploy

### 4.1 Ver Logs

En el dashboard de Render:
- Click en tu servicio
- Pestaña **"Logs"**

Deberías ver:
```
Started Application in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

### 4.2 Probar Health Check

```bash
curl https://tu-app.onrender.com/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP"
}
```

### 4.3 Probar Registro de Usuario

```bash
curl -X POST https://tu-app.onrender.com/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Respuesta esperada:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 4.4 Probar Login

```bash
curl -X POST https://tu-app.onrender.com/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### 4.5 Probar Endpoint Protegido

```bash
# Guardar token
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# Hacer petición autenticada
curl https://tu-app.onrender.com/teams/86/players \
  -H "Authorization: Bearer $TOKEN"
```

## 🔄 Paso 5: Configurar Auto-Deploy (Opcional)

### 5.1 Desde Render Dashboard

1. Ve a tu servicio
2. **Settings** → **Build & Deploy**
3. Activa **"Auto-Deploy"**

Ahora cada push a `main` deployará automáticamente.

### 5.2 Deploy Manual

```bash
# Desde tu repositorio local
git add .
git commit -m "Update feature"
git push origin main
```

Render detectará el cambio y redesplegará.

## 🐛 Troubleshooting

### Error: "Build failed"

**Causa**: Falta algún archivo o dependencia

**Solución**:
```bash
# Probar build localmente
docker build -t futbol-api .
docker run -p 8080:8080 futbol-api
```

### Error: "Health check failed"

**Causa**: La aplicación no arranca correctamente

**Solución**:
1. Revisar logs en Render
2. Verificar variables de entorno
3. Verificar conexión a base de datos

```bash
# En logs buscar:
DATABASE_URL=postgres://...  # Debe estar presente
```

### Error: "Database connection refused"

**Causa**: Credenciales incorrectas o BD no accesible

**Solución**:
1. Verificar `DATABASE_URL` en variables de entorno
2. Usar el **Internal Database URL** (no External)
3. Verificar que la BD esté en estado "Available"

### Error: "JWT signature verification failed"

**Causa**: `JWT_SECRET` cambió o no está configurado

**Solución**:
1. Verificar que `JWT_SECRET` esté en variables de entorno
2. Generar nuevo token después de cambiar secreto
3. No cambiar `JWT_SECRET` en producción sin migración

### Error: "Port already in use"

**Causa**: Conflicto de puertos

**Solución**:
Render maneja esto automáticamente. Si ocurre localmente:
```bash
# Cambiar puerto local
export PORT=8081
./gradlew bootRun
```

## 📈 Paso 6: Monitoreo

### 6.1 Métricas en Render

Render provee automáticamente:
- CPU Usage
- Memory Usage
- Request Latency
- HTTP Status Codes

### 6.2 Logs de Auditoría

Los logs están en:
- **Render Logs**: Dashboard → Logs
- **Archivo**: `/app/logs/audit.log` (dentro del contenedor)

Para acceder:
```bash
# Desde Render Shell (si disponible en tu plan)
cat /app/logs/audit.log | tail -n 100
```

### 6.3 Prometheus Metrics

Acceder a:
```
https://tu-app.onrender.com/actuator/prometheus
```

## 🔒 Seguridad

### Variables de Entorno Sensibles

✅ **Hacer**:
- Usar variables de entorno para secretos
- Generar `JWT_SECRET` largo y aleatorio
- Usar HTTPS (Render lo provee gratis)
- Configurar CORS correctamente

❌ **No hacer**:
- Hardcodear secretos en código
- Compartir `JWT_SECRET`
- Usar secretos débiles
- Exponer endpoints sin autenticación

### CORS en Producción

Actualizar en `application-prod.yml`:
```yaml
cors:
  allowed-origins: https://tu-frontend.com,https://tu-dominio.com
```

## 📝 Checklist de Deployment

### Pre-Deploy
- [ ] Dockerfile creado y probado
- [ ] Variables de entorno configuradas
- [ ] Base de datos creada en Render
- [ ] `JWT_SECRET` generado
- [ ] Código pusheado a GitHub/GitLab

### Durante Deploy
- [ ] Web Service creado en Render
- [ ] Variables de entorno agregadas
- [ ] Health check configurado
- [ ] Build exitoso
- [ ] Container ejecutándose

### Post-Deploy
- [ ] Health check pasa
- [ ] Registro de usuario funciona
- [ ] Login funciona
- [ ] Token JWT funciona
- [ ] Endpoints protegidos requieren auth
- [ ] Logs de auditoría funcionan
- [ ] Base de datos accesible

## 🎯 URLs Importantes

Después del deploy, tendrás:

| Servicio | URL |
|----------|-----|
| **API Base** | `https://futbol-api.onrender.com` |
| **Health Check** | `https://futbol-api.onrender.com/actuator/health` |
| **Swagger UI** | `https://futbol-api.onrender.com/swagger-ui.html` |
| **API Docs** | `https://futbol-api.onrender.com/v3/api-docs` |
| **Metrics** | `https://futbol-api.onrender.com/actuator/metrics` |
| **Prometheus** | `https://futbol-api.onrender.com/actuator/prometheus` |
| **Audit Logs** | `https://futbol-api.onrender.com/api/audit/logs` |

## 🔄 Actualizar el Deployment

```bash
# 1. Hacer cambios en código
git add .
git commit -m "Update: descripción del cambio"

# 2. Push a GitHub
git push origin main

# 3. Render auto-despliega (si está activado)
# O hacer deploy manual desde dashboard
```

## 💰 Costos

**Plan Free de Render**:
- ✅ 750 horas/mes gratis
- ✅ PostgreSQL: 1GB storage
- ✅ 100GB bandwidth/mes
- ⚠️ Servicio se duerme después de 15 min de inactividad
- ⚠️ Primera request puede tardar 30-60s (cold start)

**Para producción real**:
- Considerar plan **Starter** ($7/mes)
- Sin cold starts
- Más recursos

## 📞 Soporte

- [Render Docs](https://render.com/docs)
- [Render Status](https://status.render.com)
- [Render Community](https://community.render.com)

---

**Fecha**: 2025-12-02  
**Versión**: 1.0  
**Estado**: ✅ Listo para Deploy

