package org.cyclops.iconexporter.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.cyclops.iconexporter.GeneralConfig;
import org.cyclops.iconexporter.client.gui.ScreenIconExporter;

/**
 * A command to initiate the exporting process.
 * @author rubensworks
 *
 */
public class CommandExport implements Command<CommandSourceStack> {

    private final boolean param;

    public CommandExport(boolean param) {
        this.param = param;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        // Determine the scale
        int scale = GeneralConfig.defaultScale;
        if (param) {
            scale = context.getArgument("scale", Integer.class);
        }

        // Open the gui that will render the icons
        ScreenIconExporter exporter = new ScreenIconExporter(scale, Minecraft.getInstance().getWindow().getGuiScale());
        Minecraft.getInstance().submitAsync(() -> Minecraft.getInstance().setScreen(exporter));

        return 0;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        return Commands.literal("export")
                .executes(new CommandExport(false))
                .then(Commands.argument("scale", IntegerArgumentType.integer(1))
                        .executes(new CommandExport(true)));
    }

}
