package com.minelittlepony.smyp;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import nl.enjarai.showmeyourskin.ShowMeYourSkinClient;
import nl.enjarai.showmeyourskin.config.ArmorConfig;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.client.model.armour.ArmourRendererPlugin;

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
        public ItemStack[] getArmorStacks(BipedEntityRenderState state, EquipmentSlot armorSlot, EquipmentModel.LayerType layerType, ArmourType type) {
            config = state.getData(ShowMeYourSkinClient.ARMOR_CONFIG_KEY);
            return parent.getArmorStacks(state, armorSlot, layerType, type);
        }

        @Override
        public void onArmourRendered(LivingEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, EquipmentSlot armorSlot, EquipmentModel.LayerType layerType, ArmourType type) {
            parent.onArmourRendered(state, matrices, queue, armorSlot, layerType, type);
        }

        @Override
        public float getGlintAlpha(EquipmentSlot slot, ItemStack stack) {
            return getPieceConfig(slot).glint() ? parent.getGlintAlpha(slot, stack) : 0;
        }

        @Override
        public float getArmourAlpha(EquipmentSlot slot, EquipmentModel.LayerType layerType) {
            return getPieceConfig(slot).base() ? parent.getArmourAlpha(slot, layerType) : 0;
        }

        @Override
        public float getTrimAlpha(EquipmentSlot slot, ArmorTrim trim, EquipmentModel.LayerType layerType) {
            return getPieceConfig(slot).trim() ? parent.getTrimAlpha(slot, trim, layerType) : 0;
        }

        @Override
        public float getElytraAlpha(ItemStack stack, Model<?> model, LivingEntityRenderState state) {
            return getElytraConfig().base() ? parent.getElytraAlpha(stack, model, state) : 0;
        }

        @Override
        @Nullable
        public RenderLayer getArmourLayer(EquipmentSlot slot, Identifier texture, EquipmentModel.LayerType layerType) {
            return parent.getArmourLayer(slot, texture, layerType);
        }

        @Override
        @Nullable
        public RenderLayer getTrimLayer(EquipmentSlot slot, ArmorTrim trim, EquipmentModel.LayerType layerType, RegistryKey<EquipmentAsset> assetId) {
            return parent.getTrimLayer(slot, trim, layerType, assetId);
        }

        @Override
        @Nullable
        public RenderLayer getCapeLayer(BipedEntityRenderState state, Identifier texture) {
            return getElytraConfig().base() ? parent.getCapeLayer(state, texture) : null;
        }
    }
}
