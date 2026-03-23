// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

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
                        .then(Commands.literal("addxp")
                                .then(Commands.argument("amount",
                                                com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 1000))
                                        .executes(WerewolfCommand::addXp)))
                        .then(Commands.literal("stats")
                                .executes(WerewolfCommand::showStats))
                        .then(Commands.literal("upgrade")
                                .then(Commands.argument("attribute",
                                                com.mojang.brigadier.arguments.StringArgumentType.word())
                                        .executes(WerewolfCommand::upgradeAttribute)))
        );
    }

    private static int transform(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);

            if (!cap.isWerewolf()) {
                ctx.getSource().sendFailure(Component.literal("You are not a werewolf!"));
                return 0;
            }

            if (cap.isTransformed()) {
                long dayTime = player.level().getDayTime() % 24000;
                boolean isNight = dayTime >= 13000 && dayTime <= 23000;
                boolean isFullMoon = player.level().getMoonPhase() == 0;
                if (isNight && isFullMoon) {
                    ctx.getSource().sendFailure(Component.literal(
                            "§c☾ The full moon controls you... you cannot resist the transformation!"));
                    return 0;
                }
            }

            boolean nowTransformed = !cap.isTransformed();
            cap.setTransformed(nowTransformed);
            syncToClient(player, cap);

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
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
            cap.setWerewolf(true);
            syncToClient(player, cap);
            ctx.getSource().sendSuccess(() -> Component.literal("§aYou are now a werewolf!"), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            return 0;
        }
    }

    private static int addXp(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);

            if (!cap.isWerewolf()) {
                ctx.getSource().sendFailure(Component.literal("You are not a werewolf!"));
                return 0;
            }
            if (!cap.isTransformed()) {
                ctx.getSource().sendFailure(Component.literal("You must be transformed to gain XP!"));
                return 0;
            }

            int amount = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "amount");
            int levelBefore = cap.getLevel();
            cap.addExperience(amount);
            int levelAfter = cap.getLevel();

            syncToClient(player, cap);

            if (levelAfter > levelBefore) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                        "§6Level up! Now level " + levelAfter), false);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal(
                        "§aAdded " + amount + " XP. (" + cap.getExperience()
                                + "/" + cap.expNeededForNextLevel() + ")"), false);
            }
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            return 0;
        }
    }

    private static int showStats(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);

            ctx.getSource().sendSuccess(() -> Component.literal(
                    "§6--- Werewolf Stats ---\n" +
                            "§eLevel: §f" + cap.getLevel() + "/20\n" +
                            "§eXP: §f" + cap.getExperience() + "/" + cap.expNeededForNextLevel() + "\n" +
                             "§eAttribute Points: §f" + cap.getAvailableAttributePoints() + "\n" +
                             "§eAbility Points: §f" + cap.getAvailableAbilityPoints() + "\n" +
                             "§eTransformed: §f" + cap.isTransformed()
            ), false);

            for (WereAttribute attr : WereAttribute.values()) {
                int attrLevel = cap.getAttributeLevel(attr);
                if (attrLevel > 0) {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "§7" + attr.getKey() + ": §f" + attrLevel + "/" + attr.getMaxLevel()
                    ), false);
                }
            }

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            return 0;
        }
    }

    private static int upgradeAttribute(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);

            if (!cap.isWerewolf()) {
                ctx.getSource().sendFailure(Component.literal("You are not a werewolf!"));
                return 0;
            }

            String key = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "attribute");

            WereAttribute found = null;
            for (WereAttribute attr : WereAttribute.values()) {
                if (attr.name().equalsIgnoreCase(key)) {
                    found = attr;
                    break;
                }
            }

            if (found == null) {
                ctx.getSource().sendFailure(Component.literal(
                        "§cUnknown attribute: " + key + "\n§7Valid: " +
                                java.util.Arrays.stream(WereAttribute.values())
                                        .map(WereAttribute::name)
                                        .collect(java.util.stream.Collectors.joining(", "))
                ));
                return 0;
            }

            if (cap.getAvailableAttributePoints() <= 0) {
                ctx.getSource().sendFailure(Component.literal("§cNo attribute points available!"));
                return 0;
            }

            if (!cap.canUpgradeAttribute(found)) {
                ctx.getSource().sendFailure(Component.literal(
                        "§c" + found.name() + " is already at max level (" + found.getMaxLevel() + ")!"
                ));
                return 0;
            }

            cap.upgradeAttribute(found);
            syncToClient(player, cap);

            if (cap.isTransformed()) {
                WerewolfAttributeHandler.applyAllModifiers(player, cap);
            }

            WereAttribute finalFound = found;
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a" + finalFound.name() + " upgraded to level "
                            + cap.getAttributeLevel(finalFound) + "/" + finalFound.getMaxLevel()
                            + " §7(" + cap.getAvailableAttributePoints() + " points remaining)"
            ), false);

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            return 0;
        }
    }

    public static void syncToClient(ServerPlayer player, WerewolfCapability cap) {
        PacketDistributor.sendToPlayer(player, SyncWerewolfPacket.fromCap(cap));
    }
}