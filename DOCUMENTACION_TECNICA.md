# Documentación Técnica - WhiteDot

## 1. Resumen del Proyecto
WhiteDot es una aplicación de juego tipo "Clicker" para Android que permite a los usuarios ganar puntos mediante clics manuales o automáticos. El sistema incluye sincronización offline para que los usuarios sigan progresando mientras la aplicación está cerrada.

---

## 2. Arquitectura del Sistema
El proyecto se divide en dos componentes principales:
*   **Frontend (Android):** Desarrollado en Kotlin con Jetpack Compose, siguiendo el patrón MVVM.
*   **Backend (API):** Desarrollado en FastAPI (Python) con SQLAlchemy para la gestión de la base de datos MySQL.

---

## 3. Tecnologías Utilizadas

### Android
*   **UI:** Jetpack Compose (Diseño reactivo y moderno).
*   **Navegación:** Compose Navigation.
*   **Gestión de Estado:** ViewModel + Kotlin Coroutines + StateFlow.
*   **Persistencia Local:** Room Database (almacena el estado del juego y la sesión del usuario).
*   **Networking:** Retrofit + Gson (comunicación con la API de FastAPI).
*   **Sensores:** SensorManager (uso del acelerómetro para activar auto-click mediante sacudidas).
*   **Audio:** SoundPool (efectos de sonido de baja latencia) y MediaPlayer (música de fondo).


---

## 4. Flujo de Sincronización Offline
La característica principal del proyecto es la consistencia de datos entre el dispositivo y el servidor.

1.  **Persistencia de Sesión:** Al iniciar la app, se carga el `username` desde Room. Si existe, se asume sesión activa.
2.  **Cálculo de Ganancias:** Al loguear o iniciar la app, se consulta el endpoint `/users/{username}/last-logout`.
3.  **Lógica:** 
    *   `tiempo_offline = hora_actual - last_logout`
    *   `puntos_ganados = tiempo_offline (s) * clicks_por_segundo * auto_multiplier`
4.  **Sincronización:** Cada transacción importante (compra de mejoras, incremento de puntos, inicio de auto-click) dispara una llamada al endpoint `/sync` para actualizar:
    *   Puntos totales.
    *   Multiplicadores.
    *   Fecha de última actividad (`last_logout`).

---

## 5. Endpoints de la API
*   `POST /register`: Registro de nuevos usuarios.
*   `POST /login`: Validación de credenciales y retorno del estado actual del usuario.
*   `POST /sync`: Sincronización masiva de puntos y metadatos.
*   `GET /users/{username}/last-logout`: Recupera la última vez que el usuario estuvo activo para el cálculo offline.

---

## 6. Consideraciones de Implementación
*   **Formato de Fechas:** Se utiliza el patrón `yyyy-MM-dd'T'HH:mm:ss` para garantizar la compatibilidad entre el parser de Python (ISO 8601) y Android.
*   **Tratamiento de Timestamps:** Dado que el backend puede devolver milisegundos, el frontend realiza un filtrado (`substringBefore(".")`) antes del parseo para evitar errores de formato.
*   **Thread Safety:** Todas las operaciones de red y base de datos en Android se ejecutan en el `viewModelScope` utilizando `Dispatchers.IO`.

---

## 7. Configuración del Entorno
*   **Backend:** Requiere un entorno virtual (`source .venv/bin/activate`) y una instancia de MySQL configurada en `DATABASE_URL`.
*   **Android:** Requiere conexión a la misma red que el servidor o una URL accesible públicamente para Retrofit.
