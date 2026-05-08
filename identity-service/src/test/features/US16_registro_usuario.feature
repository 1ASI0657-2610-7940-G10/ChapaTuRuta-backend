Feature: Registro de usuario (US16)
  Como usuario de la plataforma
  Quiero registrarme proporcionando mis datos
  Para poder iniciar sesión en ChapaTuRuta

  Scenario: Registro exitoso de un nuevo conductor
    Given que soy un usuario nuevo con nombre "Héctor Rios", correo "hector@gmail.com" y rol "DRIVER"
    When envío mis datos de registro al sistema
    Then la cuenta se crea exitosamente
    And el sistema responde con el código de estado 201