// Esperar a que el DOM esté completamente cargado
document.addEventListener("DOMContentLoaded", function () {
  // Seleccionar el mensaje de error
  var errorMessage = document.getElementById("errorMessage");

  // Si el mensaje de error existe, configurar un temporizador para ocultarlo
  if (errorMessage) {
    setTimeout(function () {
      // Añadir clase de animación de salida
      errorMessage.classList.add("animate__backOutUp");

      // Esperar a que termine la animación antes de ocultar el elemento
      errorMessage.addEventListener("animationend", function () {
        errorMessage.style.display = "none";
      });
    }, 3000); // 5000 milisegundos = 5 segundos
  }
});
