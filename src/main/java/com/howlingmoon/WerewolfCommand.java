package com.howlingmoon;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = HowlingMoon.MODID)
public class WerewolfCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("werewolf")
                        .then(Commands.literal("transform")
                                .executes(WerewolfCommand::transform))
                        .then(Commands.literal("set")
                                .executes(WerewolfCommand::setWerewolf))
        );
    }

    private static int transform(CommandContext<CommandSourceStack> ctx) {
        try {
            Player player = ctx.getSource().getPlayerOrException();
            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);

            if (!cap.isWerewolf()) {
                ctx.getSource().sendFailure(Component.literal("You are not a werewolf!"));
                return 0;
            }

            boolean nowTransformed = !cap.isTransformed();
            cap.setTransformed(nowTransformed);

            if (nowTransformed) {
                ctx.getSource().sendSuccess(() -> Component.literal("§6The beast within takes hold!"), false);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal("§7You return to human form..."), false);
            }
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            return 0;
        }
    }

    private static int setWerewolf(CommandContext<CommandSourceStack> ctx) {
        try {
            Player player = ctx.getSource().getPlayerOrException();
            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
            cap.setWerewolf(true);
            ctx.getSource().sendSuccess(() -> Component.literal("§aYou are now a werewolf!"), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            return 0;
        }
    }
}