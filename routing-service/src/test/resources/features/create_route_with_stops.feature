Feature: Crear Rutas con Paraderos
  Como Manager de una empresa de transporte
  Quiero poder registrar una nueva ruta que incluya una lista de paraderos
  Para que los conductores y pasajeros tengan el recorrido detallado

  Scenario: Crear una ruta exitosamente con 2 paraderos
    Given que soy un usuario autenticado con rol "MANAGER"
    When envío una petición para crear una ruta de "Los Olivos" a "San Miguel" con precio 2.50
    And incluyo 2 paraderos en la petición
    Then la ruta debe ser creada exitosamente con código de estado 201
    And la ruta devuelta debe tener 2 paraderos asignados

