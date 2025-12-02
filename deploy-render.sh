#!/bin/bash

echo "🚀 Quick Start - Deploy a Render"
echo "=================================="
echo ""

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Paso 1: Verificando archivos...${NC}"
if [ -f "Dockerfile" ]; then
    echo -e "${GREEN}✓${NC} Dockerfile encontrado"
else
    echo "✗ Dockerfile NO encontrado"
    exit 1
fi

if [ -f "render.yaml" ]; then
    echo -e "${GREEN}✓${NC} render.yaml encontrado"
else
    echo "✗ render.yaml NO encontrado"
    exit 1
fi

echo ""
echo -e "${YELLOW}Paso 2: Generando JWT Secret...${NC}"
JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
echo -e "${GREEN}JWT_SECRET generado:${NC}"
echo "$JWT_SECRET"
echo ""
echo "⚠️  GUARDA ESTE SECRET - Lo necesitarás en Render"
echo ""

echo -e "${YELLOW}Paso 3: Test local con Docker...${NC}"
read -p "¿Quieres probar el build localmente? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Construyendo imagen Docker..."
    docker build -t futbol-api-test .

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓${NC} Build exitoso"
        echo ""
        read -p "¿Ejecutar container? (y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "Ejecutando container en puerto 8080..."
            docker run -p 8080:8080 \
                -e JWT_SECRET="$JWT_SECRET" \
                -e SPRING_PROFILES_ACTIVE=prod \
                futbol-api-test
        fi
    else
        echo "✗ Build falló - revisa los errores"
        exit 1
    fi
fi

echo ""
echo -e "${YELLOW}Paso 4: Commit y Push${NC}"
echo "Ejecuta estos comandos:"
echo ""
echo "  git add ."
echo "  git commit -m 'feat: Add Render deployment configuration'"
echo "  git push origin main"
echo ""

echo -e "${YELLOW}Paso 5: Configurar en Render${NC}"
echo ""
echo "1. Ve a https://dashboard.render.com"
echo "2. Crea PostgreSQL Database:"
echo "   - Name: futbol-db"
echo "   - Region: Oregon (o el más cercano)"
echo ""
echo "3. Crea Web Service:"
echo "   - Conecta tu repo de GitHub/GitLab"
echo "   - Environment: Docker"
echo "   - Agrega estas variables:"
echo ""
echo "   SPRING_PROFILES_ACTIVE=prod"
echo "   PORT=8080"
echo "   JWT_SECRET=$JWT_SECRET"
echo "   JWT_EXPIRATION=86400000"
echo "   DATABASE_URL=[copiar de Render PostgreSQL]"
echo "   DATABASE_USERNAME=futboluser"
echo "   DATABASE_PASSWORD=[copiar de Render PostgreSQL]"
echo "   DATABASE_DRIVER=org.postgresql.Driver"
echo "   DDL_AUTO=update"
echo ""
echo "4. Health Check Path: /actuator/health"
echo ""
echo "5. Click 'Create Web Service'"
echo ""
echo -e "${GREEN}¡Listo! Tu API estará disponible en unos minutos${NC}"
echo ""
echo "📚 Lee RENDER_DEPLOYMENT_GUIDE.md para más detalles"

