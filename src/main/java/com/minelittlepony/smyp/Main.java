package com.minelittlepony.smyp;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentModel;
import net.minecraft.item.equipment.EquipmentModel.LayerType;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import nl.enjarai.showmeyourskin.client.ModRenderLayers;
import nl.enjarai.showmeyourskin.config.HideableEquipment;
import nl.enjarai.showmeyourskin.util.ArmorContext;
import nl.enjarai.showmeyourskin.util.MixinContext;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.client.model.armour.ArmourRendererPlugin;
import com.minelittlepony.client.model.armour.ArmourRendererPlugin.ArmourType;

public class Main implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ArmourRendererPlugin.register(PluginImpl::new);
    }

    static final class PluginImpl implements ArmourRendererPlugin {
        @Nullable
        private ArmorContext context;

        private final ArmourRendererPlugin parent;

        PluginImpl(ArmourRendererPlugin parent) {
            this.parent = parent;
        }

        //@Override
        public ItemStack[] getArmorStacks(LivingEntity entity, EquipmentSlot armorSlot, EquipmentModel.LayerType layerType, ArmourType type) {
            MixinContext.ENTITY.setContext(entity);
            MixinContext.ARMOR.setContext(context = new ArmorContext(switch (type) {
                case ARMOUR -> HideableEquipment.fromSlot(armorSlot);
                case CAPE, ELYTRA -> HideableEquipment.ELYTRA;
                case SKULL -> HideableEquipment.HAT;
            }, entity));
            return new ItemStack[] {  entity.getEquippedStack(armorSlot) };
        }

        @Override
        public ItemStack[] getArmorStacks(BipedEntityRenderState state, EquipmentSlot armorSlot, EquipmentModel.LayerType layerType, ArmourType type) {
            MixinContext.ENTITY.setContext(entity);
            MixinContext.ARMOR.setContext(context = new ArmorContext(switch (type) {
                case ARMOUR -> HideableEquipment.fromSlot(armorSlot);
                case CAPE, ELYTRA -> HideableEquipment.ELYTRA;
                case SKULL -> HideableEquipment.HAT;
            }, state));
            return parent.getArmorStacks(state, armorSlot, layerType, type);
        }

        @Override
        public void onArmourRendered(LivingEntityRenderState state, MatrixStack matrices, VertexConsumerProvider provider, EquipmentSlot armorSlot, EquipmentModel.LayerType layerType, ArmourType type) {
            MixinContext.ENTITY.clearContext();
            MixinContext.ARMOR.clearContext();
            parent.onArmourRendered(state, matrices, provider, armorSlot, layerType, type);
        }

        @Override
        public float getGlintAlpha(EquipmentSlot slot, ItemStack stack) {
            return parent.getGlintAlpha(slot, stack) * (context == null || !context.shouldModify() ? 1 : context.getApplicableGlintTransparency());
        }

        @Override
        public float getArmourAlpha(EquipmentSlot slot, EquipmentModel.LayerType layerType) {
            return context != null && context.shouldModify() ? context.getApplicablePieceTransparency() : parent.getArmourAlpha(slot, layerType);
        }

        @Override
        public float getTrimAlpha(EquipmentSlot slot, ArmorTrim trim, EquipmentModel.LayerType layerType) {
            return context != null && context.shouldModify() ? context.getApplicableTrimTransparency() : parent.getTrimAlpha(slot, trim, layerType);
        }

        @Override
        public float getElytraAlpha(ItemStack stack, Model model, LivingEntityRenderState entity) {
            MixinContext.ARMOR.setContext(context = new ArmorContext(HideableEquipment.ELYTRA, entity));
            return parent.getElytraAlpha(stack, model, entity) * (context.shouldModify() ? context.getApplicablePieceTransparency() : 1);
        }

        @Override
        @Nullable
        public RenderLayer getArmourLayer(EquipmentSlot slot, Identifier texture, EquipmentModel.LayerType layerType) {
            return context != null && context.shouldModify() && context.getApplicablePieceTransparency() < 1
                    ? ModRenderLayers.ARMOR_TRANSLUCENT_NO_CULL.apply(texture)
                    : parent.getArmourLayer(slot, texture, layerType);
        }

        @Override
        @Nullable
        public RenderLayer getTrimLayer(EquipmentSlot slot, ArmorTrim trim, EquipmentModel.LayerType layerType, Identifier modelId) {
            return context != null && context.shouldModify() && context.getApplicableTrimTransparency() < 1
                    ? ModRenderLayers.ARMOR_TRANSLUCENT_NO_CULL.apply(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE)
                    : parent.getTrimLayer(slot, trim, layerType, modelId);
        }

        @Override
        @Nullable
        public RenderLayer getCapeLayer(BipedEntityRenderState entity, Identifier texture) {
            MixinContext.ARMOR.setContext(context = new ArmorContext(HideableEquipment.ELYTRA, entity));
            return context.shouldModify() && context.getApplicablePieceTransparency() <= 0
                    ? null
                    : parent.getCapeLayer(entity, texture);
        }
    }
}
