package com.howlingmoon;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class WerewolfAttachment {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, HowlingMoon.MODID);

    public static final Supplier<AttachmentType<WerewolfCapability>> WEREWOLF_DATA =
            ATTACHMENT_TYPES.register("werewolf_data", () ->
                    AttachmentType.builder(WerewolfCapability::new)
                            .serialize(new net.neoforged.neoforge.attachment.IAttachmentSerializer<CompoundTag, WerewolfCapability>() {
                                @Override
                                public WerewolfCapability read(net.neoforged.neoforge.attachment.IAttachmentHolder holder, CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
                                    WerewolfCapability cap = new WerewolfCapability();
                                    cap.setWerewolf(tag.getBoolean("isWerewolf"));
                                    cap.setTransformed(tag.getBoolean("isTransformed"));
                                    cap.setLevel(tag.getInt("level"));
                                    cap.setExperience(tag.getInt("experience"));
                                    return cap;
                                }

                                @Override
                                public CompoundTag write(WerewolfCapability cap, net.minecraft.core.HolderLookup.Provider provider) {
                                    CompoundTag tag = new CompoundTag();
                                    tag.putBoolean("isWerewolf", cap.isWerewolf());
                                    tag.putBoolean("isTransformed", cap.isTransformed());
                                    tag.putInt("level", cap.getLevel());
                                    tag.putInt("experience", cap.getExperience());
                                    return tag;
                                }
                            })
                            .build());
}