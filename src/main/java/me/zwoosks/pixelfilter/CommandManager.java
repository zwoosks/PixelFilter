package me.zwoosks.pixelfilter;

import me.zwoosks.pixelfilter.utils.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandManager implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length >= 1) {
            /* todo hacer comando para iterar en todos los mapas del servidor y pixels no permitidos para ir
            *  regenerando la textura de mapas con imagen no permitida
            *  Hacer async para evitar lag.
            */
        } else {
            String helpMessage = Messages.colorizer("&2&lPixelFilter >> &r&aSistema de filtrado de PixelArts. Autor: zwoosks");
        }
        return true;
    }
}
