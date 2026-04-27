#!/bin/bash

# Script de Setup - Backend Bank
# Prepara el proyecto para ejecución local o Docker

set -e  # Exit on error

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Variables
PROJECT_NAME="Backend Bank"
GRADLE_CMD="./gradlew"
DOCKER_COMPOSE="docker-compose"

echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   ${PROJECT_NAME} - Setup Inicial    ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
echo ""

# Verificar requisitos
check_requirements() {
    echo -e "${YELLOW}Verificando requisitos...${NC}"

    # Java
    if ! command -v java &> /dev/null; then
        echo -e "${RED}✗ Java no encontrado. Por favor, instala Java 21+${NC}"
        exit 1
    fi
    JAVA_VERSION=$(java -version 2>&1 | grep -oP '"\K.*?(?=")' | cut -d. -f1)
    if [ "$JAVA_VERSION" -lt 21 ]; then
        echo -e "${RED}✗ Java 21+ requerido (encontrado: $JAVA_VERSION)${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Java $JAVA_VERSION encontrado${NC}"

    # Git
    if ! command -v git &> /dev/null; then
        echo -e "${RED}✗ Git no encontrado${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Git encontrado${NC}"

    # Docker (opcional)
    if ! command -v docker &> /dev/null; then
        echo -e "${YELLOW}⚠ Docker no encontrado (necesario para tests en contenedor)${NC}"
    else
        echo -e "${GREEN}✓ Docker encontrado${NC}"
    fi

    echo ""
}

# Configurar archivo .env
setup_env() {
    echo -e "${YELLOW}Configurando variables de entorno...${NC}"

    if [ ! -f .env ]; then
        if [ -f .env.example ]; then
            cp .env.example .env
            echo -e "${GREEN}✓ .env creado desde .env.example${NC}"
        else
            echo -e "${RED}✗ .env.example no encontrado${NC}"
            exit 1
        fi
    else
        echo -e "${GREEN}✓ .env ya existe${NC}"
    fi

    echo ""
}

# Limpiar y compilar
build_project() {
    echo -e "${YELLOW}Compilando proyecto...${NC}"

    if [ ! -f "$GRADLE_CMD" ]; then
        echo -e "${RED}✗ Gradle no encontrado${NC}"
        exit 1
    fi

    $GRADLE_CMD clean build -x test --no-daemon

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Proyecto compilado exitosamente${NC}"
    else
        echo -e "${RED}✗ Error al compilar proyecto${NC}"
        exit 1
    fi

    echo ""
}

# Levantar servicios Docker
start_docker() {
    echo -e "${YELLOW}Verificando Docker...${NC}"

    if ! command -v $DOCKER_COMPOSE &> /dev/null; then
        echo -e "${YELLOW}⚠ Docker Compose no disponible${NC}"
        echo -e "${YELLOW}Instálalo desde: https://docs.docker.com/compose/install/${NC}"
        return 1
    fi

    if [ ! -f "docker-compose.yml" ]; then
        echo -e "${RED}✗ docker-compose.yml no encontrado${NC}"
        return 1
    fi

    echo -e "${YELLOW}Levantando servicios Docker (MySQL)...${NC}"
    $DOCKER_COMPOSE up -d mysql

    # Esperar a que MySQL esté healthy
    echo -e "${YELLOW}Esperando a que MySQL esté listo...${NC}"
    for i in {1..30}; do
        if $DOCKER_COMPOSE exec -T mysql mysqladmin ping -ubackend_user -pbackend_pass &> /dev/null; then
            echo -e "${GREEN}✓ MySQL está listo${NC}"
            return 0
        fi
        echo -n "."
        sleep 1
    done

    echo -e "${RED}✗ MySQL no respondió después de 30 segundos${NC}"
    return 1
}

# Mostrar próximos pasos
show_next_steps() {
    echo ""
    echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║        Próximos Pasos                  ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${GREEN}✓ Setup completado${NC}"
    echo ""
    echo "Opciones:"
    echo ""
    echo "1. ${YELLOW}Ejecutar localmente:${NC}"
    echo "   $GRADLE_CMD bootRun --args='--spring.profiles.active=local'"
    echo ""
    echo "2. ${YELLOW}Ejecutar en Docker:${NC}"
    echo "   $DOCKER_COMPOSE up -d"
    echo ""
    echo "3. ${YELLOW}Ejecutar tests:${NC}"
    echo "   $GRADLE_CMD test"
    echo ""
    echo "4. ${YELLOW}Abrir en navegador:${NC}"
    echo "   http://localhost:8080/swagger-ui/index.html"
    echo ""
    echo -e "Documentación: Ver ${YELLOW}README.md${NC}, ${YELLOW}DOCKER.md${NC}, ${YELLOW}TESTING.md${NC}"
    echo ""
}

# Menú principal
show_menu() {
    echo ""
    echo -e "${BLUE}Selecciona una opción:${NC}"
    echo "1) Verificar requisitos (solo check)"
    echo "2) Setup completo (build + Docker)"
    echo "3) Build solamente"
    echo "4) Docker solamente"
    echo "5) Salir"
    echo ""
    read -p "Opción (1-5): " option
}

# Main
main() {
    check_requirements

    show_menu

    case $option in
        1)
            echo -e "${GREEN}✓ Todos los requisitos están OK${NC}"
            ;;
        2)
            setup_env
            build_project
            start_docker
            show_next_steps
            ;;
        3)
            setup_env
            build_project
            ;;
        4)
            setup_env
            start_docker
            ;;
        5)
            echo "Adiós!"
            exit 0
            ;;
        *)
            echo -e "${RED}Opción inválida${NC}"
            exit 1
            ;;
    esac
}

# Ejecutar
main

