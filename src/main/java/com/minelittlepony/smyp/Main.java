package com.minelittlepony.smyp;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import nl.enjarai.showmeyourskin.client.ModRenderLayers;
import nl.enjarai.showmeyourskin.config.HideableEquipment;
import nl.enjarai.showmeyourskin.util.ArmorContext;
import nl.enjarai.showmeyourskin.util.MixinContext;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.client.model.armour.ArmourLayer;
import com.minelittlepony.client.model.armour.ArmourRendererPlugin;

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

        @Override
        public ItemStack[] getArmorStacks(LivingEntity entity, EquipmentSlot armorSlot, ArmourLayer layer, ArmourType type) {
            MixinContext.ENTITY.setContext(entity);
            MixinContext.ARMOR.setContext(context = new ArmorContext(switch (type) {
                case ARMOUR -> HideableEquipment.fromSlot(armorSlot);
                case CAPE, ELYTRA -> HideableEquipment.ELYTRA;
                case SKULL -> HideableEquipment.HAT;
            }, entity));
            return parent.getArmorStacks(entity, armorSlot, layer, type);
        }

        @Override
        public void onArmourRendered(LivingEntity entity, MatrixStack matrices, VertexConsumerProvider provider, EquipmentSlot armorSlot, ArmourLayer layer, ArmourType type) {
            MixinContext.ENTITY.clearContext();
            MixinContext.ARMOR.clearContext();
            parent.onArmourRendered(entity, matrices, provider, armorSlot, layer, type);
        }

        @Override
        public float getGlintAlpha(EquipmentSlot slot, ItemStack stack) {
            return parent.getGlintAlpha(slot, stack) * (context == null || !context.shouldModify() ? 1 : context.getApplicableGlintTransparency());
        }

        @Override
        public float getArmourAlpha(EquipmentSlot slot, ArmourLayer layer) {
            return context != null && context.shouldModify() ? context.getApplicablePieceTransparency() : parent.getArmourAlpha(slot, layer);
        }

        @Override
        public float getTrimAlpha(EquipmentSlot slot, RegistryEntry<ArmorMaterial> material, ArmorTrim trim, ArmourLayer layer) {
            return context != null && context.shouldModify() ? context.getApplicableTrimTransparency() : parent.getTrimAlpha(slot, material, trim, layer);
        }

        @Override
        public float getElytraAlpha(ItemStack stack, Model model, LivingEntity entity) {
            MixinContext.ARMOR.setContext(context = new ArmorContext(HideableEquipment.ELYTRA, entity));
            return parent.getElytraAlpha(stack, model, entity) * (context.shouldModify() ? context.getApplicablePieceTransparency() : 1);
        }

        @Override
        @Nullable
        public RenderLayer getArmourLayer(EquipmentSlot slot, Identifier texture, ArmourLayer layer) {
            return context != null && context.shouldModify() && context.getApplicablePieceTransparency() < 1
                    ? ModRenderLayers.ARMOR_TRANSLUCENT_NO_CULL.apply(texture)
                    : parent.getArmourLayer(slot, texture, layer);
        }

        @Override
        @Nullable
        public RenderLayer getTrimLayer(EquipmentSlot slot, RegistryEntry<ArmorMaterial> material, ArmorTrim trim, ArmourLayer layer) {
            return context != null && context.shouldModify() && context.getApplicableTrimTransparency() < 1
                    ? ModRenderLayers.ARMOR_TRANSLUCENT_NO_CULL.apply(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE)
                    : parent.getTrimLayer(slot, material, trim, layer);
        }

        @Override
        @Nullable
        public RenderLayer getCapeLayer(LivingEntity entity, Identifier texture) {
            MixinContext.ARMOR.setContext(context = new ArmorContext(HideableEquipment.ELYTRA, entity));
            return context.shouldModify() && context.getApplicablePieceTransparency() <= 0
                    ? null
                    : parent.getCapeLayer(entity, texture);
        }

        @Override
        @Nullable
        public VertexConsumer getElytraConsumer(ItemStack stack, Model model, LivingEntity entity, VertexConsumerProvider provider, Identifier texture) {
            MixinContext.ARMOR.setContext(context = new ArmorContext(HideableEquipment.ELYTRA, entity));
            if (context.shouldModify() && context.getApplicablePieceTransparency() < 1) {
                return ItemRenderer.getDirectItemGlintConsumer(provider, ModRenderLayers.ARMOR_TRANSLUCENT_NO_CULL.apply(texture), false, getGlintAlpha(EquipmentSlot.CHEST, stack) > 0);
            }
            return parent.getElytraConsumer(stack, model, entity, provider, texture);
        }
    }
}
