package com.minelittlepony.smyp;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import nl.enjarai.showmeyourskin.ShowMeYourSkinClient;
import nl.enjarai.showmeyourskin.config.ArmorConfig;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.api.model.armour.ArmourRendererPlugin;
import com.mojang.blaze3d.vertex.PoseStack;

public class Main implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ArmourRendererPlugin.register(PluginImpl::new);
    }

    static final class PluginImpl implements ArmourRendererPlugin {
        @Nullable
        private ArmorConfig config;

        private final ArmourRendererPlugin parent;

        PluginImpl(ArmourRendererPlugin parent) {
            this.parent = parent;
        }

        @Nullable
        private ArmorConfig.PieceConfig getPieceConfig(EquipmentSlot armorSlot) {
            return config == null ? ArmorConfig.PieceConfig.VANILLA_VALUES : switch (armorSlot) {
                case HEAD -> config.head;
                case FEET -> config.feet;
                case BODY, CHEST -> config.chest;
                case LEGS -> config.legs;
                case MAINHAND, OFFHAND, SADDLE -> ArmorConfig.PieceConfig.VANILLA_VALUES;
                default -> ArmorConfig.PieceConfig.VANILLA_VALUES;
            };
        }

        private ArmorConfig.PieceConfig getElytraConfig() {
            return config == null ? ArmorConfig.PieceConfig.VANILLA_VALUES : config.elytra;
        }

        @Override
        public ItemStack[] getArmorStacks(HumanoidRenderState state, EquipmentSlot armorSlot, EquipmentClientInfo.LayerType layerType, ArmourType type) {
            config = state.getData(ShowMeYourSkinClient.ARMOR_CONFIG_KEY);
            return parent.getArmorStacks(state, armorSlot, layerType, type);
        }

        @Override
        public void onArmourRendered(LivingEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, EquipmentSlot armorSlot, EquipmentClientInfo.LayerType layerType, ArmourType type) {
            parent.onArmourRendered(state, matrices, queue, armorSlot, layerType, type);
        }

        @Override
        public float getGlintAlpha(EquipmentSlot slot, ItemStack stack) {
            return getPieceConfig(slot).glint() ? parent.getGlintAlpha(slot, stack) : 0;
        }

        @Override
        public float getArmourAlpha(EquipmentSlot slot, EquipmentClientInfo.LayerType layerType) {
            return getPieceConfig(slot).base() ? parent.getArmourAlpha(slot, layerType) : 0;
        }

        @Override
        public float getTrimAlpha(EquipmentSlot slot, ArmorTrim trim, EquipmentClientInfo.LayerType layerType) {
            return getPieceConfig(slot).trim() ? parent.getTrimAlpha(slot, trim, layerType) : 0;
        }

        @Override
        public float getElytraAlpha(ItemStack stack, Model<?> model, LivingEntityRenderState state) {
            return getElytraConfig().base() ? parent.getElytraAlpha(stack, model, state) : 0;
        }

        @Override
        @Nullable
        public RenderType getArmourLayer(EquipmentSlot slot, Identifier texture, EquipmentClientInfo.LayerType layerType) {
            return parent.getArmourLayer(slot, texture, layerType);
        }

        @Override
        @Nullable
        public RenderType getTrimLayer(EquipmentSlot slot, ArmorTrim trim, EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> assetId) {
            return parent.getTrimLayer(slot, trim, layerType, assetId);
        }

        @Override
        @Nullable
        public RenderType getCapeLayer(HumanoidRenderState state, Identifier texture) {
            return getElytraConfig().base() ? parent.getCapeLayer(state, texture) : null;
        }
    }
}
