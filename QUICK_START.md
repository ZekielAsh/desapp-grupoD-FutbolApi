# 🚀 Quick Start Guide - Nuevos Endpoints

## Paso 1: Iniciar la Aplicación

```bash
gradlew bootRun
```

Espera a que veas: `Started DemoApplicationKt in X seconds`

---

## Paso 2: Obtener Token JWT

### Opción A: Con curl
```bash
curl -X POST http://localhost:8080/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"tu_usuario\",\"password\":\"tu_password\"}"
```

### Opción B: Con Postman
```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "tu_usuario",
  "password": "tu_password"
}
```

**Guarda el token** que recibes en la respuesta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## Paso 3: Probar los Nuevos Endpoints

### 🔍 Comparación de Equipos

**Real Madrid (86) vs FC Barcelona (81)**
```bash
curl -X GET "http://localhost:8080/teams/compare?team1=86&team2=81" ^
  -H "Authorization: Bearer TU_TOKEN_AQUI"
```

**Manchester City (65) vs Bayern Munich (5)**
```bash
curl -X GET "http://localhost:8080/teams/compare?team1=65&team2=5" ^
  -H "Authorization: Bearer TU_TOKEN_AQUI"
```

---

### 📊 Métricas Avanzadas de Equipos

**Real Madrid**
```bash
curl -X GET "http://localhost:8080/metrics/teams/86" ^
  -H "Authorization: Bearer TU_TOKEN_AQUI"
```

**FC Barcelona**
```bash
curl -X GET "http://localhost:8080/metrics/teams/81" ^
  -H "Authorization: Bearer TU_TOKEN_AQUI"
```

**Manchester City**
```bash
curl -X GET "http://localhost:8080/metrics/teams/65" ^
  -H "Authorization: Bearer TU_TOKEN_AQUI"
```

---

### 👤 Métricas Avanzadas de Jugadores

**Lionel Messi**
```bash
curl -X GET "http://localhost:8080/metrics/players/44/Lionel-Messi" ^
  -H "Authorization: Bearer TU_TOKEN_AQUI"
```

---

## 🧪 Probar con Swagger UI

1. Abre en tu navegador:
```
http://localhost:8080/swagger-ui.html
```

2. Click en el botón **"Authorize"** (🔒)

3. Ingresa tu token:
```
Bearer TU_TOKEN_AQUI
```

4. Ahora puedes probar todos los endpoints directamente desde Swagger

---

## 📋 IDs Útiles para Pruebas

### Equipos
| Equipo | ID |
|---|---|
| Real Madrid CF | 86 |
| FC Barcelona | 81 |
| Manchester City FC | 65 |
| Bayern Munich | 5 |
| Chelsea FC | 61 |
| Juventus FC | 109 |
| Paris Saint-Germain | 524 |
| Liverpool FC | 64 |

### Jugadores (WhoScored)
| Jugador | ID | Nombre URL |
|---|---|---|
| Lionel Messi | 44 | Lionel-Messi |

---

## ✅ Verificar que el Caché Funciona

### Primera Request (SIN caché)
```bash
# Mide el tiempo
curl -w "\nTime: %{time_total}s\n" \
  -H "Authorization: Bearer TOKEN" \
  "http://localhost:8080/teams/compare?team1=86&team2=81"
```

### Segunda Request (CON caché)
```bash
# Repite el mismo comando inmediatamente
curl -w "\nTime: %{time_total}s\n" \
  -H "Authorization: Bearer TOKEN" \
  "http://localhost:8080/teams/compare?team1=86&team2=81"
```

**Deberías ver una diferencia de velocidad significativa (80-95% más rápido)**

---

## 📊 Ver Métricas de Caché

```bash
# Ver todos los cachés
curl http://localhost:8080/actuator/caches

# Ver métricas
curl http://localhost:8080/actuator/metrics
```

---

## 🔧 Troubleshooting

### Error 401 Unauthorized
- ✅ Verifica que el token sea válido
- ✅ Asegúrate de incluir "Bearer " antes del token
- ✅ El token expira después de un tiempo, genera uno nuevo

### Error 400 Bad Request
- ✅ Verifica que los IDs existan
- ✅ Verifica los parámetros de la URL

### Error 500 Internal Server Error
- ✅ Revisa los logs de la aplicación
- ✅ Puede que la API externa esté caída
- ✅ Verifica que la base de datos esté corriendo

---

## 🎯 Comandos Windows (CMD)

Si estás usando CMD de Windows en lugar de PowerShell, usa `^` para continuar líneas:

```cmd
curl -X GET "http://localhost:8080/teams/compare?team1=86&team2=81" ^
  -H "Authorization: Bearer TU_TOKEN"
```

---

## 🎯 Comandos PowerShell

Si estás usando PowerShell, usa `` ` `` para continuar líneas:

```powershell
curl -X GET "http://localhost:8080/teams/compare?team1=86&team2=81" `
  -H "Authorization: Bearer TU_TOKEN"
```

---

## 🎨 Respuesta de Ejemplo

### Comparación de Equipos
```json
{
  "team1": {
    "id": 86,
    "name": "Real Madrid CF",
    "wins": 3,
    "draws": 1,
    "losses": 1,
    "goalsFor": 8,
    "goalsAgainst": 5,
    "points": 10,
    "position": 2,
    "form": "WWDLW"
  },
  "team2": {...},
  "headToHead": {
    "team1Wins": 2,
    "team2Wins": 1,
    "draws": 2,
    "totalMatches": 5,
    "lastMeetings": [...]
  }
}
```

---

**¡Listo para probar! 🚀**

