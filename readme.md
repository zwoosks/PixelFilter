# PixelFilter
##### Author: zwoosks

I've only added a Spanish description. English description and documentation to be added. Any contribution is appreciated!
## Descripción (ES)
Este plugin tiene como objetivo regular la creación de PixelArts. Su manera de trabajar, cuando se intenta crear un nuevo mapa es la siguiente:

Primero guarda el mapa creado y lo pasa a BufferedImage, para después hacer varias comparaciones y tomar acciones si es oportuno. Las comparaciones que hace son las siguientes:
(Nótese que en la configuración se pueden ajustar las tolerancias de similitud entre imágenes)
> #### ¿Está la imagen en la base de datos donde se almacenan los PixelArts prohibidos?
> Se procede a cancelar la creación del mapa y avisar al jugador de que el PixelArt contiene una imagen NO permitida.
> #### ¿Está la imagen en la base de datos donde se almacenan los PixelArts permitidos?
> El plugin no actúa, simplemente avisa al jugador de que el PixelArt que ha creado es permitido y no hace falta que tome más acciones.
> #### ¿Está la imagen en la base de datos donde se almacenan los PixelArts en estado de espera a ser revisados?
> Se procede a cancelar la creación del mapa y avisar al jugador de que el PixelArt ya ha sido enviado a revisión. Toca esperar.
> #### ¿El PixelArt no está en ninguna base de datos?
> Se procede a cancelar la creación del mapa y guardar la imagen en la base de datos de PixelArts pendientes de revisión. Se avisa al usuario de tal acción.

### Algunas características importantes
Hay ciertas características del plugin que vale la pena recalcar.
- Unas de ellas son las que impiden al usuario buggear el proceso de lectura en bases de datos u otro para quedarse con el mapa, sea suicidándose, dropeando el item, transfiriéndolo... Todo esto el plugin lo detecta y cancela el evento. Con el tema de desconexión mientras se procesa el código, el plugin quita el mapa generado cuando el usuario se reconecte.
- Otra es que todas las consultas a bases de datos son asíncronas, para así evitar lag en el thread principal.
- Mensajes configurables. También tiempos de espera entre interacciones.
- Timeouts entre creación/submits de PixelArts para evitar consumo excesivo de recursos de X minutos configurables.
- Permiso para bypassear el filtro de PixelArts: *pf.bypass*
- Etc.

### ¡¡¡Nota importante!!!
El sistema para pasar imagenes de la base de datos de PixelArts pendientes de verificación a la base de datos de PixelArts permitidos o no permitidos no está hecho. Opción a desarrollarlo en formato web, aplicación de escritorio o bot de Discord.

### TO-DO
- Hacer comando para iterar en todos los mapas del servidor y pixels no permitidos de la base de datos para ir regenerando la textura de mapas con imagen no permitida a una por ejemplo en blanco o con texto informativo. Hacer async para evitar lag.
- Hacer otro comando que regenere textura de un solo mapa dando su ID, cambiando la textura a totalmente blanca o con texto informativo. Hacer async.